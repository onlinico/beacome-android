package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable {

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    private String mUuid;
    private int mType;
    private String mMimeType;
    private String mDescription;
    private Uri mFileUri;
    private Uri mUri;


    public Attachment(String uuid, int type, String mimeType, String description, Uri fileUri, Uri uri) {
        mUuid = uuid;
        mType = type;
        mMimeType = mimeType;
        mDescription = description;
        mFileUri = fileUri;
        mUri = uri;
    }

    protected Attachment(Parcel in) {
        mUuid = in.readString();
        mType = in.readInt();
        mMimeType = in.readString();
        mDescription = in.readString();
        mFileUri = in.readParcelable(Uri.class.getClassLoader());
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public Uri getUri() {
        return mUri;
    }

    public Uri getFileUri() {
        return mFileUri;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getUuid() {
        return mUuid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeInt(mType);
        dest.writeString(mMimeType);
        dest.writeString(mDescription);
        dest.writeParcelable(mFileUri, flags);
        dest.writeParcelable(mUri, flags);
    }
}
