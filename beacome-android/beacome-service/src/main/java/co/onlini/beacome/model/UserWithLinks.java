package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class UserWithLinks extends User implements Parcelable {

    public static final Creator<UserWithLinks> CREATOR = new Creator<UserWithLinks>() {
        @Override
        public UserWithLinks createFromParcel(Parcel in) {
            return new UserWithLinks(in);
        }

        @Override
        public UserWithLinks[] newArray(int size) {
            return new UserWithLinks[size];
        }
    };
    private boolean mIsFacebookLinked;
    private boolean mIsTwitterLinked;
    private boolean mIsGpLinked;

    public UserWithLinks(String uuid, String shareUuid, String name, String email, Uri image, long version, boolean isFacebookLinked, boolean isTwitterLinked, boolean isGpLinked) {
        super(uuid, shareUuid, name, email, image, version);
        mIsFacebookLinked = isFacebookLinked;
        mIsTwitterLinked = isTwitterLinked;
        mIsGpLinked = isGpLinked;
    }

    protected UserWithLinks(Parcel in) {
        super(in);
        mIsFacebookLinked = in.readByte() != 0;
        mIsTwitterLinked = in.readByte() != 0;
        mIsGpLinked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mIsFacebookLinked ? 1 : 0));
        dest.writeByte((byte) (mIsTwitterLinked ? 1 : 0));
        dest.writeByte((byte) (mIsGpLinked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isFacebookLinked() {
        return mIsFacebookLinked;
    }

    public boolean isTwitterLinked() {
        return mIsTwitterLinked;
    }

    public boolean isGpLinked() {
        return mIsGpLinked;
    }

}
