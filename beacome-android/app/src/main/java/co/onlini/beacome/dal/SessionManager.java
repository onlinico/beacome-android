package co.onlini.beacome.dal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;
import java.util.UUID;

import co.onlini.beacome.model.Session;
import co.onlini.beacome.util.SortVariants;

public class SessionManager {
    private static final String PREFS_BEACON_UUID = "co.onlini.beacome.prefs_beacons";

    private static final String PREFS_SESSION = "co.onlini.beacome.prefs_session";
    private static final String PREFS_FIRST_START = "co.onlini.beacome.prefs_first_start";
    private static final String PREFS_SORT_ORDER = "co.onlini.beacome.prefs_sort_order";
    private static final String PREFS_LANGUAGE = "co.onlini.beacome.prefs_language";

    private static final String KEY_SESSION_USER_ID = "session_user_id";
    private static final String KEY_SESSION_AUTH_TOKEN = "session_auth_token";
    private static final String KEY_SESSION_AUTH_TOKEN_EXPIRES = "session_auth_token_expires";
    private static final String KEY_SESSION_BEACON_UUID = "session_beacon_uuid";
    private static final String KEY_SESSION_SCANNER_STATE = "session_scanner_state";
    private static final String KEY_SESSION_ADVERTISER_STATE = "session_advertiser_state";

    private static final String KEY_SESSION_SORT_ORDER_SCANNING = "sort_order_scanning";
    private static final String KEY_SESSION_SORT_ORDER_HISTORY = "sort_order_history";
    private static final String KEY_SESSION_SORT_ORDER_FAVORITE = "sort_order_favorite";
    private static final String KEY_SESSION_SORT_ORDER_MY_CARDS = "sort_order_my_cards";

    private static final String KEY_LANGUAGE = "language";

    private static final String KEY_IS_FIRST_START = "is_first_start";

    public static String ANONYMOUS_USER_ID = "00000000-0000-0000-0000-000000000000";
    private static Session mSession;


    public static boolean isSavedSessionValid(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        return preferences.getString(KEY_SESSION_USER_ID, null) != null;
    }

    public static Session getSession(Context context) {
        if (mSession == null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
            String userId = preferences.getString(KEY_SESSION_USER_ID, ANONYMOUS_USER_ID);
            String token = preferences.getString(KEY_SESSION_AUTH_TOKEN, null);
            String beaconUuid = preferences.getString(KEY_SESSION_BEACON_UUID, null);
            long expire = preferences.getLong(KEY_SESSION_AUTH_TOKEN_EXPIRES, 0);
            boolean isScannerRunning = preferences.getBoolean(KEY_SESSION_SCANNER_STATE, true);
            boolean isAdvertiserRunning = preferences.getBoolean(KEY_SESSION_ADVERTISER_STATE, false);
            mSession = new Session(userId, token, expire, beaconUuid, isScannerRunning, isAdvertiserRunning);
        }
        return mSession;
    }

    @SuppressLint("CommitPrefEdits")
    public static void startSession(Context context, Session session) {
        if (session == null) {
            throw new IllegalArgumentException();
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_SESSION_USER_ID, session.getUserUuid())
                .putString(KEY_SESSION_AUTH_TOKEN, session.getToken())
                .putString(KEY_SESSION_BEACON_UUID, session.getDeviseAsBeaconUuid())
                .putLong(KEY_SESSION_AUTH_TOKEN_EXPIRES, session.getExpireDate())
                .putBoolean(KEY_SESSION_SCANNER_STATE, session.isScannerRunning())
                .putBoolean(KEY_SESSION_ADVERTISER_STATE, session.isAdvertiserRunning())
                .commit();
        mSession = session;
    }

    @SuppressLint("CommitPrefEdits")
    public static void closeSession(Context context) {
        if (mSession != null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
            preferences.edit().clear().commit();
            mSession = null;
        }
    }

    public static void setScannerState(Context context, boolean isRunning) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SESSION_SCANNER_STATE, isRunning).apply();
    }

    public static void setAdvertiserState(Context context, boolean isRunning) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SESSION_ADVERTISER_STATE, isRunning).apply();
    }

    public static int getPageSortOrder(Context context, int page) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_SORT_ORDER, Context.MODE_PRIVATE);
        switch (page) {
            case Pages.SCANNING:
                return preferences.getInt(KEY_SESSION_SORT_ORDER_SCANNING, SortVariants.BY_NAME);
            case Pages.HISTORY:
                return preferences.getInt(KEY_SESSION_SORT_ORDER_HISTORY, SortVariants.BY_DATE);
            case Pages.FAVORITE:
                return preferences.getInt(KEY_SESSION_SORT_ORDER_FAVORITE, SortVariants.BY_DATE);
            case Pages.MY_CARDS:
                return preferences.getInt(KEY_SESSION_SORT_ORDER_MY_CARDS, SortVariants.BY_NAME);
            default:
                return SortVariants.BY_NAME;
        }
    }

    public static void setPageSortOrder(Context context, int page, int sortOrder) {
        SharedPreferences.Editor prefEdit = context.getSharedPreferences(PREFS_SORT_ORDER, Context.MODE_PRIVATE).edit();
        switch (page) {
            case Pages.SCANNING:
                prefEdit.putInt(KEY_SESSION_SORT_ORDER_SCANNING, sortOrder);
                break;
            case Pages.HISTORY:
                prefEdit.putInt(KEY_SESSION_SORT_ORDER_HISTORY, sortOrder);
                break;
            case Pages.FAVORITE:
                prefEdit.putInt(KEY_SESSION_SORT_ORDER_FAVORITE, sortOrder);
                break;
            case Pages.MY_CARDS:
                prefEdit.putInt(KEY_SESSION_SORT_ORDER_MY_CARDS, sortOrder);
                break;
        }
        prefEdit.apply();
    }

    public static boolean isFirstStart(Context context) {
        boolean isFirstStart = false;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_FIRST_START, Context.MODE_PRIVATE);
        if (preferences.getBoolean(KEY_IS_FIRST_START, true)) {
            isFirstStart = true;
            preferences.edit().putBoolean(KEY_IS_FIRST_START, false).apply();
        }
        return isFirstStart;
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_LANGUAGE, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, Locale.ENGLISH.getLanguage());
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_LANGUAGE, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static String getUserBeaconUuid(Context context, String userUuid) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_BEACON_UUID, Context.MODE_PRIVATE);
        String beaconUuid = preferences.getString(userUuid, null);
        if (beaconUuid == null) {
            beaconUuid = UUID.randomUUID().toString().toLowerCase();
        }
        preferences.edit().putString(userUuid, beaconUuid).apply();
        return beaconUuid;
    }

    public static class Pages {
        public static final int SCANNING = 0x1;
        public static final int HISTORY = 0x2;
        public static final int FAVORITE = 0x4;
        public static final int MY_CARDS = 0x8;
    }
}
