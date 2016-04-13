package co.onlini.beacome.ui.loader;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import co.onlini.beacome.dal.HistoryHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.HistoryCardExtended;

public class HistoryCardAsyncLoader extends AsyncTaskLoader<HistoryCardExtended> {

    private HistoryHelper mHelper;
    private String mCardUuid;
    private String mUserUuid;

    public HistoryCardAsyncLoader(Context context, String cardUuid) {
        super(context);
        mHelper = HistoryHelper.getInstance(context);
        mCardUuid = cardUuid;
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public HistoryCardExtended loadInBackground() {
        return mHelper.getHistoryCard(mUserUuid, mCardUuid);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
