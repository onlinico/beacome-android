package co.onlini.beacome.model;

import android.net.Uri;

public class DiscountItem {
    private String mDescription;
    private String mCardTitle;
    private String mAttachmentUuid;
    private String mMimeType;
    private Uri mCardLogoUri;
    private Uri mLocalFileUri;
    private Uri mAttachmentUri;
    private long mVersion;

    public DiscountItem(String attachmentUuid, String description, String cardTitle, String mimeType, Uri cardLogoUri, Uri localFileUri, Uri attachmentUri, long version) {
        mAttachmentUuid = attachmentUuid;
        mDescription = description;
        mCardTitle = cardTitle;
        mMimeType = mimeType;

        mCardLogoUri = cardLogoUri;
        mLocalFileUri = localFileUri;
        mAttachmentUri = attachmentUri;
        mVersion = version;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public Uri getAttachmentUri() {
        return mAttachmentUri;
    }

    public long getVersion() {
        return mVersion;
    }

    public String getAttachmentUuid() {
        return mAttachmentUuid;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCardTitle() {
        return mCardTitle;
    }

    public Uri getCardLogoUri() {
        return mCardLogoUri;
    }

    public Uri getLocalFileUri() {
        return mLocalFileUri;
    }
}
