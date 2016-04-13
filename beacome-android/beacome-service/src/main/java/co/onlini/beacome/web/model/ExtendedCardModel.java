package co.onlini.beacome.web.model;

import com.google.gson.annotations.SerializedName;

public class ExtendedCardModel {
    @SerializedName("id")
    private String mUuid;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("contacts")
    private ContactModel[] mContacts;

    public ExtendedCardModel(String uuid, String title, String description, ContactModel[] contacts) {
        mUuid = uuid;
        mTitle = title;
        mDescription = description;
        mContacts = contacts;
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

    public ContactModel[] getContacts() {
        return mContacts;
    }
}