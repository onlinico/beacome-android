package co.onlini.beacome.util;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import co.onlini.beacome.model.Attachment;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    public static void removeTmpFilesAll(Context context) {
        deleteRecursive(getTmpDir(context));
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        fileOrDirectory.delete();
    }

    private static File getTmpDir(Context context) {
        String path = context.getCacheDir().getPath() + "/tmp";
        File dir = new File(path);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        }
        return dir;
    }

    public static File getTempFile(Context context) {
        File tmpfile = null;
        String fileName = "tmp";
        try {
            tmpfile = File.createTempFile(fileName, null, getTmpDir(context));
        } catch (IOException e) {
            Log.e(TAG, "Unable to create tmp file");
        }
        return tmpfile;
    }

    public static void writeToFile(File file, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(data);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }

    public static Intent getOpenAttachmentIntent(Attachment attachment) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = attachment.getMimeType();
        if (mimeType == null) {
            if (attachment.getFileUri() != null) {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(attachment.getFileUri().getLastPathSegment());
                if (fileExtension != null) {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                }
            }
            if (mimeType == null) {
                mimeType = "image/*";
            }
        }
        intent.setDataAndType(attachment.getFileUri(), mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static boolean isAttachmentFileExists(Attachment attachment) {
        boolean isExists = false;
        Uri fileUri = attachment.getFileUri();
        if (fileUri != null) {
            if (fileUri.getScheme().equals("file")) {
                File file = new File(fileUri.getPath());
                isExists = file.exists();
            } else {
                Log.e(TAG, "Uri scheme is not file");
            }
        } else {
            Log.e(TAG, "File does not exist");
        }
        return isExists;
    }


}
