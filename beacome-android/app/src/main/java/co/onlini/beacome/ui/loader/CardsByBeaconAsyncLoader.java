package co.onlini.beacome.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardByUserAndBeaconItem;
import co.onlini.beacome.model.CardExtended;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.CardUser;

public class CardsByBeaconAsyncLoader extends AsyncTaskLoader<List<CardByUserAndBeaconItem>> {

    private CardHelper mHelper;
    private String mBeaconUuid;
    private String mUserUuid;

    public CardsByBeaconAsyncLoader(Context context, String beaconUuid) {
        super(context);
        if (beaconUuid == null) {
            throw new IllegalArgumentException();
        }
        mBeaconUuid = beaconUuid;
        mHelper = CardHelper.getInstance(context);
        mUserUuid = SessionManager.getSession(context).getUserUuid();
    }

    @Override
    public List<CardByUserAndBeaconItem> loadInBackground() {
        CardExtended[] extendedCards = mHelper.getCardsByBeacon(mUserUuid, mBeaconUuid);
        List<CardByUserAndBeaconItem> items = new ArrayList<>(extendedCards.length);
        for (CardExtended cardExtended : extendedCards) {
            boolean isOwner = false;
            for (CardUser cardUser : cardExtended.getUsers()) {
                if (mUserUuid.equals(cardUser.getUuid())) {
                    isOwner = cardUser.isOwner();
                    break;
                }
            }
            boolean isActive = false;
            for (Beacon beacon : cardExtended.getBeaconLinks()) {
                if (mBeaconUuid.equals(beacon.getBeaconUuid())) {
                    for (CardLink cardLink : beacon.getCardLinks()) {
                        if (cardLink.getCardUuid().equals(cardExtended.getUuid())) {
                            isActive = cardLink.isActive();
                            break;
                        }
                    }
                }
                if (isActive) {
                    break;
                }
            }
            items.add(new CardByUserAndBeaconItem(cardExtended.getUuid(), cardExtended.getTitle(),
                    cardExtended.getDescription(), cardExtended.getVersion(), isOwner,
                    cardExtended.getBeaconLinks().length, cardExtended.getImage(), isActive));
        }
        return items;
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
