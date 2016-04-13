package co.onlini.beacome.event;

public class SetHistoryFavoriteResult extends RequestResult {

    private String mCardUuid;
    private boolean mIsFavorite;

    public SetHistoryFavoriteResult(boolean isSuccess, String cardUuid, boolean isFavorite) {
        super(isSuccess);
        mCardUuid = cardUuid;

        mIsFavorite = isFavorite;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }
}
