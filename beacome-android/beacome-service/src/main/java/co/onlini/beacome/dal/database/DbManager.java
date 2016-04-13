package co.onlini.beacome.dal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {

    private static DbHelper sHelper;

    private synchronized static DbHelper getHelper(Context context) {
        if (sHelper == null) {
            sHelper = new DbHelper(context);
        }
        return sHelper;
    }

    public static SQLiteDatabase getWritableDatabase(Context context) {
        return getHelper(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDatabase(Context context) {
        return getHelper(context).getReadableDatabase();
    }


}
