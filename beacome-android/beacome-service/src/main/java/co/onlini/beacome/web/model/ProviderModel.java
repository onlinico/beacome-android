package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class ProviderModel {
    @SerializedName("provider")
    private String mAuthProvider;
    @SerializedName("access_token")
    private String mToken;
    @SerializedName("access_token_secret")
    private String mTokenSecret;

    public ProviderModel(String authProvider, String token, String tokenSecret) {
        mAuthProvider = authProvider;
        mToken = token;
        mTokenSecret = tokenSecret;
    }

    public String getAuthProvider() {
        return mAuthProvider;
    }

    public String getToken() {
        return mToken;
    }

    public String getTokenSecret() {
        return mTokenSecret;
    }
}
