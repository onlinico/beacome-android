package co.onlini.beacome.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.LinkedList;
import java.util.List;

import co.onlini.beacome.dal.database.DbConst;
import co.onlini.beacome.dal.database.DbManager;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.model.HistoryCardBase;
import co.onlini.beacome.model.HistoryCardExtended;
import co.onlini.beacome.model.Vcard;

public class HistoryHelper {

    private static HistoryHelper sInstance;
    private Context mContext;

    private HistoryHelper(Context context) {
        mContext = context;
    }

    public static HistoryHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HistoryHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public void insertOrUpdateHistoryRecord(String userUuid, String cardUuid, long discoveryDate, Boolean isFavorite, long version) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String were = String.format("%s=? AND %s=?",
                DbConst.HistoryTable.CARD_UUID_COLUMN, DbConst.HistoryTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid, userUuid};
        String[] columns = new String[]{DbConst.HistoryTable.CARD_UUID_COLUMN};
        Cursor cursor = database.query(DbConst.HistoryTable.TABLE_NAME, columns, were, whereArgs, null, null, null);
        ContentValues cv = new ContentValues();
        cv.put(DbConst.HistoryTable.IS_FAVORITE_COLUMN, isFavorite != null ? isFavorite : false);
        cv.put(DbConst.HistoryTable.VERSION_COLUMN, version);
        cv.put(DbConst.HistoryTable.LAST_DISCOVERY_DATE_COLUMN, discoveryDate);
        if (cursor.moveToNext()) {
            database.update(DbConst.HistoryTable.TABLE_NAME, cv, were, whereArgs);
        } else {
            cv.put(DbConst.HistoryTable.USER_UUID_COLUMN, userUuid);
            cv.put(DbConst.HistoryTable.CARD_UUID_COLUMN, cardUuid);
            database.insert(DbConst.HistoryTable.TABLE_NAME, null, cv);
        }
        cursor.close();
    }

    public void setHistoryRecordFavorite(String userUuid, String cardUUID, boolean isFavorite) {
        ContentValues cv = new ContentValues();
        cv.put(DbConst.HistoryTable.USER_UUID_COLUMN, userUuid);
        cv.put(DbConst.HistoryTable.CARD_UUID_COLUMN, cardUUID);
        cv.put(DbConst.HistoryTable.IS_FAVORITE_COLUMN, isFavorite);
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=? AND %s=?",
                DbConst.HistoryTable.USER_UUID_COLUMN, DbConst.HistoryTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{userUuid, cardUUID};
        database.update(DbConst.HistoryTable.TABLE_NAME, cv, where, whereArgs);
    }

    public HistoryCardExtended getHistoryCard(String userUuid, String cardUuid) {
        String where = String.format("%s=? AND %s=?", DbConst.HistoryCardView.CARD_UUID_COLUMN, DbConst.HistoryCardView.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid, userUuid};
        String[] columns = new String[]{
                DbConst.HistoryCardView.CARD_UUID_COLUMN,
                DbConst.HistoryCardView.TITLE_COLUMN,
                DbConst.HistoryCardView.DESCRIPTION_COLUMN,
                DbConst.HistoryCardView.DISCOVERY_DATE_COLUMN,
                DbConst.HistoryCardView.IS_FAVORITE_COLUMN,
                DbConst.HistoryCardView.IMAGE_URL_COLUMN,
                DbConst.HistoryCardView.CARD_VERSION_COLUMN
        };
        Cursor cursor = DbManager.getReadableDatabase(mContext).query(DbConst.HistoryCardView.VIEW_NAME, columns, where, whereArgs, null, null, null);
        HistoryCardExtended card = null;
        if (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.CARD_UUID_COLUMN));
            String title = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.TITLE_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.DESCRIPTION_COLUMN));
            long lastDiscoveryDate = cursor.getLong(cursor.getColumnIndex(DbConst.HistoryCardView.DISCOVERY_DATE_COLUMN));
            boolean isFavorite = cursor.getInt(cursor.getColumnIndex(DbConst.HistoryCardView.IS_FAVORITE_COLUMN)) > 0;
            String uri = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.IMAGE_URL_COLUMN));
            long cardVersion = cursor.getLong(cursor.getColumnIndex(DbConst.HistoryCardView.CARD_VERSION_COLUMN));
            Uri imageUri = uri != null ? Uri.parse(uri) : null;
            Vcard[] vcards = AttachmentHelper.getInstance(mContext).getVCardsByCard(uuid);
            Contact[] contacts = CardHelper.getInstance(mContext).getCardContacts(uuid);
            Attachment[] attachments = CardHelper.getInstance(mContext).getCardAttachments(cardUuid);
            card = new HistoryCardExtended(uuid, title, description, imageUri, lastDiscoveryDate, isFavorite, cardVersion, contacts, vcards, attachments);
        }
        cursor.close();
        return card;
    }

    public List<HistoryCardBase> getHistoryCards(String userUuid) {
        String where = String.format("%s=?", DbConst.HistoryCardView.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{userUuid};
        String[] columns = new String[]{
                DbConst.HistoryCardView.CARD_UUID_COLUMN,
                DbConst.HistoryCardView.TITLE_COLUMN,
                DbConst.HistoryCardView.DESCRIPTION_COLUMN,
                DbConst.HistoryCardView.DISCOVERY_DATE_COLUMN,
                DbConst.HistoryCardView.IS_FAVORITE_COLUMN,
                DbConst.HistoryCardView.IMAGE_URL_COLUMN,
                DbConst.HistoryCardView.IS_DELETED_COLUMN,
                DbConst.HistoryCardView.CARD_VERSION_COLUMN
        };
        Cursor cursor = DbManager.getReadableDatabase(mContext).query(DbConst.HistoryCardView.VIEW_NAME, columns, where, whereArgs, null, null, null);
        List<HistoryCardBase> records = new LinkedList<>();
        HistoryCardBase card;
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex(DbConst.HistoryCardView.IS_DELETED_COLUMN)) == 0) {
                String uuid = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.CARD_UUID_COLUMN));
                String title = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.TITLE_COLUMN));
                String description = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.DESCRIPTION_COLUMN));
                long lastDiscoveryDate = cursor.getLong(cursor.getColumnIndex(DbConst.HistoryCardView.DISCOVERY_DATE_COLUMN));
                long cardVersion = cursor.getLong(cursor.getColumnIndex(DbConst.HistoryCardView.CARD_VERSION_COLUMN));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndex(DbConst.HistoryCardView.IS_FAVORITE_COLUMN)) > 0;
                String uri = cursor.getString(cursor.getColumnIndex(DbConst.HistoryCardView.IMAGE_URL_COLUMN));
                Uri imageUri = uri != null ? Uri.parse(uri) : null;
                card = new HistoryCardBase(uuid, title, description, imageUri, lastDiscoveryDate, isFavorite, cardVersion);
                records.add(card);
            }
        }
        cursor.close();
        return records;
    }

    public long getVersion(String userUuid) {
        long version = 0;
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String were = String.format("%s=?", DbConst.HistoryTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{userUuid};
        Cursor cursor = database.query(DbConst.HistoryTable.TABLE_NAME, new String[]{DbConst.HistoryTable.VERSION_COLUMN}, were, whereArgs, null, null, null, "1");
        if (cursor.moveToNext()) {
            version = cursor.getLong(cursor.getColumnIndex(DbConst.HistoryTable.VERSION_COLUMN));
        }
        cursor.close();
        return version;
    }
}
