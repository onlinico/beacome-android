package co.onlini.beacome.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.onlini.beacome.BuildConfig;
import co.onlini.beacome.DataService;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.event.GetCardsByBeaconResult;
import co.onlini.beacome.event.ScannerServiceState;
import co.onlini.beacome.model.BeaconInfo;
import co.onlini.beacome.util.BeaconDataParser;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScannerService extends Service {
    private static final int MANUFACTURER_ID_APPLE = 0x4C;
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb");
    private static final String TAG = ScannerService.class.getSimpleName();

    private static final int SCAN_STAGE_DURATION = 5000;
    private static final int IDLE_STAGE_DURATION = 1000;

    private static final int KICK_BEACON_TIMEOUT = 30000;
    private final ArrayList<ScanningEventListener> mObservers = new ArrayList<>();
    private volatile boolean mIsRunning;
    private BluetoothLeScannerCompat mLeScannerCompat;
    private volatile Map<String, BeaconInfo> mProcessedBeaconsMap;
    private Handler mUiHandler;
    private ScanSettings mScanSettings;
    private volatile Map<String, List<String>> mBeaconToCardLinks = new HashMap<>();
    private IBinder mBinder = new ServiceBinder();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeDeviceFound(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onLeDeviceFound(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed " + errorCode);
        }
    };
    private Runnable mScanningCycleTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "suspended scanning");
            try {
                mLeScannerCompat.flushPendingScanResults(mScanCallback);
                mLeScannerCompat.stopScan(mScanCallback);
            } catch (Exception e) {
                Log.d(TAG, "No scanning callback");
            }
            mUiHandler.postDelayed(mRestartScannerTask, IDLE_STAGE_DURATION);
            ArrayList<String> removedBeacons = new ArrayList<>();
            for (Map.Entry<String, BeaconInfo> entry : mProcessedBeaconsMap.entrySet()) {
                if ((KICK_BEACON_TIMEOUT + entry.getValue().getLastDiscoveryTimeStamp()) < System.currentTimeMillis()) {
                    removedBeacons.add(entry.getKey());
                }
            }
            for (String removedBeaconUuid : removedBeacons) {
                BeaconInfo beaconInfo = mProcessedBeaconsMap.get(removedBeaconUuid);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Beacon left:" + beaconInfo.getUuid());
                }
                mProcessedBeaconsMap.remove(removedBeaconUuid);
                notifyListenersDeviceLost(beaconInfo);
            }
            if (removedBeacons.size() > 0) {
                clearNearCardsForBeacon(removedBeacons.toArray(new String[removedBeacons.size()]));
            }
        }
    };
    private Runnable mRestartScannerTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "resumed scanning");
            if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON) {
                try {
                    mLeScannerCompat.startScan(null, mScanSettings, mScanCallback);
                } catch (Exception e) {
                    Log.d(TAG, "No scanning callback");
                }
                mUiHandler.postDelayed(mScanningCycleTask, SCAN_STAGE_DURATION);
            }
        }
    };
    private final BroadcastReceiver mBtStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        stopScanning();
                        stopSelf();
                        break;
                }
            }
        }
    };

    private void setNearCardsForBeacon(String beaconUuid, List<String> cardUuids) {
        synchronized (this) {
            mBeaconToCardLinks.put(beaconUuid, cardUuids);
            notifyListenersNearBeaconsCardsListChanged();
        }
    }

    private void clearNearCardsForBeacon(String[] beaconUuids) {
        if (beaconUuids == null) {
            return;
        }
        synchronized (this) {
            for (String uuid : beaconUuids) {
                mBeaconToCardLinks.remove(uuid);
            }
            if (beaconUuids.length > 0) {
                notifyListenersNearBeaconsCardsListChanged();
            }
        }
    }

    private void clearNearCardsAll() {
        synchronized (this) {
            mBeaconToCardLinks.clear();
            notifyListenersNearBeaconsCardsListChanged();
        }
    }

    public Collection<String> getNearBeaconsCardsUuids() {
        synchronized (this) {
            Set<String> cardsUuids = new HashSet<>();
            for (Map.Entry<String, List<String>> entry : mBeaconToCardLinks.entrySet()) {
                cardsUuids.addAll(entry.getValue());
            }
            return cardsUuids;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mBtStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mProcessedBeaconsMap = new HashMap<>();
        mLeScannerCompat = BluetoothLeScannerCompat.getScanner();
        mUiHandler = new Handler(getMainLooper());
        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setUseHardwareBatchingIfSupported(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy scanning service");
        unregisterReceiver(mBtStatusReceiver);
        stopScanning();
        if (mObservers.size() > 0) {
            Log.e(TAG, "Service is stopping, but still has subscribers");
        }
        super.onDestroy();
    }

    public void startScanning() {
        synchronized (this) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
            if (!mIsRunning) {
                Log.d(TAG, "start scanning");
                mProcessedBeaconsMap.clear();
                clearNearCardsAll();
                mLeScannerCompat.startScan(null, mScanSettings, mScanCallback);
                mIsRunning = true;
                mUiHandler.postDelayed(mScanningCycleTask, SCAN_STAGE_DURATION);
                sendStatusChangeBroadcast();
            }
        }
    }

    public void stopScanning() {
        synchronized (this) {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            Log.d(TAG, "stop scanning");
            mProcessedBeaconsMap.clear();
            clearNearCardsAll();
            mUiHandler.removeCallbacks(mScanningCycleTask);
            mUiHandler.removeCallbacks(mRestartScannerTask);
            try {
                mLeScannerCompat.stopScan(mScanCallback);
            } catch (Exception e) {
                //do nothing
            }
            mIsRunning = false;
            sendStatusChangeBroadcast();
        }
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(GetCardsByBeaconResult result) {
        synchronized (this) {
            if (result.isSuccessful()) {
                setNearCardsForBeacon(result.getBeaconUuid(), result.getLinkedCardsUuids());
            } else {
                mProcessedBeaconsMap.remove(result.getBeaconUuid());
            }
        }
    }

    private void sendStatusChangeBroadcast() {
        EventBus.getDefault().postSticky(new ScannerServiceState(mIsRunning));
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    private void processBeaconInfo(final BeaconInfo beaconInfo) {
        if (mProcessedBeaconsMap.put(beaconInfo.getUuid(), beaconInfo) == null) {
            DataService.pullCardsByBeacon(this, SessionManager.getSession(this), beaconInfo.getUuid());
            notifyListenersDeviceFound(beaconInfo);
            Log.d(TAG, "Beacon entered:" + beaconInfo.getUuid());
        }
    }

    private void onLeDeviceFound(ScanResult scanResult) {
        BeaconInfo beaconInfo;
        if ((beaconInfo = getEddystoneBeacon(scanResult)) != null || (beaconInfo = getAppleBeacon(scanResult)) != null) {
            processBeaconInfo(beaconInfo);
        }
    }

    private BeaconInfo getAppleBeacon(ScanResult scanResult) {
        if (scanResult == null || scanResult.getScanRecord() == null) {
            return null;
        }
        byte[] manufacturerData = scanResult.getScanRecord().getManufacturerSpecificData(MANUFACTURER_ID_APPLE);
        return BeaconDataParser.parseAppleBeaconData(manufacturerData, scanResult);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private BeaconInfo getEddystoneBeacon(ScanResult scanResult) {
        if (scanResult == null || scanResult.getScanRecord() == null) {
            return null;
        }
        byte[] eddystoneBeaconData = scanResult.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);
        return BeaconDataParser.parseEddystoneBeaconData(eddystoneBeaconData, scanResult);
    }

    public BeaconInfo[] getBeacons() {
        BeaconInfo[] beaconInfos = new BeaconInfo[mProcessedBeaconsMap.size()];
        int i = 0;
        for (Map.Entry<String, BeaconInfo> entry : mProcessedBeaconsMap.entrySet()) {
            beaconInfos[i++] = entry.getValue();
        }
        return beaconInfos;
    }

    private void notifyListenersDeviceLost(final BeaconInfo beaconInfo) {
        if (beaconInfo == null) {
            return;
        }
        for (ScanningEventListener listener : mObservers) {
            listener.onBeaconLost(beaconInfo);
        }
    }

    private void notifyListenersNearBeaconsCardsListChanged() {
        for (ScanningEventListener listener : mObservers) {
            listener.onNearBeaconsCardsListChanged();
        }
    }

    private void notifyListenersDeviceFound(BeaconInfo beaconInfo) {
        if (beaconInfo == null) {
            return;
        }
        for (ScanningEventListener listener : mObservers) {
            listener.onBeaconFound(beaconInfo);
        }
    }

    public void registerObserver(ScanningEventListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                throw new IllegalStateException("Observer " + observer + " is already registered.");
            }
            mObservers.add(observer);
        }
    }

    public void unregisterObserver(ScanningEventListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
                throw new IllegalStateException("Observer " + observer + " was not registered.");
            }
            mObservers.remove(index);
        }
    }

    public interface ScanningEventListener {
        void onBeaconFound(BeaconInfo beaconInfo);

        void onBeaconLost(BeaconInfo beaconInfo);

        void onNearBeaconsCardsListChanged();
    }

    public class ServiceBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }

}