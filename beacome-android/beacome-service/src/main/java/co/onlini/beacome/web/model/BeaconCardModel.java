package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class BeaconCardModel {
    @SerializedName("id")
    private String mBeaconUuid;
    @SerializedName("is_active")
    private boolean mIsActive;

    public BeaconCardModel(String beaconUuid, boolean isActive) {
        mBeaconUuid = beaconUuid;
        mIsActive = isActive;
    }

    public String getBeaconUuid() {
        return mBeaconUuid;
    }

    public boolean isActive() {
        return mIsActive;
    }
}
