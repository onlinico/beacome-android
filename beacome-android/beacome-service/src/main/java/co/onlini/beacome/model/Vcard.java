package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Vcard implements Parcelable {

    public static final Creator<Vcard> CREATOR = new Creator<Vcard>() {
        @Override
        public Vcard createFromParcel(Parcel in) {
            return new Vcard(in);
        }

        @Override
        public Vcard[] newArray(int size) {
            return new Vcard[size];
        }
    };
    private String mUuid;
    private String mName;
    private String mEmail;
    private String mPhone;
    private Uri mVcfFile;
    private Uri mImageFile;
    private long mTimestamp;

    public Vcard(String uuid, String name, String email, String phone, Uri vcfFile, Uri imageFile, long timestamp) {
        mUuid = uuid;
        mName = name;
        mEmail = email;
        mPhone = phone;
        mVcfFile = vcfFile;
        mImageFile = imageFile;
        mTimestamp = timestamp;
    }

    protected Vcard(Parcel in) {
        mUuid = in.readString();
        mName = in.readString();
        mEmail = in.readString();
        mPhone = in.readString();
        mVcfFile = in.readParcelable(Uri.class.getClassLoader());
        mImageFile = in.readParcelable(Uri.class.getClassLoader());
        mTimestamp = in.readLong();
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public Uri getVcfFile() {
        return mVcfFile;
    }

    public void setVcfFile(Uri vcfFile) {
        mVcfFile = vcfFile;
    }

    public Uri getImageFile() {
        return mImageFile;
    }

    public void setImageFile(Uri imageFile) {
        mImageFile = imageFile;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeString(mPhone);
        dest.writeParcelable(mVcfFile, flags);
        dest.writeParcelable(mImageFile, flags);
        dest.writeLong(mTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vcard vcard = (Vcard) o;

        if (mUuid != null ? !mUuid.equals(vcard.mUuid) : vcard.mUuid != null) return false;
        if (mName != null ? !mName.equals(vcard.mName) : vcard.mName != null) return false;
        if (mEmail != null ? !mEmail.equals(vcard.mEmail) : vcard.mEmail != null) return false;
        if (mPhone != null ? !mPhone.equals(vcard.mPhone) : vcard.mPhone != null) return false;
        if (mVcfFile != null ? !mVcfFile.equals(vcard.mVcfFile) : vcard.mVcfFile != null)
            return false;
        return !(mImageFile != null ? !mImageFile.equals(vcard.mImageFile) : vcard.mImageFile != null);

    }

    @Override
    public int hashCode() {
        int result = mUuid != null ? mUuid.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mEmail != null ? mEmail.hashCode() : 0);
        result = 31 * result + (mPhone != null ? mPhone.hashCode() : 0);
        result = 31 * result + (mVcfFile != null ? mVcfFile.hashCode() : 0);
        result = 31 * result + (mImageFile != null ? mImageFile.hashCode() : 0);
        return result;
    }
}
