package co.onlini.beacome.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.CardExtended;

public class CardAsyncLoader extends AsyncTaskLoader<CardExtended> {

    private CardHelper mHelper;
    private String mCardUuid;
    private String mUserUuid;

    public CardAsyncLoader(Context context, String cardUuid) {
        super(context);
        if (cardUuid == null) {
            throw new IllegalArgumentException();
        }
        mCardUuid = cardUuid;
        mHelper = CardHelper.getInstance(context);
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public CardExtended loadInBackground() {
        return mHelper.getCard(mCardUuid, mUserUuid);
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
