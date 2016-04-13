package co.onlini.beacome.ui.loader;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.Beacon;

public class BeaconsAsyncLoader extends AsyncTaskLoader<Beacon[]> {

    private CardHelper mHelper;
    private String mUserUuid;

    public BeaconsAsyncLoader(Context context) {
        super(context);
        mHelper = CardHelper.getInstance(context);
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public Beacon[] loadInBackground() {
        return mHelper.getBeaconsByUser(mUserUuid);
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
