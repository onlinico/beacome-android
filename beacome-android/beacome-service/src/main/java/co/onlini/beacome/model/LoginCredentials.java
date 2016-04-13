package co.onlini.beacome.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginCredentials implements Parcelable {
    public static final Creator<LoginCredentials> CREATOR = new Creator<LoginCredentials>() {
        @Override
        public LoginCredentials createFromParcel(Parcel in) {
            return new LoginCredentials(in);
        }

        @Override
        public LoginCredentials[] newArray(int size) {
            return new LoginCredentials[size];
        }
    };
    private String mAuthProvider;
    private String mAuthToken;
    private String mAuthSecret;

    public LoginCredentials(String authProvider, String authToken, String authSecret) {
        mAuthProvider = authProvider;
        mAuthToken = authToken;
        mAuthSecret = authSecret;
    }

    protected LoginCredentials(Parcel in) {
        mAuthProvider = in.readString();
        mAuthToken = in.readString();
        mAuthSecret = in.readString();
    }

    public String getAuthProvider() {
        return mAuthProvider;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getAuthSecret() {
        return mAuthSecret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthProvider);
        dest.writeString(mAuthToken);
        dest.writeString(mAuthSecret);
    }
}
