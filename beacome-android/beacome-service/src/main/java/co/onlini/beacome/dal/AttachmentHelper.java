package co.onlini.beacome.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import co.onlini.beacome.dal.database.DbConst;
import co.onlini.beacome.dal.database.DbManager;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.util.FileCacheUtil;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Photo;

public class AttachmentHelper {

    private static final String TAG = DbConst.VCardsTable.class.getSimpleName();
    private static AttachmentHelper sInstance;
    private Context mContext;

    private AttachmentHelper(Context context) {
        mContext = context;
    }

    public static AttachmentHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AttachmentHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Parses and saves a vcard to database, also saves raw vcard and vcard`s photo to filesystem as `.vcf and bitmap,
     * and store that files URIs to database.
     * If the vcard isDeleted is true, it marks it as deleted and removes related files, if they are existing.
     */
    public void saveVcard(String vcardUuid, String cardUuid, InputStream stream, long version, boolean isDeleted) {
        byte[] data = FileCacheUtil.getBytes(stream);
        if (data == null) {
            Log.e(TAG, "Unable to save vcard");
            return;
        }
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        ContentValues cv = new ContentValues();
        cv.put(DbConst.VCardsTable.VERSION_COLUMN, version);
        if (!isDeleted) {
            VCard parsed = Ezvcard.parse(new String(data)).first();
            String fullName = null;
            String phone = null;
            String email = null;
            byte[] image = null;
            String imageType = null;
            Uri imageUri = null;
            if (parsed != null) {
                fullName = parsed.getFormattedName() != null ? parsed.getFormattedName().getValue() : null;
                phone = parsed.getTelephoneNumbers().size() > 0 ? parsed.getTelephoneNumbers().get(0).getText() : null;
                email = parsed.getEmails().size() > 0 ? parsed.getEmails().get(0).getValue() : null;
                if (parsed.getPhotos().size() > 0) {
                    Photo photo = parsed.getPhotos().get(0);
                    image = photo.getData();
                    if (image != null) {
                        imageType = photo.getContentType().getExtension();
                    } else {
                        imageUri = photo.getUrl() != null ? Uri.parse(photo.getUrl()) : null;
                    }
                }
            }
            Uri vcfUri = null;
            try {
                vcfUri = FileCacheUtil.write(mContext, data, String.format("%s.vcf", vcardUuid));
                if (imageUri == null && image != null) {
                    imageUri = FileCacheUtil.write(mContext, image, String.format("%s.%s", vcardUuid, imageType));
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to save vcard to file");
            }
            cv.put(DbConst.VCardsTable.EMAIL_COLUMN, email);
            cv.put(DbConst.VCardsTable.NAME_COLUMN, fullName);
            cv.put(DbConst.VCardsTable.PHONE_COLUMN, phone);
            cv.put(DbConst.VCardsTable.CARD_UUID_COLUMN, cardUuid);
            cv.put(DbConst.VCardsTable.IMAGE_URI_COLUMN, imageUri != null ? imageUri.toString() : null);
            cv.put(DbConst.VCardsTable.VCF_URI_COLUMN, vcfUri != null ? vcfUri.toString() : null);
        }
        cv.put(DbConst.VCardsTable.IS_DELETED_COLUMN, isDeleted);

        String where = String.format("%s=?", DbConst.VCardsTable.VCARD_UUID_COLUMN);
        String[] whereArgs = new String[]{vcardUuid};
        String[] columns = new String[]{
                DbConst.VCardsTable.VCARD_UUID_COLUMN,
                DbConst.VCardsTable.VCF_URI_COLUMN,
                DbConst.VCardsTable.IMAGE_URI_COLUMN};

        Cursor cursor = database.query(DbConst.VCardsTable.TABLE_NAME, columns, where, whereArgs, null, null, null, "1");
        boolean isEntryExists = false;
        if (cursor.moveToNext()) {
            isEntryExists = true;
            String imageUriStr = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.IMAGE_URI_COLUMN));
            Uri imageUri = imageUriStr != null ? Uri.parse(imageUriStr) : null;
            String vcfUriStr = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.VCF_URI_COLUMN));
            Uri vcfUri = vcfUriStr != null ? Uri.parse(vcfUriStr) : null;
            if (isDeleted) {
                if (imageUri != null) {
                    FileCacheUtil.delete(imageUri);
                }
                if (vcfUri != null) {
                    FileCacheUtil.delete(vcfUri);
                }
            }
        }
        cursor.close();
        if (isEntryExists) {
            where = String.format("%s=?", DbConst.VCardsTable.VCARD_UUID_COLUMN);
            whereArgs = new String[]{vcardUuid};
            database.update(DbConst.VCardsTable.TABLE_NAME, cv, where, whereArgs);
        } else {
            cv.put(DbConst.VCardsTable.VCARD_UUID_COLUMN, vcardUuid);
            database.insert(DbConst.VCardsTable.TABLE_NAME, null, cv);
        }
    }

    public void saveAttachment(String attachmentUuid, int type, String mimeType, String cardUuid, String url, String description, long version, boolean isDeleted) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN);
        String[] whereArgs = new String[]{attachmentUuid};
        String[] columns = new String[]{DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN};
        Cursor cursor = database.query(DbConst.AttachmentTable.TABLE_NAME, columns, where, whereArgs, null, null, null, "1");
        boolean isExists = cursor.getCount() > 0;
        cursor.close();
        ContentValues cv = new ContentValues();
        cv.put(DbConst.AttachmentTable.DESCRIPTION_COLUMN, description);
        cv.put(DbConst.AttachmentTable.MIME_TYPE_COLUMN, mimeType);
        cv.put(DbConst.AttachmentTable.VERSION_COLUMN, version);
        cv.put(DbConst.AttachmentTable.IS_DELETED_COLUMN, isDeleted);
        cv.put(DbConst.AttachmentTable.URI_COLUMN, url);
        cv.put(DbConst.AttachmentTable.TYPE_COLUMN, type);
        if (isExists) {
            database.update(DbConst.AttachmentTable.TABLE_NAME, cv, where, whereArgs);
        } else {
            cv.put(DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN, attachmentUuid);
            cv.put(DbConst.AttachmentTable.CARD_UUID_COLUMN, cardUuid);
            database.insert(DbConst.AttachmentTable.TABLE_NAME, null, cv);
        }
    }

//    public Attachment[] getAttachmentsByCard(String cardUuid) {
//        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
//        String where = String.format("%s=0 AND %s=?", DbConst.AttachmentTable.IS_DELETED_COLUMN, DbConst.AttachmentTable.CARD_UUID_COLUMN);
//        String[] whereArgs = new String[]{cardUuid};
//        Cursor cursor = database.query(DbConst.AttachmentTable.TABLE_NAME, null, where, whereArgs, null, null, null);
//        Attachment[] attachments = new Attachment[cursor.getCount()];
//        while (cursor.moveToNext()) {
//            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN));
//            String description = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.DESCRIPTION_COLUMN));
//            int type = cursor.getInt(cursor.getColumnIndex(DbConst.AttachmentTable.TYPE_COLUMN));
//            String mimeType = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.MIME_TYPE_COLUMN));
//            String uriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.URI_COLUMN));
//            Uri uri = uriStr != null ? Uri.parse(uriStr) : null;
//
//            String fileUriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN));
//            Uri fileUri = fileUriStr != null ? Uri.parse(fileUriStr) : null;
//
//            Attachment attachment = new Attachment(uuid, type, mimeType, description, fileUri, uri);
//            attachments[cursor.getPosition()] = attachment;
//        }
//        cursor.close();
//        return attachments;
//    }

    public Vcard[] getVCardsByCard(String cardUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=0 AND %s=?", DbConst.VCardsTable.IS_DELETED_COLUMN, DbConst.VCardsTable.CARD_UUID_COLUMN);
        String[] whereArgs = new String[]{cardUuid};
        Cursor cursor = database.query(DbConst.VCardsTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        Vcard[] vcards = new Vcard[cursor.getCount()];
        Vcard vcard;
        while (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.VCARD_UUID_COLUMN));
            long version = cursor.getLong(cursor.getColumnIndex(DbConst.VCardsTable.VERSION_COLUMN));
            String fullName = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.NAME_COLUMN));
            String phone = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.PHONE_COLUMN));
            String email = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.EMAIL_COLUMN));
            String imageUriStr = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.IMAGE_URI_COLUMN));
            Uri imageUri = imageUriStr != null ? Uri.parse(imageUriStr) : null;
            String vcfUriStr = cursor.getString(cursor.getColumnIndex(DbConst.VCardsTable.VCF_URI_COLUMN));
            Uri vcfUri = vcfUriStr != null ? Uri.parse(vcfUriStr) : null;
            vcard = new Vcard(uuid, fullName, email, phone, vcfUri, imageUri, version);
            vcards[cursor.getPosition()] = vcard;
        }
        cursor.close();
        return vcards;
    }

//    public long getVersionOfVcardRelatedToUserCards(String userUuid) {
//        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
//        String sql_query = " SELECT " +
//                "MAX(vc." + DbConst.VCardsTable.VERSION_COLUMN + ") " +
//                "FROM " + DbConst.VCardsTable.TABLE_NAME + " vc " +
//                "JOIN " + DbConst.UserCardsTable.TABLE_NAME + " uc " +
//                "ON vc." + DbConst.VCardsTable.CARD_UUID_COLUMN + "=" +
//                "uc." + DbConst.UserCardsTable.CARD_UUID_COLUMN + " " +
//                "WHERE uc." + DbConst.UserCardsTable.USER_UUID_COLUMN + " = ?;";
//        String[] whereArgs = new String[]{userUuid};
//        Cursor cursor = database.rawQuery(sql_query, whereArgs);
//        long version = 0;
//        if (cursor.moveToNext()) {
//            version = cursor.getLong(0);
//        }
//        cursor.close();
//        return version;
//    }

    public byte[] getVcardVcfFileContent(@NonNull Uri fileUri) {
        return FileCacheUtil.readFile(mContext, fileUri);
    }

//    public long getVcardVersion(String vcardUuid) {
//        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
//        String where = String.format("%s=?", DbConst.VCardsTable.VCARD_UUID_COLUMN);
//        String[] whereArgs = new String[]{vcardUuid};
//        String[] columns = new String[]{DbConst.VCardsTable.VERSION_COLUMN};
//        Cursor cursor = database.query(DbConst.VCardsTable.TABLE_NAME, columns, where, whereArgs, null, null, null, "1");
//        long version = 0;
//        if (cursor.moveToNext()) {
//            version = cursor.getLong(0);
//        }
//        cursor.close();
//        return version;
//    }

    public void setAttachmentFileUri(String attachmentUuid, Uri uri) {
        SQLiteDatabase database = DbManager.getWritableDatabase(mContext);
        String where = String.format("%s=?", DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN);
        String[] whereArgs = new String[]{attachmentUuid};

        ContentValues cv = new ContentValues();
        cv.put(DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN, uri.toString());
        database.update(DbConst.AttachmentTable.TABLE_NAME, cv, where, whereArgs);
    }

    public Attachment getAttachment(String attachmentUuid) {
        SQLiteDatabase database = DbManager.getReadableDatabase(mContext);
        String where = String.format("%s=?", DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN);
        String[] whereArgs = new String[]{attachmentUuid};
        Cursor cursor = database.query(DbConst.AttachmentTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        Attachment attachment = null;
        while (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN));
            String description = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.DESCRIPTION_COLUMN));
            int type = cursor.getInt(cursor.getColumnIndex(DbConst.AttachmentTable.TYPE_COLUMN));
            String mimeType = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.MIME_TYPE_COLUMN));
            String uriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.URI_COLUMN));
            Uri uri = uriStr != null ? Uri.parse(uriStr) : null;
            String fileUriStr = cursor.getString(cursor.getColumnIndex(DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN));
            Uri fileUri = fileUriStr != null ? Uri.parse(fileUriStr) : null;
            attachment = new Attachment(uuid, type, mimeType, description, fileUri, uri);
        }
        cursor.close();
        return attachment;
    }
}
