package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class CardModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("is_delete")
    private boolean mIsDelete;
    @SerializedName("rowversion")
    private long mVersion;
    @SerializedName("contacts")
    private ContactModel[] mContacts;
    @SerializedName("attachments")
    private AttachmentModel[] mAttachment;

    public CardModel(String uuid, String title, String description, boolean isDelete, long version, ContactModel[] contacts, AttachmentModel[] attachment) {
        mUuid = uuid;
        mTitle = title;
        mDescription = description;
        mIsDelete = isDelete;
        mVersion = version;
        mContacts = contacts;
        mAttachment = attachment;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isDelete() {
        return mIsDelete;
    }

    public long getVersion() {
        return mVersion;
    }

    public ContactModel[] getContacts() {
        return mContacts;
    }

    public AttachmentModel[] getAttachments() {
        return mAttachment;
    }
}
