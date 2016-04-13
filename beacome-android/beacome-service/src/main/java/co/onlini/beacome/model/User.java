package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String mUuid;
    private String mShareUuid;
    private String mName;
    private String mEmail;
    private Uri mImage;
    private long mVersion;


    public User(String uuid, String shareUuid, String name, String email, Uri image, long version) {
        mUuid = uuid;
        mShareUuid = shareUuid;
        mName = name;
        mEmail = email;
        mImage = image;
        mVersion = version;
    }

    protected User(Parcel in) {
        mUuid = in.readString();
        mShareUuid = in.readString();
        mName = in.readString();
        mEmail = in.readString();
        mImage = in.readParcelable(Uri.class.getClassLoader());
        mVersion = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeString(mShareUuid);
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeParcelable(mImage, flags);
        dest.writeLong(mVersion);
    }

    public String getUuid() {
        return mUuid;
    }

    public String getShareUuid() {
        return mShareUuid;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public Uri getImage() {
        return mImage;
    }

    public long getVersion() {
        return mVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (mVersion != user.mVersion) return false;
        if (mUuid != null ? !mUuid.equals(user.mUuid) : user.mUuid != null) return false;
        if (mShareUuid != null ? !mShareUuid.equals(user.mShareUuid) : user.mShareUuid != null)
            return false;
        if (mName != null ? !mName.equals(user.mName) : user.mName != null) return false;
        if (mEmail != null ? !mEmail.equals(user.mEmail) : user.mEmail != null) return false;
        return !(mImage != null ? !mImage.equals(user.mImage) : user.mImage != null);

    }

    @Override
    public int hashCode() {
        int result = mUuid != null ? mUuid.hashCode() : 0;
        result = 31 * result + (mShareUuid != null ? mShareUuid.hashCode() : 0);
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mEmail != null ? mEmail.hashCode() : 0);
        result = 31 * result + (mImage != null ? mImage.hashCode() : 0);
        result = 31 * result + (int) (mVersion ^ (mVersion >>> 32));
        return result;
    }
}

