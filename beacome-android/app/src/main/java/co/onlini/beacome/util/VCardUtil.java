package co.onlini.beacome.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import co.onlini.beacome.R;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.ImageType;
import ezvcard.property.FormattedName;
import ezvcard.property.Photo;
import ezvcard.property.StructuredName;

public class VCardUtil {
    public static Uri getVCardVcfFile(Context context, String name, String phone, String email, Uri image) {
        VCard vcardData = new VCard();
        StructuredName n = new StructuredName();
        String[] nameParts = name.split(" ");
        if (nameParts.length != 2) {
            n.setGiven(nameParts[0]);
        } else {
            n.setGiven(nameParts[0]);
            n.setFamily(nameParts[1]);
        }
        vcardData.setStructuredName(n);
        vcardData.setFormattedName(new FormattedName(name));
        vcardData.addTelephoneNumber(phone);
        vcardData.addEmail(email);

        if (image != null) {
            if (image.getScheme().equals("file")) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (inputStream != null) {
                    int size = context.getResources().getInteger(R.integer.image_compressing_biggest_side);
                    Bitmap bitmap = BitmapUtil.decodeSampledBitmapFromStream(inputStream, size, size);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        if (byteArray != null) {
                            vcardData.addPhoto(new Photo(byteArray, ImageType.PNG));
                        }
                    }
                }
            } else if (image.getScheme().equals("http")) {
                vcardData.addPhoto(new Photo(image.toString(), ImageType.PNG));
            }
        }
        String data = Ezvcard.write(vcardData).version(VCardVersion.V3_0).go();
        File tmpFile = FileUtil.getTempFile(context);
        FileUtil.writeToFile(tmpFile, data);
        return tmpFile != null ? Uri.fromFile(tmpFile) : null;
    }

}
