package co.onlini.beacome.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import co.onlini.beacome.event.AdvertiserServiceState;
import co.onlini.beacome.util.BluetoothUtil;
import co.onlini.beacome.util.DataUtil;

public class AdvertiserService extends Service {
    private static final int MANUFACTURER_ID_APPLE = 0x4C;
    private static final byte[] DEVICE_TYPE_IBEACON = new byte[]{0x02, 0x15};
    private static final String TAG = AdvertiserService.class.getSimpleName();
    private static final int ADVERTISEMENT_DATA_SIZE = 23; //bytes

    private static final String COMMAND_START = "command_start";
    private static final String COMMAND_STOP = "command_stop";

    private static final String EXTRA_COMMAND = "extra_command";
    private static final String EXTRA_ADVERTISE_DATA = "extra_advertise_data";
    private final BroadcastReceiver mBtStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        stopSelf();
                        break;
                }
            }
        }
    };
    private boolean mIsAdvertise;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mCallback;

    public static void startAdvertising(@NonNull Context context, @NonNull String uuidString, short major, short minor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "Unable to start an advertising, minSdkVersion is 21");
            return;
        }
        byte[] advertisingData = null;
        try {
            advertisingData = prepareData(uuidString, major, minor);
        } catch (Exception e) {
            //do nothing
        }
        if (advertisingData == null) {
            Log.e(TAG, "Unable to start an advertising, advertisingData is not correct");
            return;
        }
        Intent intent = new Intent(context, AdvertiserService.class);
        intent.putExtra(EXTRA_COMMAND, COMMAND_START);
        intent.putExtra(EXTRA_ADVERTISE_DATA, advertisingData);
        context.startService(intent);
    }

    public static void stopAdvertise(@NonNull Context context) {
        Intent intent = new Intent(context, AdvertiserService.class);
        intent.putExtra(EXTRA_COMMAND, COMMAND_STOP);
        context.startService(intent);
    }

    private static byte[] prepareData(String uuidString, short major, short minor) {
        UUID u = UUID.fromString(uuidString);
        byte[] uuidBytes = DataUtil.uuidToByteArray(u);
        byte[] majorBytes = DataUtil.shortToByteArray(major);
        byte[] minorBytes = DataUtil.shortToByteArray(minor);
        int avrPowerDbm = -56; //Average power level value for AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
        byte[] power = new byte[]{(byte) avrPowerDbm};
        byte[] manufactureData = new byte[ADVERTISEMENT_DATA_SIZE];
        int i = 0;
        System.arraycopy(DEVICE_TYPE_IBEACON, 0, manufactureData, i, DEVICE_TYPE_IBEACON.length);
        System.arraycopy(uuidBytes, 0, manufactureData, i += DEVICE_TYPE_IBEACON.length, uuidBytes.length);
        System.arraycopy(majorBytes, 0, manufactureData, i += uuidBytes.length, majorBytes.length);
        System.arraycopy(minorBytes, 0, manufactureData, i += majorBytes.length, minorBytes.length);
        System.arraycopy(power, 0, manufactureData, i += minorBytes.length, power.length);
        return manufactureData;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(mBtStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            mCallback = new AdvertiseCallback() {
                @Override
                public void onStartFailure(int errorCode) {
                    Log.e(TAG, "onStartFailure " + errorCode);
                }
            };
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra(EXTRA_COMMAND);
        if (command == null) {
            Log.e(TAG, "There is no required params, use startAdvertising and stopAdvertising methods");
            stopSelf();
        } else {
            if (COMMAND_START.equals(command)) {
                byte[] data = intent.getByteArrayExtra(EXTRA_ADVERTISE_DATA);
                if (data != null) {
                    if (mIsAdvertise) {
                        stopAdvertising();
                    }
                    advertise(data);
                } else {
                    Log.e(TAG, "Advertising data is null");
                    stopSelf();
                }
            } else {
                stopAdvertising();
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBtStatusReceiver);
        stopAdvertising();
        super.onDestroy();
    }

    private void advertise(byte[] manufactureData) {
        if (BluetoothUtil.isBluetoothTurnedOn()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothLeAdvertiser != null && manufactureData != null
                        && manufactureData.length == ADVERTISEMENT_DATA_SIZE) {
                    AdvertiseSettings settings = new AdvertiseSettings.Builder()
                            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                            .setConnectable(false)
                            .setTimeout(0)
                            .build();
                    AdvertiseData adData = new AdvertiseData.Builder()
                            .addManufacturerData(MANUFACTURER_ID_APPLE, manufactureData)
                            .build();
                    Log.d(TAG, String.format("Advertising data: %s", DataUtil.bytesToHexStr(manufactureData)));
                    mBluetoothLeAdvertiser.startAdvertising(settings, adData, mCallback);
                    mIsAdvertise = true;
                } else {
                    Log.e(TAG, "Manufacturer data, invalid format");
                }
            } else {
                Log.e(TAG, "Advertising is not available on pre-Lollipop devices");
            }
        } else {
            Log.e(TAG, "Bluetooth is turned off");
        }
        sendAdvertisingStateChanged();
    }

    private void stopAdvertising() {
        synchronized (this) {
            Log.d(TAG, "stop advertising");
            mIsAdvertise = false;
            if (BluetoothUtil.isBluetoothTurnedOn()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mBluetoothLeAdvertiser != null && BluetoothUtil.isBluetoothTurnedOn()) {
                        mBluetoothLeAdvertiser.stopAdvertising(mCallback);
                    }
                }
            } else {
                Log.e(TAG, "Bluetooth is turned off");
            }
            sendAdvertisingStateChanged();
        }
    }

    private void sendAdvertisingStateChanged() {
        EventBus.getDefault().postSticky(new AdvertiserServiceState(mIsAdvertise));
    }

}