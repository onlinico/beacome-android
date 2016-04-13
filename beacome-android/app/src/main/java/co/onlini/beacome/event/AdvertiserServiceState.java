package co.onlini.beacome.event;

public class AdvertiserServiceState {

    private final boolean mIsRunning;

    public AdvertiserServiceState(boolean isRunning) {
        mIsRunning = isRunning;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

}