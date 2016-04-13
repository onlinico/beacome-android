package co.onlini.beacome.model;

/**
 * All beacon`s data provided by a scanning service
 */

public class BeaconInfo {
    private String mUuid;
    private String mAddress;
    private int mTxPower;
    private int mRssi;
    private int mMajor;
    private int mMinor;
    private long mLastDiscoveryTimeStamp;


    public BeaconInfo(String address, int txPower, int rssi, String uuid, int major, int minor, long lastDiscoveryTimeStamp) {
        mAddress = address;
        mTxPower = txPower;
        mRssi = rssi;
        mUuid = uuid.toLowerCase();
        mMajor = major;
        mMinor = minor;
        mLastDiscoveryTimeStamp = lastDiscoveryTimeStamp;
    }

    public long getLastDiscoveryTimeStamp() {
        return mLastDiscoveryTimeStamp;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getTxPower() {
        return mTxPower;
    }

    public int getRssi() {
        return mRssi;
    }

    public String getUuid() {
        return mUuid;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeaconInfo that = (BeaconInfo) o;

        return !(mUuid != null ? !mUuid.equals(that.mUuid) : that.mUuid != null);

    }

    @Override
    public int hashCode() {
        return mUuid != null ? mUuid.hashCode() : 0;
    }

}
