package co.onlini.beacome.event;

import co.onlini.beacome.model.Credentials;

public class SignInResult extends RequestResult {
    private final Credentials mCredentials;
    private final boolean mIsNewSession;

    public SignInResult(boolean isSuccess, Credentials credentials, boolean isNewSession) {
        super(isSuccess);
        mCredentials = credentials;
        mIsNewSession = isNewSession;
    }

    public Credentials getCredentials() {
        return mCredentials;
    }

    public boolean isNewSession() {
        return mIsNewSession;
    }
}
