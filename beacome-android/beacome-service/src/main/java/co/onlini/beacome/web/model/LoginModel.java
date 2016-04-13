package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class LoginModel {
    @SerializedName("login_provider")
    private String mProvider;
    @SerializedName("provider_key")
    private String mId;

    public LoginModel(String provider, String id) {
        mProvider = provider;
        mId = id;
    }

    public String getProvider() {
        return mProvider;
    }

    public String getId() {
        return mId;
    }
}
