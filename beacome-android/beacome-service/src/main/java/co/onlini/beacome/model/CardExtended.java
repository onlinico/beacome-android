package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class CardExtended extends Card implements Parcelable {

    public static final Creator<CardExtended> CREATOR = new Creator<CardExtended>() {
        @Override
        public CardExtended createFromParcel(Parcel in) {
            return new CardExtended(in);
        }

        @Override
        public CardExtended[] newArray(int size) {
            return new CardExtended[size];
        }
    };
    private CardUser[] mUsers;
    private Beacon[] mBeaconLinks;

    public CardExtended(String uuid, String title, String description, long version, Uri image, Contact[] contacts, Vcard[] vcards, Attachment[] attachments, CardUser[] users, Beacon[] beaconLinks) {
        super(uuid, title, description, version, image, contacts, vcards, attachments);
        mUsers = users;
        mBeaconLinks = beaconLinks;
    }

    protected CardExtended(Parcel in) {
        super(in);
        mUsers = in.createTypedArray(CardUser.CREATOR);
        mBeaconLinks = in.createTypedArray(Beacon.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(mUsers, flags);
        dest.writeTypedArray(mBeaconLinks, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public CardUser[] getUsers() {
        return mUsers;
    }

    public Beacon[] getBeaconLinks() {
        return mBeaconLinks;
    }
}



