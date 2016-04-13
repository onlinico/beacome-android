package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class UserPermissionModel {
    @SerializedName("user_id")
    private String mUserUuid;
    @SerializedName("is_owner")
    private boolean mIsOwner;

    public UserPermissionModel(String userUuid, boolean isOwner) {
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
