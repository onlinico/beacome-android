package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class CardByUserItem implements Parcelable {
    public static final Creator<CardByUserItem> CREATOR = new Creator<CardByUserItem>() {
        @Override
        public CardByUserItem createFromParcel(Parcel in) {
            return new CardByUserItem(in);
        }

        @Override
        public CardByUserItem[] newArray(int size) {
            return new CardByUserItem[size];
        }
    };
    private String mCardUuid;
    private String mTitle;
    private String mDescription;
    private Uri mImage;
    private long mVersion;
    private boolean mIsCurrentUserOwner;
    private int mBeaconsCount;

    public CardByUserItem(String cardUuid, String title, String description, Uri image, long version, boolean isCurrentUserOwner, int beaconsCount) {
        mCardUuid = cardUuid;
        mTitle = title;
        mDescription = description;
        mVersion = version;
        mIsCurrentUserOwner = isCurrentUserOwner;
        mBeaconsCount = beaconsCount;
        mImage = image;
    }

    protected CardByUserItem(Parcel in) {
        mCardUuid = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mImage = in.readParcelable(Uri.class.getClassLoader());
        mVersion = in.readLong();
        mIsCurrentUserOwner = in.readByte() != 0;
        mBeaconsCount = in.readInt();
    }

    public int getBeaconsCount() {
        return mBeaconsCount;
    }

    public boolean isCurrentUserOwner() {
        return mIsCurrentUserOwner;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Uri getImage() {
        return mImage;
    }

    public long getVersion() {
        return mVersion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCardUuid);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeParcelable(mImage, flags);
        dest.writeLong(mVersion);
        dest.writeByte((byte) (mIsCurrentUserOwner ? 1 : 0));
        dest.writeInt(mBeaconsCount);
    }
}
