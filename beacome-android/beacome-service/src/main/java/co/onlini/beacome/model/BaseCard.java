package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class BaseCard implements Parcelable {

    public static final Creator<BaseCard> CREATOR = new Creator<BaseCard>() {
        @Override
        public BaseCard createFromParcel(Parcel in) {
            return new BaseCard(in);
        }

        @Override
        public BaseCard[] newArray(int size) {
            return new BaseCard[size];
        }
    };
    private String mUuid;
    private String mTitle;
    private String mDescription;
    private long mVersion;
    private Uri mImage;

    public BaseCard(String uuid, String title, String description, long version, Uri image) {
        mUuid = uuid;
        mTitle = title;
        mDescription = description;
        mVersion = version;
        mImage = image;
    }

    protected BaseCard(Parcel in) {
        mUuid = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mVersion = in.readLong();
        mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    public String getUuid() {
        return mUuid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public long getVersion() {
        return mVersion;
    }

    public Uri getImage() {
        return mImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeLong(mVersion);
        dest.writeParcelable(mImage, flags);
    }
}
