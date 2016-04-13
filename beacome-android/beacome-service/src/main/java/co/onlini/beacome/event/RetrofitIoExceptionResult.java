package co.onlini.beacome.event;

public class RetrofitIoExceptionResult {
    private String mMessage;

    public RetrofitIoExceptionResult(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }
}
