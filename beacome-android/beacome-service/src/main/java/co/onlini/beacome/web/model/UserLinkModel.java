package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class UserLinkModel {
    @SerializedName("id")
    private String mUserUuid;
    @SerializedName("is_owner")
    private boolean mIsOwner;

    public UserLinkModel(String userUuid, boolean isOwner) {
        mUserUuid = userUuid;
        mIsOwner = isOwner;
    }

    public String getUserUuid() {
        return mUserUuid;
    }

    public boolean isOwner() {
        return mIsOwner;
    }
}
