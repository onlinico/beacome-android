package co.onlini.beacome.ui.loader;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.DiscountItem;

public class DiscountsAsyncLoader extends AsyncTaskLoader<List<DiscountItem>> {

    private CardHelper mHelper;
    private String mUserUuid;

    public DiscountsAsyncLoader(Context context) {
        super(context);
        mHelper = CardHelper.getInstance(context);
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public List<DiscountItem> loadInBackground() {
        return mHelper.getDiscounts(mUserUuid);
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
