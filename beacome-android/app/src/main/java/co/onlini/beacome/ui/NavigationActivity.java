package co.onlini.beacome.ui;


import android.os.Bundle;

public interface NavigationActivity {
    void showScreen(String newFragmentTag, Bundle args);

    void onStartFragment(String fragmentTag, int fragmentTab);
}
