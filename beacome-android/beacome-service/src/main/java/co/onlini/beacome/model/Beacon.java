package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a beacon entity related to a user,
 * contains a number of card-beacon links to the beacon for the user.
 */
public class Beacon implements Parcelable {
    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };
    private String mBeaconUuid;
    private CardLink[] mCardLinks;

    public Beacon(String beaconUuid, CardLink[] cardLinks) {
        mBeaconUuid = beaconUuid;
        mCardLinks = cardLinks;
    }

    protected Beacon(Parcel in) {
        mBeaconUuid = in.readString();
        mCardLinks = in.createTypedArray(CardLink.CREATOR);
    }

    public String getBeaconUuid() {
        return mBeaconUuid;
    }

    public CardLink[] getCardLinks() {
        return mCardLinks;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beacon beacon = (Beacon) o;

        return !(mBeaconUuid != null ? !mBeaconUuid.equals(beacon.mBeaconUuid) : beacon.mBeaconUuid != null);

    }

    @Override
    public int hashCode() {
        return mBeaconUuid != null ? mBeaconUuid.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBeaconUuid);
        dest.writeTypedArray(mCardLinks, flags);
    }
}
