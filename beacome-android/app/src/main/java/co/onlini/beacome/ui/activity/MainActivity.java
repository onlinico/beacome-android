package co.onlini.beacome.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.Set;

import co.onlini.beacome.R;
import co.onlini.beacome.bluetooth.AdvertiserService;
import co.onlini.beacome.bluetooth.ScannerService;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.dal.UserHelper;
import co.onlini.beacome.event.AdvertiserServiceState;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.ScannerServiceState;
import co.onlini.beacome.event.UserAccountUpdatingResult;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.model.User;
import co.onlini.beacome.ui.NavigationActivity;
import co.onlini.beacome.ui.ServiceProvider;
import co.onlini.beacome.ui.fragment.AccountFragment;
import co.onlini.beacome.ui.fragment.BeaconDetailsFragment;
import co.onlini.beacome.ui.fragment.BeaconsListFragment;
import co.onlini.beacome.ui.fragment.CardsListFragment;
import co.onlini.beacome.ui.fragment.DiscountsListFragment;
import co.onlini.beacome.ui.fragment.HistoryFragment;
import co.onlini.beacome.util.BluetoothUtil;
import co.onlini.beacome.util.LocationServiceUtil;

public class MainActivity extends BaseActivity implements NavigationActivity, ServiceProvider {
    public static final String FRAGMENT_BEACONS = "beacons";
    public static final String FRAGMENT_HISTORY = "history";
    public static final String FRAGMENT_CARDS = "cards";
    public static final String FRAGMENT_TRANSLATOR = "transmitter";
    public static final String FRAGMENT_ACCOUNT = "account";
    public static final String FRAGMENT_DISCOUNTS = "discounts";

    private static final int REQUEST_ENABLE_BT = 0x11;
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 0x12;
    private static final int LOADER_USER_DATA = 0x1;
    private android.support.v7.widget.SwitchCompat mScanSwitch;
    private DrawerLayout mDrawer;
    private ImageView mUserPic;
    private TextView mTvUserName;
    private UserHelper mUserHelper;
    private View.OnClickListener mSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signOut();
        }
    };
    private ScannerService mScannerService;
    private TextView mNavItemFavorites;
    private TextView mNavItemHistory;
    private TextView mNavItemNear;
    private TextView mNavItemTranslator;
    private TextView mNavItemBeacon;
    private TextView mNavItemCards;
    private TextView mNavItemAccount;
    private LoaderManager.LoaderCallbacks<User> mUserPicLoader = new LoaderManager.LoaderCallbacks<User>() {
        @Override
        public android.content.Loader<User> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<User>(MainActivity.this) {
                @Override
                public User loadInBackground() {
                    String userUuid = SessionManager.getSession(getContext()).getUserUuid();
                    return mUserHelper.getUser(userUuid);
                }

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                protected void onStopLoading() {
                    cancelLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(android.content.Loader<User> loader, User data) {
            if (data != null) {
                Glide.with(MainActivity.this)
                        .load(data.getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()

                        .signature(new StringSignature(String.valueOf(data.getVersion())))
                        .into(mUserPic);
                mTvUserName.setText(data.getName());
            }
        }

        @Override
        public void onLoaderReset(android.content.Loader<User> loader) {

        }
    };
    private Set<ServiceProvider.ServiceDst<ScannerService>> mScanningServiceDsts = new HashSet<>();
    private Session mSession;
    private android.support.v7.widget.SwitchCompat mAdvertiserSwitch;
    private boolean mIsAdvertiserRequiresBt;
    private ServiceConnection mScanningServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScannerService = ((ScannerService.ServiceBinder) service).getService();
            mScanSwitch.setChecked(mScannerService.isRunning());
            if (SessionManager.getSession(MainActivity.this).isScannerRunning()) {
                startScanning();
            }
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.lay_container);
            if (fragment != null && FRAGMENT_HISTORY.equals(fragment.getTag())) {
                ((HistoryFragment) fragment).updateNearDevicesList();
            }

            for (ServiceProvider.ServiceDst<ScannerService> dest : mScanningServiceDsts) {
                dest.onServiceReady(mScannerService);
            }
            mScanningServiceDsts.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScanSwitch.setChecked(false);
            mScannerService = null;
        }
    };
    private View.OnClickListener mOnSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.switch_scan:
                    if (mScannerService != null) {
                        if (mScannerService.isRunning()) {
                            stopScanning();
                        } else {
                            startScanning();
                        }
                    }
                    break;
                case R.id.switch_translator:
                    AdvertiserServiceState stickyEvent = EventBus.getDefault().getStickyEvent(AdvertiserServiceState.class);
                    boolean isRunning = false;
                    if (stickyEvent != null) {
                        isRunning = stickyEvent.isRunning();
                    }
                    if (isRunning) {
                        stopAdvertising();
                    } else {
                        startAdvertising();
                    }
                    break;
            }
        }
    };
    private TextView mNavItemDiscount;
    private View.OnClickListener mNavDrawerItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean doCloseDrawer = true;
            Bundle args;
            switch (v.getId()) {
                case R.id.nav_item_favorites:
                    args = new Bundle();
                    args.putInt(HistoryFragment.ARGUMENT_PAGE, 2);
                    showScreen(FRAGMENT_HISTORY, args);
                    break;
                case R.id.nav_item_history:
                    args = new Bundle();
                    args.putInt(HistoryFragment.ARGUMENT_PAGE, 1);
                    showScreen(FRAGMENT_HISTORY, args);
                    break;
                case R.id.nav_item_near:
                    args = new Bundle();
                    args.putInt(HistoryFragment.ARGUMENT_PAGE, 0);
                    showScreen(FRAGMENT_HISTORY, args);
                    break;
                case R.id.nav_item_discounts:
                    showScreen(FRAGMENT_DISCOUNTS, null);
                    break;
                case R.id.nav_item_beacons:
                    showScreen(FRAGMENT_BEACONS, null);
                    break;
                case R.id.nav_item_cards:
                    showScreen(FRAGMENT_CARDS, null);
                    break;
                case R.id.nav_item_as_translator:
                    String uuid = SessionManager.getSession(MainActivity.this).getDeviseAsBeaconUuid();
                    Bundle bundle = new Bundle();
                    bundle.putString(BeaconDetailsFragment.ARGUMENT_BEACON_UUID, uuid);
                    bundle.putString(BeaconDetailsFragment.ARGUMENT_TITLE, getString(R.string.title_activity_transmitter));
                    showScreen(FRAGMENT_TRANSLATOR, bundle);
                    break;
                case R.id.nav_item_account:
                    showScreen(FRAGMENT_ACCOUNT, null);
                    break;
                default:
                    doCloseDrawer = false;
            }
            if (doCloseDrawer) {
                invalidateOptionsMenu();
                mDrawer.closeDrawer(GravityCompat.START);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserHelper = UserHelper.getInstance(this);
        mSession = SessionManager.getSession(this);
        setContentView(R.layout.activity_main);
        initControls();
        getLoaderManager().initLoader(LOADER_USER_DATA, null, mUserPicLoader);
        showFirstFragment();
        bindScanService();
    }

    private void initControls() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ((InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(MainActivity.this.getWindow().getDecorView().getWindowToken(), 0);
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mTvUserName = (TextView) findViewById(R.id.tv_user_name);
        mAdvertiserSwitch = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switch_translator);
        mNavItemFavorites = (TextView) findViewById(R.id.nav_item_favorites);
        mNavItemHistory = (TextView) findViewById(R.id.nav_item_history);
        mNavItemNear = (TextView) findViewById(R.id.nav_item_near);
        mNavItemDiscount = (TextView) findViewById(R.id.nav_item_discounts);
        mNavItemFavorites.setOnClickListener(mNavDrawerItemOnClickListener);
        mNavItemHistory.setOnClickListener(mNavDrawerItemOnClickListener);
        mNavItemNear.setOnClickListener(mNavDrawerItemOnClickListener);
        mNavItemDiscount.setOnClickListener(mNavDrawerItemOnClickListener);
        mNavItemTranslator = (TextView) findViewById(R.id.nav_item_as_translator);
        mNavItemBeacon = (TextView) findViewById(R.id.nav_item_beacons);
        mNavItemCards = (TextView) findViewById(R.id.nav_item_cards);
        mNavItemAccount = (TextView) findViewById(R.id.nav_item_account);
        TextView tvSignOut = (TextView) findViewById(R.id.tv_sign_out);
        String signBtnText;
        if (!mSession.isAnonymous()) {
            mNavItemTranslator.setOnClickListener(mNavDrawerItemOnClickListener);
            mNavItemBeacon.setOnClickListener(mNavDrawerItemOnClickListener);
            mNavItemCards.setOnClickListener(mNavDrawerItemOnClickListener);
            mNavItemAccount.setOnClickListener(mNavDrawerItemOnClickListener);
            mAdvertiserSwitch.setOnClickListener(mOnSwitchClickListener);
            signBtnText = getString(R.string.nav_btn_sign_out_text_out);
            boolean canAdvertise = checkBTState() && BluetoothUtil.isDeviseCanAdvertise(this);
            mNavItemTranslator.setEnabled(canAdvertise);
            mAdvertiserSwitch.setEnabled(canAdvertise);
        } else {
            mNavItemTranslator.setEnabled(false);
            mNavItemBeacon.setEnabled(false);
            mNavItemCards.setEnabled(false);
            mNavItemAccount.setEnabled(false);
            mAdvertiserSwitch.setEnabled(false);
            signBtnText = getString(R.string.nav_btn_sign_out_text_in);
        }
        //noinspection ConstantConditions
        tvSignOut.setText(signBtnText);
        tvSignOut.setOnClickListener(mSignInClickListener);
        mScanSwitch = (android.support.v7.widget.SwitchCompat) findViewById(R.id.switch_scan);
        //noinspection ConstantConditions
        mScanSwitch.setOnClickListener(mOnSwitchClickListener);
        mUserPic = (ImageView) findViewById(R.id.iv_user_image);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        unBindScanningService();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult exception) {
        hideProgress();
        Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ScannerServiceState result) {
        mScanSwitch.setChecked(result.isRunning());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AdvertiserServiceState result) {
        mAdvertiserSwitch.setChecked(result.isRunning());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserAccountUpdatingResult result) {
        getLoaderManager().restartLoader(LOADER_USER_DATA, null, mUserPicLoader);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_COARSE_LOCATION) {
            if (permissions.length == 1 && Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, R.string.toast_location_permission_disabled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.lay_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (mIsAdvertiserRequiresBt) {
                    startAdvertising();
                } else {
                    startScanning();
                }
                mAdvertiserSwitch.setEnabled(BluetoothUtil.isDeviseCanAdvertise(this));
                mNavItemTranslator.setEnabled(BluetoothUtil.isDeviseCanAdvertise(this));
            } else {
                Toast.makeText(this, R.string.toast_bluetooth_turned_off, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showFirstFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.lay_container) == null) {
            Bundle args = new Bundle();
            args.putInt(HistoryFragment.ARGUMENT_PAGE, 0);
            showScreen(FRAGMENT_HISTORY, args);
        }
    }

    private void startAdvertising() {
        boolean isRunning = false;
        if (checkBTState()) {
            if (BluetoothUtil.isDeviseCanAdvertise(this)) {
                Session session = SessionManager.getSession(this);
                String beaconUuid = session.getDeviseAsBeaconUuid();
                AdvertiserService.startAdvertising(this, beaconUuid, (short) 0, (short) 0);
                isRunning = true;
            } else {
                Toast.makeText(this, R.string.toast_device_does_not_support_advertisement, Toast.LENGTH_SHORT).show();
            }
        } else {
            mIsAdvertiserRequiresBt = true;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        SessionManager.setAdvertiserState(this, isRunning);
        mAdvertiserSwitch.setChecked(isRunning);
    }

    private void stopAdvertising() {
        AdvertiserService.stopAdvertise(this);
        SessionManager.setAdvertiserState(this, false);
        mAdvertiserSwitch.setChecked(false);
    }

    private void unBindScanningService() {
        if (mScannerService != null) {
            boolean isRunning = mScannerService.isRunning();
            this.unbindService(mScanningServiceConnection);
            if (!isRunning) {
                stopService(new Intent(this, ScannerService.class));
            }
            mScannerService = null;
        }
    }

    private void bindScanService() {
        this.bindService(new Intent(this, ScannerService.class), mScanningServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startScanning() {
        boolean isTurned = false;
        if (mScannerService != null) {
            if (checkScanningPreConditions()) {
                // Service already created by binding,
                // execute a start command will keep the service alive after unbinding
                startService(new Intent(this, ScannerService.class));
                mScannerService.startScanning();
                isTurned = true;
            }
        }
        SessionManager.setScannerState(MainActivity.this, isTurned);
        mScanSwitch.setChecked(isTurned);
    }

    private boolean checkScanningPreConditions() {
        boolean isSatisfied = false;
        if (checkLocationPermission()) {
            if (checkBTState()) {
                if (LocationServiceUtil.isLocationProviderNeed(this)) {
                    isSatisfied = true;
                } else {
                    requestLocationService();
                }
            } else {
                mIsAdvertiserRequiresBt = false;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_COARSE_LOCATION);
        }
        return isSatisfied;
    }

    private void requestLocationService() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_location_service_title)
                .setMessage(getString(R.string.dialog_location_service_text))
                .setCancelable(true)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void stopScanning() {
        if (mScannerService != null) {
            SessionManager.setScannerState(MainActivity.this, false);
            mScanSwitch.setChecked(false);
            mScannerService.stopScanning();

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.lay_container);
            if (fragment != null && FRAGMENT_HISTORY.equals(fragment.getTag())) {
                ((HistoryFragment) fragment).updateNearDevicesList();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    public void showScreen(String newFragmentTag, Bundle args) {
        Fragment top_fragment = getSupportFragmentManager().findFragmentById(R.id.lay_container);
        if (top_fragment == null || !top_fragment.getTag().equals(newFragmentTag)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = getFragmentByTag(newFragmentTag);
            fragment.setArguments(args);
            transaction.replace(R.id.lay_container, fragment, newFragmentTag);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (FRAGMENT_HISTORY.equals(newFragmentTag)) {
            if (args == null) {
                args = new Bundle();
                args.putInt(HistoryFragment.ARGUMENT_PAGE, 0);
            }
            setNavItemSelection(FRAGMENT_HISTORY, args.getInt(HistoryFragment.ARGUMENT_PAGE));
            ((HistoryFragment) top_fragment).setTab(args.getInt(HistoryFragment.ARGUMENT_PAGE));
        }
    }

    private Fragment getFragmentByTag(String fragmentTag) {
        Fragment fragment = null;
        switch (fragmentTag) {
            case FRAGMENT_HISTORY:
                fragment = new HistoryFragment();
                break;
            case FRAGMENT_CARDS:
                fragment = CardsListFragment.newInstance();
                break;
            case FRAGMENT_BEACONS:
                fragment = BeaconsListFragment.newInstance();
                break;
            case FRAGMENT_TRANSLATOR:
                fragment = new BeaconDetailsFragment();
                break;
            case FRAGMENT_ACCOUNT:
                fragment = AccountFragment.newInstance();
                break;
            case FRAGMENT_DISCOUNTS:
                fragment = DiscountsListFragment.newInstance();
                break;
        }
        return fragment;
    }

    private void signOut() {
        stopService(new Intent(this, ScannerService.class));
        SessionManager.closeSession(this);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public void getScanningService(ServiceProvider.ServiceDst<ScannerService> dst) {
        if (mScannerService != null) {
            dst.onServiceReady(mScannerService);
        } else {
            mScanningServiceDsts.add(dst);
        }
    }

    @Override
    public void onStartFragment(String fragmentTag, int fragmentTab) {
        setNavItemSelection(fragmentTag, fragmentTab);
    }

    private void setNavItemSelection(String fragmentTag, int fragmentTab) {
        mNavItemNear.setSelected(false);
        mNavItemHistory.setSelected(false);
        mNavItemFavorites.setSelected(false);
        mNavItemBeacon.setSelected(false);
        mNavItemTranslator.setSelected(false);
        mNavItemCards.setSelected(false);
        mNavItemAccount.setSelected(false);
        mNavItemDiscount.setSelected(false);
        switch (fragmentTag) {
            case FRAGMENT_HISTORY:
                switch (fragmentTab) {
                    case 0:
                        mNavItemNear.setSelected(true);
                        break;
                    case 1:
                        mNavItemHistory.setSelected(true);
                        break;
                    case 2:
                        mNavItemFavorites.setSelected(true);
                        break;
                }
                break;
            case FRAGMENT_BEACONS:
                mNavItemBeacon.setSelected(true);
                break;
            case FRAGMENT_DISCOUNTS:
                mNavItemDiscount.setSelected(true);
                break;
            case FRAGMENT_TRANSLATOR:
                mNavItemTranslator.setSelected(true);
                break;
            case FRAGMENT_CARDS:
                mNavItemCards.setSelected(true);
                break;
            case FRAGMENT_ACCOUNT:
                mNavItemAccount.setSelected(true);
                break;
        }
    }
}
