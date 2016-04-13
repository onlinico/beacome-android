package co.onlini.beacome.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import co.onlini.beacome.dal.UserHelper;
import co.onlini.beacome.model.UserWithLinks;


public class RegisteredUserAsyncLoader extends AsyncTaskLoader<UserWithLinks> {

    private UserHelper mHelper;
    private String mUserUuid;

    public RegisteredUserAsyncLoader(Context context, String userUuid) {
        super(context);
        mUserUuid = userUuid;
        mHelper = UserHelper.getInstance(context);
    }

    @Override
    public UserWithLinks loadInBackground() {
        return mHelper.getRegisteredUser(mUserUuid);
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
