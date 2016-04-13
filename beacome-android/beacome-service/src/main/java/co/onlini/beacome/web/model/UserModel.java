package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("name")
    private String mName;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("rowversion")
    private long mVersion;

    public UserModel(String uuid, String name, String email, long version) {
        mUuid = uuid;
        mName = name;
        mEmail = email;
        mVersion = version;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public long getVersion() {
        return mVersion;
    }
}
