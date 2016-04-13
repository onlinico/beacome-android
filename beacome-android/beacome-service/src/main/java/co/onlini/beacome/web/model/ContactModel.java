package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class ContactModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("contact_type")
    private int mType;
    @SerializedName("data")
    private String mData;

    public ContactModel(String uuid, int type, String data) {
        mUuid = uuid;
        mType = type;
        mData = data;
    }

    public String getUuid() {
        return mUuid;
    }

    public int getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }
}
