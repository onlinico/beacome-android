package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class BeaconModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("relations")
    private CardRelationsModel[] mCardRelations;

    public BeaconModel(String uuid, CardRelationsModel[] cardRelations) {
        mUuid = uuid;
        mCardRelations = cardRelations;
    }

    public String getUuid() {
        return mUuid;
    }

    public CardRelationsModel[] getCardRelations() {
        return mCardRelations;
    }
}
