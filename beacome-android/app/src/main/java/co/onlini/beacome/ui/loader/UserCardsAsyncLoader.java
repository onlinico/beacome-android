package co.onlini.beacome.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.BaseCard;
import co.onlini.beacome.model.CardByUserItem;

public class UserCardsAsyncLoader extends AsyncTaskLoader<List<CardByUserItem>> {

    private CardHelper mHelper;
    private String mUserUuid;

    public UserCardsAsyncLoader(Context context) {
        super(context);
        mHelper = CardHelper.getInstance(context);
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public List<CardByUserItem> loadInBackground() {
        List<BaseCard> baseCards = mHelper.getBaseCardsByUser(mUserUuid);
        List<CardByUserItem> cardByUserItems = new ArrayList<>(baseCards.size());
        for (BaseCard baseCard : baseCards) {
            boolean isOwner = mHelper.isUserOwnerOfCard(baseCard.getUuid(), mUserUuid);
            int beaconsCount = mHelper.getBeaconsCountLinkedToCard(baseCard.getUuid());
            CardByUserItem card =
                    new CardByUserItem(baseCard.getUuid(), baseCard.getTitle(), baseCard.getDescription(), baseCard.getImage(), baseCard.getVersion(), isOwner, beaconsCount);
            cardByUserItems.add(card);
        }
        return cardByUserItems;
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
