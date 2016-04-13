package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class AttachmentModel {
    @SerializedName("id")
    private String mUuid;
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

    public AttachmentModel(String uuid, String description, int type, String mimeType, boolean isDeleted, long version) {
        mUuid = uuid;
        mDescription = description;
        mType = type;
        mMimeType = mimeType;
        mIsDeleted = isDeleted;
        mVersion = version;
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
}
