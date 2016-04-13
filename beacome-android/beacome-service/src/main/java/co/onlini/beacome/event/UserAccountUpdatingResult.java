package co.onlini.beacome.event;

public class UserAccountUpdatingResult extends RequestResult {

    private final String mUpdatedData;

    public UserAccountUpdatingResult(boolean isSuccess, String updatedData) {
        super(isSuccess);
        mUpdatedData = updatedData;
    }

    public String getmUpdatedData() {
        return mUpdatedData;
    }

    public class UpdatedDataType {
        public static final String NAME_OR_EMAIL = "name_or_email";
        public static final String IMAGE = "image";
        public static final String SOCIAL_NETWORKS_LINKS = "social_networks_links";
    }
}
