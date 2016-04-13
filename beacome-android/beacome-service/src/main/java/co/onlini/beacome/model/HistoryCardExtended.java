package co.onlini.beacome.model;

import android.net.Uri;

public class HistoryCardExtended extends HistoryCardBase {

    private Contact[] mContacts;
    private Vcard[] mVcards;
    private Attachment[] mAttachments;

    public HistoryCardExtended(String uuid, String title, String description, Uri imageUri, long lastDiscoveryDate, boolean isFavorite, long cardVersion, Contact[] contacts, Vcard[] vcards, Attachment[] attachments) {
        super(uuid, title, description, imageUri, lastDiscoveryDate, isFavorite, cardVersion);
        mContacts = contacts;
        mVcards = vcards;
        mAttachments = attachments;
    }

    public Contact[] getContacts() {
        return mContacts;
    }

    public Vcard[] getVcards() {
        return mVcards;
    }

    public Attachment[] getAttachments() {
        return mAttachments;
    }
}
