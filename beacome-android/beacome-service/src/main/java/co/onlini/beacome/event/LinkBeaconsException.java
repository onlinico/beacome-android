package co.onlini.beacome.event;

import java.util.List;

public class LinkBeaconsException {
    private List<String> mUnprocessedBeaconsUuids;

    public LinkBeaconsException(List<String> unprocessedBeaconsUuids) {
        mUnprocessedBeaconsUuids = unprocessedBeaconsUuids;
    }

    public List<String> getUnprocessedBeaconsUuids() {
        return mUnprocessedBeaconsUuids;
    }
}
