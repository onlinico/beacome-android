package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class InviteModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("card_id")
    private String mCardUuid;
    @SerializedName("is_owner")
    private boolean mIsOwner;
    @SerializedName("email")
    private String mEmail;

    public InviteModel(String uuid, String cardUuid, boolean isOwner, String email) {
        mUuid = uuid;
        mCardUuid = cardUuid;
        mIsOwner = isOwner;
        mEmail = email;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public boolean isOwner() {
        return mIsOwner;
    }

    public String getEmail() {
        return mEmail;
    }
}
