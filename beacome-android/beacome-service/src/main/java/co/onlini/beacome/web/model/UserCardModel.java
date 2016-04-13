package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class UserCardModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("rowversion")
    private long mVersion;
    @SerializedName("contacts")
    private ContactModel[] mContacts;
    @SerializedName("users")
    private UserLinkModel[] mUsers;
    @SerializedName("invites")
    private InviteModel[] mInvites;
    @SerializedName("attachments")
    private AttachmentModel[] mAttachments;
    @SerializedName("beacons")
    private BeaconCardModel[] mBeacons;
    @SerializedName("is_delete")
    private boolean mIsDelete;

    public UserCardModel(String uuid, String title, String description, long version, ContactModel[] contacts, UserLinkModel[] users, InviteModel[] invites, AttachmentModel[] attachments, BeaconCardModel[] beacons, boolean isDelete) {
        mUuid = uuid;
        mTitle = title;
        mDescription = description;
        mVersion = version;
        mContacts = contacts;
        mUsers = users;
        mInvites = invites;
        mAttachments = attachments;
        mBeacons = beacons;
        mIsDelete = isDelete;
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

    public long getVersion() {
        return mVersion;
    }

    public ContactModel[] getContacts() {
        return mContacts;
    }

    public UserLinkModel[] getUsers() {
        return mUsers;
    }

    public InviteModel[] getInvites() {
        return mInvites;
    }

    public AttachmentModel[] getAttachments() {
        return mAttachments;
    }

    public BeaconCardModel[] getBeacons() {
        return mBeacons;
    }

    public boolean isDelete() {
        return mIsDelete;
    }
}
