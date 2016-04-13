package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Credentials implements Parcelable {
    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        @Override
        public Credentials createFromParcel(Parcel in) {
            return new Credentials(in);
        }

        @Override
        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
    private String mUserUuid;
    private String mToken;
    private long mExpireDate;

    public Credentials(@NonNull String userUuid, String token, long expireDate) {
        mToken = token;
        mExpireDate = expireDate;
        mUserUuid = userUuid;
    }

    protected Credentials(Parcel in) {
        mUserUuid = in.readString();
        mToken = in.readString();
        mExpireDate = in.readLong();
    }

    public boolean isAnonymous() {
        return mToken == null;
    }

    public String getToken() {
        return mToken;
    }

    public long getExpireDate() {
        return mExpireDate;
    }

    public String getUserUuid() {
        return mUserUuid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserUuid);
        dest.writeString(mToken);
        dest.writeLong(mExpireDate);
    }
}
