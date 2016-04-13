package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class HistoryModel {
    @SerializedName("card_id")
    private String mCardUuid;
    @SerializedName("received")
    private long mReceivedTime;
    @SerializedName("is_favorite")
    private boolean mIsFavorite;
    @SerializedName("rowversion")
    private long mVersion;

    public HistoryModel(String cardUuid, long receivedTime, boolean isFavorite, long version) {
        mCardUuid = cardUuid;
        mReceivedTime = receivedTime;
        mIsFavorite = isFavorite;
        mVersion = version;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public long getReceivedTime() {
        return mReceivedTime;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public long getVersion() {
        return mVersion;
    }
}
