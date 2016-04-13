package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CardLink implements Parcelable {
    public static final Creator<CardLink> CREATOR = new Creator<CardLink>() {
        @Override
        public CardLink createFromParcel(Parcel in) {
            return new CardLink(in);
        }

        @Override
        public CardLink[] newArray(int size) {
            return new CardLink[size];
        }
    };
    private String mCardUuid;
    private boolean mIsActive;

    public CardLink(String cardUuid, boolean isActive) {
        mCardUuid = cardUuid;
        mIsActive = isActive;
    }

    protected CardLink(Parcel in) {
        mCardUuid = in.readString();
        mIsActive = in.readByte() != 0;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public void setIsActive(boolean isActive) {
        mIsActive = isActive;
    }

    public boolean isActive() {
        return mIsActive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCardUuid);
        dest.writeByte((byte) (mIsActive ? 1 : 0));
    }
}
