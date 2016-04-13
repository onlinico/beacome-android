package co.onlini.beacome.model;

import android.net.Uri;

public class HistoryCardBase {
    private String mUuid;
    private String mTitle;
    private String mDescription;
    private Uri mImageUri;
    private long mLastDiscoveryDate;
    private boolean mIsFavorite;
    private long mCardVersion;

    public HistoryCardBase(String uuid, String title, String description, Uri imageUri, long lastDiscoveryDate, boolean isFavorite, long cardVersion) {
        mUuid = uuid;
        mTitle = title;
        mDescription = description;
        mImageUri = imageUri;
        mLastDiscoveryDate = lastDiscoveryDate;
        mIsFavorite = isFavorite;
        mCardVersion = cardVersion;
    }

    public void setIsFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
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

    public Uri getImageUri() {
        return mImageUri;
    }

    public long getLastDiscoveryDate() {
        return mLastDiscoveryDate;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryCardBase cardBase = (HistoryCardBase) o;

        if (mLastDiscoveryDate != cardBase.mLastDiscoveryDate) return false;
        if (mIsFavorite != cardBase.mIsFavorite) return false;
        if (mCardVersion != cardBase.mCardVersion) return false;
        if (mUuid != null ? !mUuid.equals(cardBase.mUuid) : cardBase.mUuid != null) return false;
        if (mTitle != null ? !mTitle.equals(cardBase.mTitle) : cardBase.mTitle != null)
            return false;
        if (mDescription != null ? !mDescription.equals(cardBase.mDescription) : cardBase.mDescription != null)
            return false;
        return !(mImageUri != null ? !mImageUri.equals(cardBase.mImageUri) : cardBase.mImageUri != null);

    }

    @Override
    public int hashCode() {
        int result = mUuid != null ? mUuid.hashCode() : 0;
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        result = 31 * result + (mImageUri != null ? mImageUri.hashCode() : 0);
        result = 31 * result + (int) (mLastDiscoveryDate ^ (mLastDiscoveryDate >>> 32));
        result = 31 * result + (mIsFavorite ? 1 : 0);
        result = 31 * result + (int) (mCardVersion ^ (mCardVersion >>> 32));
        return result;
    }

    public long getCardVersion() {

        return mCardVersion;
    }
}
