package co.onlini.beacome.event;

import java.util.List;

public class GetCardsByBeaconResult {
    private String mBeaconUuid;
    private List<String> mLinkedCardsUuids;
    private boolean mIsSuccessful;

    public GetCardsByBeaconResult(String beaconUuid, List<String> linkedCardsUuids, boolean isSuccessful) {
        mBeaconUuid = beaconUuid;
        mLinkedCardsUuids = linkedCardsUuids;
        mIsSuccessful = isSuccessful;
    }

    public String getBeaconUuid() {
        return mBeaconUuid;
    }

    public List<String> getLinkedCardsUuids() {
        return mLinkedCardsUuids;
    }

    public boolean isSuccessful() {
        return mIsSuccessful;
    }
}
