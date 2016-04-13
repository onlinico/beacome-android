package co.onlini.beacome.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.onlini.beacome.model.Attachment;

public class FileCacheUtil {

    private static final String TAG = FileCacheUtil.class.getSimpleName();

    public static Uri write(Context context, byte[] data, String name) throws IOException {
        File cacheDir = context.getFilesDir();
        File file = new File(cacheDir, name);
        FileOutputStream os = new FileOutputStream(file);
        try {
            os.write(data);
        } finally {
            os.flush();
            os.close();
        }
        return Uri.fromFile(file);
    }

    public static void delete(Uri fileUri) {
        if ("file".equals(fileUri.getScheme())) {
            File file = new File(fileUri.getPath());
            if (file.exists()) {
                file.delete();
            }
        } else {
            Log.e(TAG, "Unable to delete file, invalid scheme");
        }
    }

    public static byte[] readFile(Context context, Uri fileName) {
        byte[] data = null;
        FileInputStream fis = null;
        if (fileName.getScheme().equals("content")) {
            try {
                AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(fileName, "r");
                if (fd != null) {
                    fis = fd.createInputStream();
                    data = new byte[(int) fd.getDeclaredLength()];
                }
            } catch (IOException e) {
                Log.e("VcardReader", "Unable to read vcard from contacts");
            }
        } else if (fileName.getScheme().equals("file")) {
            File file = new File(fileName.getPath());
            if (file.exists()) {
                int length = (int) file.length();
                data = new byte[length];
                try {
                    fis = new FileInputStream(file);
                } catch (IOException ignore) {
                }
            }
        } else {
            Log.e("VcardReader", "Unsupported uri scheme");
        }

        if (fis != null) {
            try {
                fis.read(data);
            } catch (IOException ignore) {
            } finally {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        return data;
    }

    @SuppressLint("SetWorldReadable")
    public static Uri saveAttachment(Attachment attachment, InputStream is) {
        boolean isOk = true;
        String extension = attachment.getMimeType() != null ? MimeTypeMap.getSingleton().getExtensionFromMimeType(attachment.getMimeType()) : "image/*";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir.mkdirs()) {
            Log.e(TAG, "Unable to create dirs");
        }
        File file = new File(dir, attachment.getUuid() + "." + extension);
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to save attachment");
            isOk = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //noinspection ResultOfMethodCallIgnored
        return isOk ? Uri.fromFile(file) : null;
    }

    public static byte[] getBytes(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (Exception ignore) {

        }
        return buffer.toByteArray();
    }
}
