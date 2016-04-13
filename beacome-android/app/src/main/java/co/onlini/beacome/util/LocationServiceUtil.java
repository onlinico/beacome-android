package co.onlini.beacome.util;


import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

public class LocationServiceUtil {
    public static boolean isLocationProviderNeed(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //Pre-M do not require location permission for bluetooth scanner
            return true;
        }
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("LocationServiceUtil", ex.getMessage());
        }
        return gps_enabled || network_enabled;
    }
}
