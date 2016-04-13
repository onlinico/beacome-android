package co.onlini.beacome;

import android.app.Application;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.Locale;

import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.util.FileUtil;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        applyLocale();
        FileUtil.removeTmpFilesAll(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private void applyLocale() {
        String localeName = SessionManager.getLanguage(getApplicationContext());
        Locale locale = new Locale(localeName);
        Locale.setDefault(locale);
        Configuration config = getApplicationContext().getResources().getConfiguration();
        config.locale = locale;
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        getApplicationContext().getResources().updateConfiguration(config, metrics);
    }
}
