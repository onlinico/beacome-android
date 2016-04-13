package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Session extends Credentials implements Parcelable {

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
    private String mDeviseAsBeaconUuid;
    private boolean mIsScannerRunning;
    private boolean mIsAdvertiserRunning;

    public Session(@NonNull String userUuid, String token, long expireDate, String deviseAsBeaconUuid, boolean isScannerRunning, boolean isAdvertiserRunning) {
        super(userUuid, token, expireDate);
        mDeviseAsBeaconUuid = deviseAsBeaconUuid;
        mIsScannerRunning = isScannerRunning;
        mIsAdvertiserRunning = isAdvertiserRunning;
    }

    protected Session(Parcel in) {
        super(in);
        mDeviseAsBeaconUuid = in.readString();
        mIsScannerRunning = in.readByte() != 0;
        mIsAdvertiserRunning = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mDeviseAsBeaconUuid);
        dest.writeByte((byte) (mIsScannerRunning ? 1 : 0));
        dest.writeByte((byte) (mIsAdvertiserRunning ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDeviseAsBeaconUuid() {
        return mDeviseAsBeaconUuid;
    }

    public boolean isScannerRunning() {
        return mIsScannerRunning;
    }

    public boolean isAdvertiserRunning() {
        return mIsAdvertiserRunning;
    }
}
