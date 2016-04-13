package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Card extends BaseCard implements Parcelable {

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
    private Contact[] mContacts;
    private Vcard[] mVcards;
    private Attachment[] mAttachments;

    public Card(String uuid, String title, String description, long version, Uri image, Contact[] contacts, Vcard[] vcards, Attachment[] attachments) {
        super(uuid, title, description, version, image);
        mContacts = contacts;
        mVcards = vcards;
        mAttachments = attachments;
    }

    protected Card(Parcel in) {
        super(in);
        mContacts = in.createTypedArray(Contact.CREATOR);
        mVcards = in.createTypedArray(Vcard.CREATOR);
        mAttachments = in.createTypedArray(Attachment.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(mContacts, flags);
        dest.writeTypedArray(mVcards, flags);
        dest.writeTypedArray(mAttachments, flags);
    }

    @Override
    public int describeContents() {
        return 0;
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
