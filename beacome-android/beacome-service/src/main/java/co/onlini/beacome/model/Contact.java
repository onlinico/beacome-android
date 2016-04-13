package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    private String mUuid;
    private int mContactType;
    private String mData;

    public Contact(String uuid, int contactType, String data) {
        mUuid = uuid;
        mContactType = contactType;
        mData = data;
    }

    protected Contact(Parcel in) {
        mUuid = in.readString();
        mContactType = in.readInt();
        mData = in.readString();
    }

    public String getUuid() {
        return mUuid;
    }

    public int getContactType() {
        return mContactType;
    }

    public void setContactType(int contactType) {
        mContactType = contactType;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeInt(mContactType);
        dest.writeString(mData);
    }
}
