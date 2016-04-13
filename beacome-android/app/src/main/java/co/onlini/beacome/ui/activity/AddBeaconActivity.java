package co.onlini.beacome.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.onlini.beacome.R;
import co.onlini.beacome.bluetooth.ScannerService;
import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.BeaconInfo;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.ui.adapter.BeaconListAdapter;
import co.onlini.beacome.util.HexCharsInputFilter;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class AddBeaconActivity extends AppCompatActivity implements
        ScannerService.ScanningEventListener {

    public static final String EXTRA_BEACON_UUID = "extra_beacon_uuid";
    private static final int REQUEST_ENABLE_BT = 0x1;
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 0x2;
    private static final String SAVED_STATE_BEACONS = "saved_state_beacons";
    private static final String SAVED_STATE_SCANNER = "saved_state_scanner";
    private ListView mLvBeacons;
    private EditText mEtBeaconUuid;
    private volatile ScannerService mScannerService;
    private boolean mIsScannerServiceHasStarted;
    private Map<String, Beacon> mBeaconLinks;
    private Beacon[] mStoredLinks;
    private TabLayout mTabLayout;

    private final ServiceConnection mScanningServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScannerService = ((ScannerService.ServiceBinder) service).getService();
            mScannerService.registerObserver(AddBeaconActivity.this);
            if (!mScannerService.isRunning()) {
                startScanning();
            } else {
                updateBeacons();
                mIsScannerServiceHasStarted = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScannerService = null;
        }
    };

    private final MenuItem.OnMenuItemClickListener mOnMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_link:
                    returnBeacon();
                    break;
                default:
                    onBackPressed();
            }
            return true;
        }
    };

    public static Intent getIntent(Context context) {
        return new Intent(context, AddBeaconActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeaconLinks = new HashMap<>();
        if (savedInstanceState != null) {
            Parcelable[] links = savedInstanceState.getParcelableArray(SAVED_STATE_BEACONS);
            if (links != null) {
                for (Parcelable link : links) {
                    Beacon beaconLink = (Beacon) link;
                    mBeaconLinks.put(beaconLink.getBeaconUuid(), beaconLink);
                }
            }
            mIsScannerServiceHasStarted = savedInstanceState.getBoolean(SAVED_STATE_SCANNER);
        }
        setContentView(R.layout.activity_add_beacon);
        mLvBeacons = (ListView) findViewById(R.id.lv_beacons);
        mEtBeaconUuid = (EditText) findViewById(R.id.et_beacon_uuid);
        mEtBeaconUuid.setFilters(new InputFilter[]{new HexCharsInputFilter()});
        mLvBeacons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Beacon beacon = (Beacon) mLvBeacons.getAdapter().getItem(position);
                if (beacon != null) {
                    finishWithResult(beacon);
                }
            }
        });
        initToolbar();
        initTabs();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArray(SAVED_STATE_BEACONS, mBeaconLinks.values().toArray(new Beacon[mBeaconLinks.size()]));
        outState.putBoolean(SAVED_STATE_SCANNER, mIsScannerServiceHasStarted);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindScanService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unBindScanningService();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_COARSE_LOCATION) {
            if (permissions.length == 1 && Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                startScanning();
            }
        }
    }

    private boolean checkBTState() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    private void unBindScanningService() {
        if (mScannerService != null) {
            if (!mIsScannerServiceHasStarted) {
                mScannerService.stopScanning();
            }
            mScannerService.unregisterObserver(this);
            this.unbindService(mScanningServiceConnection);
            mScannerService = null;
        }
    }

    private boolean isLocationProviderNeed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (mLocationManager != null) {
            try {
                gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                //do nothing
            }
            try {
                network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                //do nothing
            }
        }
        return gps_enabled || network_enabled;
    }

    private void bindScanService() {
        this.bindService(new Intent(this, ScannerService.class), mScanningServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean checkLocationPermission() {
        return PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PermissionChecker.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private void startScanning() {
        if (mScannerService != null) {
            if (checkLocationPermission()) {
                if (checkBTState()) {
                    if (isLocationProviderNeed()) {
                        mScannerService.startScanning();
                        updateBeacons();
                    } else {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_COARSE_LOCATION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_action_link, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_link).setOnMenuItemClickListener(mOnMenuItemClickListener);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        hideKeyboard();
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    @SuppressWarnings("ConstantConditions")
    private void initTabs() {
        mTabLayout = (TabLayout) findViewById(R.id.tab_lay);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_add_beacon_enter_uuid));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_add_beacon_scan));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() != 0) {
                    updateList();
                    hideKeyboard();
                    mLvBeacons.setVisibility(View.VISIBLE);
                    mEtBeaconUuid.setVisibility(View.GONE);
                } else {
                    showKeyboard();
                    mLvBeacons.setVisibility(View.GONE);
                    mEtBeaconUuid.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void returnBeacon() {
        //noinspection StringEquality
        if (mTabLayout.getSelectedTabPosition() == 0) {
            String rawUuid = mEtBeaconUuid.getText().toString();
            String uuid = parseUuid(rawUuid);
            if (uuid != null) {
                Session session = SessionManager.getSession(this);
                CardLink[] links = CardHelper.getInstance(this).getCardLinksByBeacon(uuid, session.getUserUuid());
                finishWithResult(new Beacon(uuid, links));
            } else {
                mEtBeaconUuid.setError(getString(R.string.et_error_invalid_uuid));
            }
        } else {
            showSelectUuidDialog();
        }
    }

    private String parseUuid(String src) {
        String result = null;
        if (src != null) {
            try {
                result = UUID.fromString(src).toString().toLowerCase();
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
        return result;
    }

    private void showSelectUuidDialog() {
        Toast.makeText(this, R.string.toast_select_beacon_no_selected, Toast.LENGTH_LONG).show();
    }

    private void finishWithResult(Beacon beacon) {
        Intent data = new Intent();
        data.putExtra(EXTRA_BEACON_UUID, beacon);
        setResult(RESULT_OK, data);
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtBeaconUuid, 0);
    }

    private void updateBeacons() {
        if (mScannerService == null) {
            return;
        }
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean hasChanges = false;
                if (mStoredLinks == null) {
                    Session session = SessionManager.getSession(AddBeaconActivity.this);
                    mStoredLinks = CardHelper.getInstance(AddBeaconActivity.this).getBeaconsByUser(session.getUserUuid());
                }
                for (BeaconInfo beaconInfo : mScannerService.getBeacons()) {
                    String beaconUuid = beaconInfo.getUuid();
                    if (!mBeaconLinks.containsKey(beaconUuid)) {
                        Beacon link = null;
                        for (Beacon beaconLink : mStoredLinks) {
                            if (beaconLink.getBeaconUuid().equals(beaconUuid)) {
                                link = new Beacon(beaconLink.getBeaconUuid(), beaconLink.getCardLinks());
                                break;
                            }
                        }
                        if (link == null) {
                            link = new Beacon(beaconUuid, new CardLink[0]);
                        }
                        mBeaconLinks.put(beaconUuid, link);
                        hasChanges = true;
                    }
                }
                return hasChanges;
            }

            @Override
            protected void onPostExecute(Boolean hasChanges) {
                if (hasChanges && !AddBeaconActivity.this.isDestroyed() && mTabLayout.getSelectedTabPosition() == 1) {
                    updateList();
                }
            }
        }.execute();
    }

    private void updateList() {
        if (mTabLayout.getSelectedTabPosition() == 1) {
            List<Beacon> links = new ArrayList<>(mBeaconLinks.values());
            mLvBeacons.setAdapter(new BeaconListAdapter(links));
        }
    }

    @Override
    public void onBeaconFound(BeaconInfo beaconInfo) {
        updateBeacons();
    }

    @Override
    public void onBeaconLost(BeaconInfo beaconInfo) {
        //do nothing
    }

    @Override
    public void onNearBeaconsCardsListChanged() {
        //do nothing
    }
}
