package co.onlini.beacome.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class BluetoothUtil {
    public static boolean isDeviseCanAdvertise(Context context) {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (context.getApplicationContext().getPackageManager().
                    hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                try {
                    return mAdapter.getBluetoothLeAdvertiser() != null;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isBluetoothTurnedOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
    }
}
