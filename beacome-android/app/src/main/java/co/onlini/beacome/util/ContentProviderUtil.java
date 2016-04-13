package co.onlini.beacome.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;

import co.onlini.beacome.model.Vcard;
import ezvcard.Ezvcard;
import ezvcard.VCard;

public class ContentProviderUtil {

    public static Vcard convertContactToVcard(Context context, Uri contactUri, String vcardUuid) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(contactUri, null, null, null, null);
        Vcard vcard = null;
        VCard ezVcard = null;
        Uri vcardFileUri = null;
        Uri imageUri = null;
        if (c != null) {
            if (c.moveToNext()) {
                String photoUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
                if (photoUri != null) {
                    imageUri = Uri.parse(photoUri);
                }
                String look = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                if (look != null) {
                    vcardFileUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, look);
                    try {
                        AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(vcardFileUri, "r");
                        FileInputStream fis = fd.createInputStream();
                        ezVcard = Ezvcard.parse(fis).first();
                    } catch (Exception e) {
                        Log.e("ContactToVcard", "Unable to get vcard file");
                    }
                }
            }
            c.close();
        }
        if (ezVcard != null) {
            String phone = ezVcard.getTelephoneNumbers().size() > 0 ? ezVcard.getTelephoneNumbers().get(0).getText() : null;
            String email = ezVcard.getEmails().size() > 0 ? ezVcard.getEmails().get(0).getValue() : null;
            String name = ezVcard.getFormattedName() != null ? ezVcard.getFormattedName().getValue() : null;
            vcard = new Vcard(vcardUuid, name, email, phone, vcardFileUri, imageUri, 0);
        }
        return vcard;
    }

    public static boolean checkFileSize(Context context, Uri fileUri, int sizeLimit) {
        long size = getSize(context, fileUri);
        return size > 0 && size < sizeLimit;
    }

    public static long getSize(Context context, Uri uri) {
        long length = -1;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    length = cursor.getLong(cursor.getColumnIndex("_size"));
                }
            } catch (Exception ignore) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    length = file.length();
                }
            }
        }
        return length;
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mimeType = cursor.getString(cursor.getColumnIndex("mime_type"));
                }
            } catch (Exception ignore) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    mimeType = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
                }
            }
        }
        return mimeType;
    }
}
