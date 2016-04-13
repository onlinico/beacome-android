package co.onlini.beacome.event;

public class ScannerServiceState {
    private boolean mIsRunning;

    public ScannerServiceState(boolean isRunning) {
        mIsRunning = isRunning;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

}
