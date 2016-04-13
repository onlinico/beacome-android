package co.onlini.beacome.event;

public abstract class RequestResult {
    private boolean mIsSuccess;

    public RequestResult(boolean isSuccess) {
        mIsSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return mIsSuccess;
    }
}
