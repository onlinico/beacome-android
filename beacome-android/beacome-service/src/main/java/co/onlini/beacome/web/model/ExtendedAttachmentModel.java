package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class ExtendedAttachmentModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("card_id")
    private String mCardUuid;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("type")
    private int mType;
    @SerializedName("mime_type")
    private String mMimeType;
    @SerializedName("is_deleted")
    private boolean mIsDeleted;
    @SerializedName("rowversion")
    private long mVersion;
    @SerializedName("file")
    private String mEncodedFile;

    public ExtendedAttachmentModel(String uuid, String cardUuid, String description, int type, String mimeType, boolean isDeleted, long version, String encodedFile) {
        mUuid = uuid;
        mCardUuid = cardUuid;
        mDescription = description;
        mType = type;
        mMimeType = mimeType;
        mIsDeleted = isDeleted;
        mVersion = version;
        mEncodedFile = encodedFile;
    }

    public String getCardUuid() {
        return mCardUuid;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public long getVersion() {
        return mVersion;
    }

    public int getType() {
        return mType;
    }

    public String getEncodedFile() {
        return mEncodedFile;
    }
}
