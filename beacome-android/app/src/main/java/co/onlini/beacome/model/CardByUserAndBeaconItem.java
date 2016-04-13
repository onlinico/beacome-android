package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class CardByUserAndBeaconItem extends CardByUserItem implements Parcelable {

    public static final Creator<CardByUserAndBeaconItem> CREATOR = new Creator<CardByUserAndBeaconItem>() {
        @Override
        public CardByUserAndBeaconItem createFromParcel(Parcel in) {
            return new CardByUserAndBeaconItem(in);
        }

        @Override
        public CardByUserAndBeaconItem[] newArray(int size) {
            return new CardByUserAndBeaconItem[size];
        }
    };
    private boolean mIsActive;

    public CardByUserAndBeaconItem(String cardUuid, String title, String description, long version, boolean isCurrentUserOwner, int beaconsCount, Uri image, boolean isActive) {
        super(cardUuid, title, description, image, version, isCurrentUserOwner, beaconsCount);
        mIsActive = isActive;
    }

    protected CardByUserAndBeaconItem(Parcel in) {
        super(in);
        mIsActive = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mIsActive ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        mIsActive = isActive;
    }
}
