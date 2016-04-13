package co.onlini.beacome.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.Map;

import co.onlini.beacome.dal.database.DbConst;
import co.onlini.beacome.dal.database.DbManager;
import co.onlini.beacome.model.User;
import co.onlini.beacome.model.UserWithLinks;
import co.onlini.beacome.web.Conventions;

public class UserHelper {

    private static UserHelper sInstance;
    private Context mContext;

    private UserHelper(Context context) {
        mContext = context;
    }

    public static UserHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UserHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public User getUser(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{userUuid};
        String[] columns = new String[]{
                DbConst.UsersTable.FULL_NAME_COLUMN,
                DbConst.UsersTable.EMAIL_COLUMN,
                DbConst.UsersTable.IMAGE_URL_COLUMN,
                DbConst.UsersTable.VERSION_COLUMN
        };
        Cursor cursor = database.query(DbConst.UsersTable.TABLE_NAME, columns, where, whereArgs, null, null, null);
        User user = null;
        if (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.FULL_NAME_COLUMN));
            String email = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.EMAIL_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.UsersTable.VERSION_COLUMN));
            String sharedUuid = Conventions.USER_EMPTY_SHARE_UUID;
            user = new User(userUuid, sharedUuid, name, email, imageUri, version);
        }
        cursor.close();
        return user;
    }

    public void saveUser(String userUuid, String userName, String email, long version, Uri imageUrl) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        if (getUser(userUuid) == null) {
            ContentValues cv = new ContentValues();
            cv.put(DbConst.UsersTable.USER_UUID_COLUMN, userUuid);
            cv.put(DbConst.UsersTable.FULL_NAME_COLUMN, userName);
            cv.put(DbConst.UsersTable.EMAIL_COLUMN, email);
            cv.put(DbConst.UsersTable.VERSION_COLUMN, version);
            cv.put(DbConst.UsersTable.IMAGE_URL_COLUMN, imageUrl.toString());
            database.insert(DbConst.UsersTable.TABLE_NAME, null, cv);
        } else {
            String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
            String[] whereArgs = new String[]{String.valueOf(userUuid)};
            ContentValues cv = new ContentValues();
            cv.put(DbConst.UsersTable.FULL_NAME_COLUMN, userName);
            cv.put(DbConst.UsersTable.EMAIL_COLUMN, email);
            cv.put(DbConst.UsersTable.VERSION_COLUMN, version);
            cv.put(DbConst.UsersTable.IMAGE_URL_COLUMN, imageUrl.toString());
            database.update(DbConst.UsersTable.TABLE_NAME, cv, where, whereArgs);
        }
    }

    public UserWithLinks getRegisteredUser(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{String.valueOf(userUuid)};
        Cursor cursor = database.query(DbConst.UsersTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        UserWithLinks user = null;
        if (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.FULL_NAME_COLUMN));
            String email = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.EMAIL_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.UsersTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            boolean isFacebookLinked = cursor.getInt(cursor.getColumnIndex(DbConst.UsersTable.HAS_FACEBOOK_LINK_COLUMN)) > 0;
            boolean isTwitterLinked = cursor.getInt(cursor.getColumnIndex(DbConst.UsersTable.HAS_TWITTER_LINK_COLUMN)) > 0;
            boolean isGpLinked = cursor.getInt(cursor.getColumnIndex(DbConst.UsersTable.HAS_GP_LINK_COLUMN)) > 0;
            String sharedUuid = Conventions.USER_EMPTY_SHARE_UUID;
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.UsersTable.VERSION_COLUMN));
            user = new UserWithLinks(userUuid, sharedUuid, name, email, imageUri, version, isFacebookLinked, isTwitterLinked, isGpLinked);
        }
        cursor.close();
        return user;
    }


    public void addSocialNetworkLink(String userUuid, Map<String, Boolean> socialLinks) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{userUuid};
        ContentValues cv = new ContentValues();
        if (socialLinks.containsKey(Conventions.AUTH_PROVIDER_FACEBOOK)) {
            cv.put(DbConst.UsersTable.HAS_FACEBOOK_LINK_COLUMN, socialLinks.get(Conventions.AUTH_PROVIDER_FACEBOOK));
        }
        if (socialLinks.containsKey(Conventions.AUTH_PROVIDER_TWITTER)) {
            cv.put(DbConst.UsersTable.HAS_TWITTER_LINK_COLUMN, socialLinks.get(Conventions.AUTH_PROVIDER_TWITTER));
        }
        if (socialLinks.containsKey(Conventions.AUTH_PROVIDER_GOOGLE)) {
            cv.put(DbConst.UsersTable.HAS_GP_LINK_COLUMN, socialLinks.get(Conventions.AUTH_PROVIDER_GOOGLE));
        }
        database.update(DbConst.UsersTable.TABLE_NAME, cv, where, whereArgs);
    }

    public void setUserImage(String uuid, Uri uri) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{uuid};
        ContentValues cv = new ContentValues();
        cv.put(DbConst.UsersTable.IMAGE_URL_COLUMN, uri.toString());
        database.update(DbConst.UsersTable.TABLE_NAME, cv, where, whereArgs);
    }

    public void setUserNameAndEmail(String uuid, String name, String email) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{uuid};
        ContentValues cv = new ContentValues();
        cv.put(DbConst.UsersTable.FULL_NAME_COLUMN, name);
        cv.put(DbConst.UsersTable.EMAIL_COLUMN, email);
        database.update(DbConst.UsersTable.TABLE_NAME, cv, where, whereArgs);
    }

    public long getVersion(String userUuid) {
        long version = -1;
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.UsersTable.VERSION_COLUMN);
        String[] whereArgs = new String[]{userUuid};
        String[] columns = new String[]{DbConst.UsersTable.USER_UUID_COLUMN};
        Cursor cursor = database.query(DbConst.UsersTable.TABLE_NAME, columns, where, whereArgs, null, null, null, "1");
        if (cursor.moveToNext()) {
            version = cursor.getLong(cursor.getColumnIndex(DbConst.UsersTable.VERSION_COLUMN));
        }
        cursor.close();
        return version;
    }

}
