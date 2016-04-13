package co.onlini.beacome.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import co.onlini.beacome.dal.HistoryHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.HistoryCardBase;

public class HistoryAsyncLoader extends AsyncTaskLoader<List<HistoryCardBase>> {

    private final Context mContext;
    private HistoryHelper mHelper;

    public HistoryAsyncLoader(Context context) {
        super(context);
        mHelper = HistoryHelper.getInstance(context);
        mContext = context;
    }

    @Override
    public List<HistoryCardBase> loadInBackground() {
        String userUuid = SessionManager.getSession(mContext).getUserUuid();
        return mHelper.getHistoryCards(userUuid);
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
