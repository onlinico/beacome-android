package co.onlini.beacome.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.dal.database.DbConst;
import co.onlini.beacome.dal.database.DbManager;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.BaseCard;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardExtended;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.CardUser;
import co.onlini.beacome.model.DiscountItem;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.web.Conventions;
import co.onlini.beacome.web.model.BeaconCardModel;
import co.onlini.beacome.web.model.ContactModel;
import co.onlini.beacome.web.model.InviteModel;
import co.onlini.beacome.web.model.UserLinkModel;

public class CardHelper {

    private static final String TAG = CardHelper.class.getSimpleName();
    private static CardHelper sInstance;
    private Context mContext;

    private CardHelper(Context context) {
        mContext = context;
    }

    public static CardHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CardHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public void insertCard(String cardUuid, String title, String description, long version, Uri imageUri, boolean isDeleted) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        ContentValues cv = new ContentValues();
        cv.put(DbConst.CardsTable.TITLE_COLUMN, title);
        cv.put(DbConst.CardsTable.DESCRIPTION_COLUMN, description);
        cv.put(DbConst.CardsTable.VERSION_COLUMN, version);
        cv.put(DbConst.CardsTable.IMAGE_URL_COLUMN, imageUri.toString());
        cv.put(DbConst.CardsTable.CARD_UUID_COLUMN, cardUuid);
        cv.put(DbConst.CardsTable.IS_DELETED_COLUMN, isDeleted);
        database.insert(DbConst.CardsTable.TABLE_NAME, null, cv);
    }

    public void updateCard(String cardUuid, String title, String description, long version, Uri imageUri, boolean isDeleted) {
        ContentValues cv = new ContentValues();
        cv.put(DbConst.CardsTable.TITLE_COLUMN, title);
        cv.put(DbConst.CardsTable.DESCRIPTION_COLUMN, description);
        cv.put(DbConst.CardsTable.VERSION_COLUMN, version);
        cv.put(DbConst.CardsTable.IMAGE_URL_COLUMN, imageUri.toString());
        cv.put(DbConst.CardsTable.IS_DELETED_COLUMN, isDeleted);

        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.CardsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        database.update(DbConst.CardsTable.TABLE_NAME, cv, where, whereArgs);
    }

    public boolean isCardExists(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.CardsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.query(DbConst.CardsTable.TABLE_NAME, new String[]{DbConst.CardsTable.VERSION_COLUMN}, where, whereArgs, null, null, null);
        boolean isExists = cursor.getCount() > 0;
        cursor.close();
        return isExists;
    }

    public boolean isBeaconExists(String beaconUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.CardBeaconsTable.BEACON_UUID_COLUMN);
        String[] whereArgs = new String[]{beaconUuid};
        Cursor cursor = database.query(DbConst.CardBeaconsTable.TABLE_NAME, new String[]{DbConst.CardBeaconsTable.CARD_UUID_COLUMN}, where, whereArgs, null, null, null, "1");
        boolean isExists = cursor.getCount() > 0;
        cursor.close();
        return isExists;
    }

    public Beacon[] getBeaconsByUser(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "cb." + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + " " +
                "FROM " + DbConst.CardBeaconsTable.TABLE_NAME + " cb " +
                "JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                "ON cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + "=" +
                "uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN + " " +

                "JOIN " + DbConst.CardsTable.TABLE_NAME + " c " +
                "ON cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + "=" +
                "c." + DbConst.CardsTable.CARD_UUID_COLUMN + " " +

                "WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " =? " +
                "AND c." + DbConst.CardsTable.IS_DELETED_COLUMN + " = 0 " +
                "GROUP BY cb." + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + ";";
        String[] whereArgs = new String[]{userUuid};
        Cursor cursor = database.rawQuery(sql_query, whereArgs);
        Beacon[] beacons = new Beacon[cursor.getCount()];
        while (cursor.moveToNext()) {
            String beaconUuid = cursor.getString(cursor.getColumnIndex(DbConst.CardBeaconsTable.BEACON_UUID_COLUMN));
            CardLink[] cardLinks = getCardLinksByBeacon(beaconUuid, userUuid);
            Beacon link = new Beacon(beaconUuid, cardLinks);
            beacons[cursor.getPosition()] = link;
        }
        cursor.close();
        return beacons;
    }

    public CardLink[] getCardLinksByBeacon(String beaconUuid, String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + ", " +
                "cb." + DbConst.CardBeaconsTable.STATE_COLUMN + " " +
                "FROM " + DbConst.CardBeaconsTable.TABLE_NAME + " cb " +
                "JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                "ON cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + "=" +
                "uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN + " " +

                "JOIN " + DbConst.CardsTable.TABLE_NAME + " c " +
                "ON cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + "=" +
                "c." + DbConst.CardsTable.CARD_UUID_COLUMN + " " +

                "WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " =? " +
                "AND cb." + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + " =? " +
                "AND c." + DbConst.CardsTable.IS_DELETED_COLUMN + " = 0 " +
                "GROUP BY cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + ";";
        String[] whereArgs = new String[]{userUuid, beaconUuid};
        Cursor cursor = database.rawQuery(sql_query, whereArgs);
        CardLink[] cardLinks = new CardLink[cursor.getCount()];
        while (cursor.moveToNext()) {
            String cardUuid = cursor.getString(cursor.getColumnIndex(DbConst.CardBeaconsTable.CARD_UUID_COLUMN));
            boolean isActive = cursor.getInt(cursor.getColumnIndex(DbConst.CardBeaconsTable.STATE_COLUMN)) > 0;
            cardLinks[cursor.getPosition()] = new CardLink(cardUuid, isActive);
        }
        cursor.close();
        return cardLinks;
    }

    public co.onlini.beacome.model.Contact[] getCardContacts(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.ContactsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.query(DbConst.ContactsTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        co.onlini.beacome.model.Contact[] contacts = new co.onlini.beacome.model.Contact[cursor.getCount()];
        co.onlini.beacome.model.Contact contact;
        while (cursor.moveToNext()) {
            String value = cursor.getString(cursor.getColumnIndex(DbConst.ContactsTable.VALUE_COLUMN));
            int type = cursor.getInt(cursor.getColumnIndex(DbConst.ContactsTable.TYPE_COLUMN));
            String contactId = cursor.getString(cursor.getColumnIndex(DbConst.ContactsTable.CONTACT_UUID_COLUMN));
            contact = new co.onlini.beacome.model.Contact(contactId, type, value);
            contacts[cursor.getPosition()] = contact;
        }
        cursor.close();
        return contacts;
    }

    public co.onlini.beacome.model.Attachment[] getCardAttachments(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=? AND %s=0", DbConst.AttachmentTable.CARD_UUID_COLUMN, DbConst.AttachmentTable.IS_DELETED_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.query(DbConst.AttachmentTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        co.onlini.beacome.model.Attachment[] attachments = new co.onlini.beacome.model.Attachment[cursor.getCount()];
        co.onlini.beacome.model.Attachment attachment;
        while (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN));
            int type = cursor.getInt(cursor.getColumnIndex(DbConst.AttachmentTable.TYPE_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.DESCRIPTION_COLUMN));
            String mimeType = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.MIME_TYPE_COLUMN));
            String uriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.URI_COLUMN));
            Uri uri = null;
            if (uriStr != null) {
                uri = Uri.parse(uriStr);
            }
            String fileUriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN));
            Uri fileUri = null;
            if (fileUriStr != null) {
                fileUri = Uri.parse(fileUriStr);
            }
            attachment = new co.onlini.beacome.model.Attachment(uuid, type, mimeType, description, fileUri, uri);
            attachments[cursor.getPosition()] = attachment;
        }
        cursor.close();
        return attachments;
    }

    public CardExtended[] getCardsByBeacon(String userUuid, String beaconUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "c." + DbConst.CardsTable.CARD_UUID_COLUMN + ", " +
                "c." + DbConst.CardsTable.TITLE_COLUMN + ", " +
                "c." + DbConst.CardsTable.DESCRIPTION_COLUMN + ", " +
                "c." + DbConst.CardsTable.IMAGE_URL_COLUMN + ", " +
                "c." + DbConst.CardsTable.VERSION_COLUMN + ", " +
                "cb." + DbConst.CardBeaconsTable.STATE_COLUMN + ", " +
                "uc." + DbConst.UserCardsTable.IS_OWNER_COLUMN +
                " FROM " + DbConst.CardsTable.TABLE_NAME + " c " +

                " JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                " ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                " uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN +

                " JOIN " + DbConst.CardBeaconsTable.TABLE_NAME + " cb " +
                " ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                " cb." + DbConst.CardBeaconsTable.CARD_UUID_COLUMN +

                " WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " =? " +
                " AND cb." + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + "= ?" +
                " AND c." + DbConst.CardsTable.IS_DELETED_COLUMN + " =0" +
                " GROUP BY c." + DbConst.CardsTable.CARD_UUID_COLUMN + ";";
        Cursor cursor = database.rawQuery(sql_query, new String[]{userUuid, beaconUuid});
        CardExtended[] cards = new CardExtended[cursor.getCount()];
        while (cursor.moveToNext()) {
            String cardUuid = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.CARD_UUID_COLUMN));
            String title = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.TITLE_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.DESCRIPTION_COLUMN));
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.CardsTable.VERSION_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            Vcard[] vcards = AttachmentHelper.getInstance(mContext).getVCardsByCard(cardUuid);
            co.onlini.beacome.model.Contact[] contacts = getCardContacts(cardUuid);
            CardUser[] users = getCardUsers(cardUuid);
            Beacon[] beaconsUuids = getBeaconsByCard(cardUuid, userUuid);
            Attachment[] attachments = getCardAttachments(cardUuid);
            cards[cursor.getPosition()] = new CardExtended(cardUuid, title, description, version, imageUri, contacts, vcards, attachments, users, beaconsUuids);
        }
        cursor.close();
        return cards;
    }


    public CardExtended getCard(String cardUuid, String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "c." + DbConst.CardsTable.CARD_UUID_COLUMN + ", " +
                "c." + DbConst.CardsTable.TITLE_COLUMN + ", " +
                "c." + DbConst.CardsTable.DESCRIPTION_COLUMN + ", " +
                "c." + DbConst.CardsTable.IMAGE_URL_COLUMN + ", " +
                "c." + DbConst.CardsTable.VERSION_COLUMN + ", " +
                "uc." + DbConst.UserCardsTable.IS_OWNER_COLUMN +
                " FROM " + DbConst.CardsTable.TABLE_NAME + " c " +
                " JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                " ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                " uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN +
                " WHERE c." + DbConst.CardsTable.CARD_UUID_COLUMN +
                " =?;";
        Cursor cursor = database.rawQuery(sql_query, new String[]{cardUuid});
        CardExtended card = null;
        if (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.TITLE_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.DESCRIPTION_COLUMN));
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.CardsTable.VERSION_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            Vcard[] vcards = AttachmentHelper.getInstance(mContext).getVCardsByCard(cardUuid);
            co.onlini.beacome.model.Contact[] contacts = getCardContacts(cardUuid);
            CardUser[] users = getCardUsers(cardUuid);
            Beacon[] beaconsUuids = getBeaconsByCard(cardUuid, userUuid);
            Attachment[] attachments = getCardAttachments(cardUuid);
            card = new CardExtended(cardUuid, title, description, version, imageUri, contacts, vcards, attachments, users, beaconsUuids);
        }
        cursor.close();
        return card;
    }

    public CardUser[] getCardUsers(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "u." + DbConst.UsersTable.USER_UUID_COLUMN + ", " +
                "u." + DbConst.UsersTable.FULL_NAME_COLUMN + ", " +
                "u." + DbConst.UsersTable.EMAIL_COLUMN + ", " +
                "u." + DbConst.UsersTable.IMAGE_URL_COLUMN + ", " +
                "u." + DbConst.UsersTable.VERSION_COLUMN + ", " +
                "uc." + DbConst.UserCardsTable.IS_OWNER_COLUMN +
                " FROM " + DbConst.UsersTable.TABLE_NAME + " u " +
                " JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                " ON u." + DbConst.UsersTable.USER_UUID_COLUMN + "=" +
                " uc." + DbConst.UserCardsTable.USER_UUID_COLUMN +
                " WHERE uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN +
                " =?;";
        Cursor cursorUsers = database.rawQuery(sql_query, new String[]{cardUuid});
        sql_query = " SELECT " +
                "s." + DbConst.ShareTable.SHARE_UUID_COLUMN + ", " +
                "s." + DbConst.ShareTable.EMAIL_COLUMN + ", " +
                "s." + DbConst.ShareTable.IS_OWNER_COLUMN +
                " FROM " + DbConst.ShareTable.TABLE_NAME + " s " +
                " WHERE s." + DbConst.ShareTable.CARD_UUID_COLUMN +
                " =?;";
        Cursor cursorShares = database.rawQuery(sql_query, new String[]{cardUuid});
        CardUser[] users = new CardUser[cursorUsers.getCount() + cursorShares.getCount()];
        String userUuid;
        String userName;
        String userEmail;
        long version;
        boolean isOwner;
        String shareUuid;
        while (cursorUsers.moveToNext()) {
            userUuid = cursorUsers.getString(cursorUsers.getColumnIndex(DbConst.UsersTable.USER_UUID_COLUMN));
            userName = cursorUsers.getString(cursorUsers.getColumnIndex(DbConst.UsersTable.FULL_NAME_COLUMN));
            userEmail = cursorUsers.getString(cursorUsers.getColumnIndex(DbConst.UsersTable.EMAIL_COLUMN));
            version = cursorUsers.getLong(cursorUsers.getColumnIndex(DbConst.UsersTable.VERSION_COLUMN));
            String img = cursorUsers.getString(cursorUsers.getColumnIndex(DbConst.UsersTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            isOwner = cursorUsers.getInt(cursorUsers.getColumnIndex(DbConst.UserCardsTable.IS_OWNER_COLUMN)) > 0;
            shareUuid = Conventions.USER_EMPTY_SHARE_UUID;
            users[cursorUsers.getPosition()] = new CardUser(userUuid, shareUuid, userName, userEmail, imageUri, isOwner, version);
        }
        int userCount = cursorUsers.getCount();
        cursorUsers.close();

        while (cursorShares.moveToNext()) {
            userEmail = cursorShares.getString(cursorShares.getColumnIndex(DbConst.ShareTable.EMAIL_COLUMN));
            isOwner = cursorShares.getInt(cursorShares.getColumnIndex(DbConst.ShareTable.IS_OWNER_COLUMN)) > 0;
            shareUuid = cursorShares.getString(cursorShares.getColumnIndex(DbConst.ShareTable.SHARE_UUID_COLUMN));
            users[userCount + cursorShares.getPosition()] = new CardUser(null, shareUuid, null, userEmail, null, isOwner, 0);
        }
        cursorShares.close();
        return users;
    }

    public List<BaseCard> getBaseCardsByUser(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "c." + DbConst.CardsTable.CARD_UUID_COLUMN + ", " +
                "c." + DbConst.CardsTable.TITLE_COLUMN + ", " +
                "c." + DbConst.CardsTable.DESCRIPTION_COLUMN + ", " +
                "c." + DbConst.CardsTable.IMAGE_URL_COLUMN + ", " +
                "c." + DbConst.CardsTable.VERSION_COLUMN + " " +

                " FROM " + DbConst.CardsTable.TABLE_NAME + " c " +
                " JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                " ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                " uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN +

                " WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " =?" +
                " AND c." + DbConst.CardsTable.IS_DELETED_COLUMN + " =0" +
                " GROUP BY c." + DbConst.CardsTable.CARD_UUID_COLUMN + ";";

        Cursor cursor = database.rawQuery(sql_query, new String[]{userUuid});
        List<BaseCard> cards = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String cardUuid = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.CARD_UUID_COLUMN));
            String title = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.TITLE_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.DESCRIPTION_COLUMN));
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.CardsTable.VERSION_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            cards.add(new BaseCard(cardUuid, title, description, version, imageUri));
        }
        cursor.close();
        return cards;
    }

    public int getBeaconsCountLinkedToCard(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT COUNT(" +
                DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + ")" +
                " FROM " + DbConst.CardBeaconsTable.TABLE_NAME +
                " WHERE " + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + " =?;";

        Cursor cursor = database.rawQuery(sql_query, new String[]{cardUuid});
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean isUserOwnerOfCard(String cardUuid, String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=? AND %s=?", DbConst.UserCardsTable.CARD_UUID_COLUMN, DbConst.UserCardsTable.USER_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid, userUuid};
        Cursor cursor = database.query(DbConst.UserCardsTable.TABLE_NAME, new String[]{DbConst.UserCardsTable.IS_OWNER_COLUMN}, where, whereArgs, null, null, null);
        boolean isOwner = false;
        if (cursor.moveToNext()) {
            isOwner = cursor.getInt(cursor.getColumnIndex(DbConst.UserCardsTable.IS_OWNER_COLUMN)) > 0;
        }
        cursor.close();
        return isOwner;
    }

    public long getCardVersion(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.CardsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.query(DbConst.CardsTable.TABLE_NAME, new String[]{DbConst.CardsTable.VERSION_COLUMN}, where, whereArgs, null, null, null);
        long lastDiscoveryDate = 0;
        if (cursor.moveToNext()) {
            lastDiscoveryDate = cursor.getLong(cursor.getColumnIndex(DbConst.CardsTable.VERSION_COLUMN));
        }
        cursor.close();
        return lastDiscoveryDate;
    }

    public long getVersion(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "MAX(c." + DbConst.CardsTable.VERSION_COLUMN + ") " +
                "FROM " + DbConst.CardsTable.TABLE_NAME + " c " +

                "JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
                "ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                "uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN + " " +

                "WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " = ?;";
        String[] whereArgs = new String[]{userUuid};
        Cursor cursor = database.rawQuery(sql_query, whereArgs);
        long version = 0;
        if (cursor.moveToNext()) {
            version = cursor.getLong(0);
        }
        cursor.close();
        return version;
    }

    public void setCardUsers(String cardUuid, UserLinkModel[] users) {
        SQLiteDatabase db = DbManager.getWritableDatabase(mContext);

        String where = String.format("%s=?", DbConst.UserCardsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        db.delete(DbConst.UserCardsTable.TABLE_NAME, where, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DbConst.UserCardsTable.CARD_UUID_COLUMN, cardUuid);
        for (UserLinkModel userLink : users) {
            cv.put(DbConst.UserCardsTable.USER_UUID_COLUMN, userLink.getUserUuid());
            cv.put(DbConst.UserCardsTable.IS_OWNER_COLUMN, userLink.isOwner());
            db.insert(DbConst.UserCardsTable.TABLE_NAME, null, cv);
        }
    }

    public void setCardInvites(String cardUuid, InviteModel[] invites) {
        SQLiteDatabase db = DbManager.getWritableDatabase(mContext);

        String where = String.format("%s=?", DbConst.ShareTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        db.delete(DbConst.ShareTable.TABLE_NAME, where, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DbConst.ShareTable.CARD_UUID_COLUMN, cardUuid);
        for (InviteModel invite : invites) {
            cv.put(DbConst.ShareTable.EMAIL_COLUMN, invite.getEmail());
            cv.put(DbConst.ShareTable.IS_OWNER_COLUMN, invite.isOwner());
            cv.put(DbConst.ShareTable.SHARE_UUID_COLUMN, invite.getUuid());
            db.insert(DbConst.ShareTable.TABLE_NAME, null, cv);
        }
    }

    public void setCardContacts(String cardUuid, ContactModel[] contacts) {
        SQLiteDatabase db = DbManager.getWritableDatabase(mContext);

        String where = String.format("%s=?", DbConst.ContactsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        db.delete(DbConst.ContactsTable.TABLE_NAME, where, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DbConst.ContactsTable.CARD_UUID_COLUMN, cardUuid);
        for (ContactModel contact : contacts) {
            cv.put(DbConst.ContactsTable.CONTACT_UUID_COLUMN, contact.getUuid());
            cv.put(DbConst.ContactsTable.TYPE_COLUMN, contact.getType());
            cv.put(DbConst.ContactsTable.VALUE_COLUMN, contact.getData());
            db.insert(DbConst.ContactsTable.TABLE_NAME, null, cv);
        }
    }

    public void setCardLinksToBeacon(String beaconUuid, List<CardLink> cardLinkList) {
        SQLiteDatabase db = DbManager.getWritableDatabase(mContext);

        String where = String.format("%s=?", DbConst.CardBeaconsTable.BEACON_UUID_COLUMN);
        String[] whereArgs = new String[]{beaconUuid};
        db.delete(DbConst.CardBeaconsTable.TABLE_NAME, where, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DbConst.CardBeaconsTable.BEACON_UUID_COLUMN, beaconUuid);
        for (CardLink cardLink : cardLinkList) {
            cv.put(DbConst.CardBeaconsTable.CARD_UUID_COLUMN, cardLink.getCardUuid());
            cv.put(DbConst.CardBeaconsTable.STATE_COLUMN, cardLink.isActive());
            db.insert(DbConst.CardBeaconsTable.TABLE_NAME, null, cv);
        }
    }

    public Beacon[] getBeaconsByCard(String cardUuid, String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + " " +
                "FROM " + DbConst.CardBeaconsTable.TABLE_NAME + "  " +
                "WHERE " + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + " =? " +
                "GROUP BY " + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + ";";
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.rawQuery(sql_query, whereArgs);
        Beacon[] beacons = new Beacon[cursor.getCount()];
        while (cursor.moveToNext()) {
            String beaconUuid = cursor.getString(cursor.getColumnIndex(DbConst.CardBeaconsTable.BEACON_UUID_COLUMN));
            CardLink[] cardLinks = getCardLinksByBeacon(beaconUuid, userUuid);
            beacons[cursor.getPosition()] = new Beacon(beaconUuid, cardLinks);
        }
        cursor.close();
        return beacons;
    }

    public void setBeacons(String cardUuid, BeaconCardModel[] beacons) {
        SQLiteDatabase db = DbManager.getWritableDatabase(mContext);

        String where = String.format("%s=?", DbConst.CardBeaconsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        db.delete(DbConst.CardBeaconsTable.TABLE_NAME, where, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DbConst.CardBeaconsTable.CARD_UUID_COLUMN, cardUuid);
        for (BeaconCardModel beaconCardModel : beacons) {
            cv.put(DbConst.CardBeaconsTable.BEACON_UUID_COLUMN, beaconCardModel.getBeaconUuid());
            cv.put(DbConst.CardBeaconsTable.STATE_COLUMN, beaconCardModel.isActive() ? 1 : 0);
            db.insert(DbConst.CardBeaconsTable.TABLE_NAME, null, cv);
        }
    }

    public List<DiscountItem> getDiscounts(String userUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String sql_query = " SELECT " +
                "a." + DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN + ", " +
                "a." + DbConst.AttachmentTable.DESCRIPTION_COLUMN + ", " +
                "a." + DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN + ", " +
                "a." + DbConst.AttachmentTable.URI_COLUMN + ", " +
                "a." + DbConst.AttachmentTable.VERSION_COLUMN + ", " +
                "a." + DbConst.AttachmentTable.MIME_TYPE_COLUMN + ", " +
                "c." + DbConst.CardsTable.TITLE_COLUMN + ", " +
                "c." + DbConst.CardsTable.IMAGE_URL_COLUMN + " " +

                " FROM " + DbConst.AttachmentTable.TABLE_NAME + " a " +

                " JOIN " + DbConst.HistoryTable.TABLE_NAME + " h " +
                " ON a." + DbConst.AttachmentTable.CARD_UUID_COLUMN + "=" +
                " h." + DbConst.HistoryTable.CARD_UUID_COLUMN +

                " JOIN " + DbConst.CardsTable.TABLE_NAME + " c " +
                " ON c." + DbConst.CardsTable.CARD_UUID_COLUMN + "=" +
                " a." + DbConst.AttachmentTable.CARD_UUID_COLUMN +

                " WHERE h." + DbConst.HistoryTable.USER_UUID_COLUMN + " =?" +
                " AND a." + DbConst.AttachmentTable.IS_DELETED_COLUMN + " =0" +
                " AND a." + DbConst.AttachmentTable.TYPE_COLUMN + " = ?" +
                " AND c." + DbConst.CardsTable.IS_DELETED_COLUMN + " =0" +
                " GROUP BY a." + DbConst.AttachmentTable.CARD_UUID_COLUMN + ";";

        Cursor cursor = database.rawQuery(sql_query, new String[]{userUuid, String.valueOf(Conventions.ATTACHMENT_TYPE_DISCOUNT)});
        List<DiscountItem> discounts = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.DESCRIPTION_COLUMN));
            String cardTitle = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.TITLE_COLUMN));
            String mimeType = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.MIME_TYPE_COLUMN));
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.AttachmentTable.VERSION_COLUMN));
            String img = cursor.getString(cursor.getColumnIndex(DbConst.CardsTable.IMAGE_URL_COLUMN));
            Uri imageUri = null;
            if (img != null) {
                imageUri = Uri.parse(img);
            }
            String fileStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN));
            Uri fileUri = null;
            if (fileStr != null) {
                fileUri = Uri.parse(fileStr);
            }
            String uriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.URI_COLUMN));
            Uri uri = null;
            if (uriStr != null) {
                uri = Uri.parse(uriStr);
            }
            discounts.add(new DiscountItem(uuid, description, cardTitle, mimeType, imageUri, fileUri, uri, version));
        }
        cursor.close();
        return discounts;
    }
}
