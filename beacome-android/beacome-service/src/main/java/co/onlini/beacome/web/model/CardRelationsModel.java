package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class CardRelationsModel {
    @SerializedName("card_id")
    private String mCardUuid;
    @SerializedName("is_active")
    private boolean mIsActive;

    public CardRelationsModel(String cardUuid, boolean isActive) {
        mCardUuid = cardUuid;
        mIsActive = isActive;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public boolean isActive() {
        return mIsActive;
    }
}