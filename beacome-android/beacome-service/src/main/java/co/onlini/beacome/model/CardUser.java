package co.onlini.beacome.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class CardUser extends User implements Parcelable {

    public static final Creator<CardUser> CREATOR = new Creator<CardUser>() {
        @Override
        public CardUser createFromParcel(Parcel in) {
            return new CardUser(in);
        }

        @Override
        public CardUser[] newArray(int size) {
            return new CardUser[size];
        }
    };
    private boolean mIsOwner;

    public CardUser(String uuid, String shareUuid, String name, String email, Uri image, boolean isOwner, long version) {
        super(uuid, shareUuid, name, email, image, version);
        mIsOwner = isOwner;
    }


    public CardUser(User user, boolean isOwner) {
        this(user.getUuid(), user.getShareUuid(), user.getName(), user.getEmail(), user.getImage(), isOwner, user.getVersion());
    }

    protected CardUser(Parcel in) {
        super(in);
        mIsOwner = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mIsOwner ? 1 : 0));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isOwner() {
        return mIsOwner;
    }

    public void setIsOwner(boolean isOwner) {
        mIsOwner = isOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CardUser cardUser = (CardUser) o;

        return mIsOwner == cardUser.mIsOwner;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mIsOwner ? 1 : 0);
        return result;
    }
}
