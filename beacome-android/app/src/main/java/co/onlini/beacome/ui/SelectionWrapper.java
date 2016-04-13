package co.onlini.beacome.ui;

import android.os.Parcel;
import android.os.Parcelable;

public class SelectionWrapper<T extends Parcelable> implements Parcelable {
    private T mItem;
    private boolean mIsSelected;

    public SelectionWrapper(T item, boolean isSelected) {
        mItem = item;
        mIsSelected = isSelected;
    }

    public SelectionWrapper(T item) {
        this(item, false);
    }

    protected SelectionWrapper(Parcel in) {
        mIsSelected = in.readByte() != 0;
        Class<?> objectsType = (Class) in.readSerializable();
        in.readParcelable(objectsType.getClassLoader());
    }

    public static final Creator<SelectionWrapper> CREATOR = new Creator<SelectionWrapper>() {
        @Override
        public SelectionWrapper createFromParcel(Parcel in) {
            return new SelectionWrapper(in);
        }

        @Override
        public SelectionWrapper[] newArray(int size) {
            return new SelectionWrapper[size];
        }
    };

    public T getItem() {
        return mItem;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mIsSelected ? 1 : 0));
        final Class<?> objectsType = mItem.getClass();
        dest.writeSerializable(objectsType);
        dest.writeParcelable(mItem, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectionWrapper<?> that = (SelectionWrapper<?>) o;

        return !(mItem != null ? !mItem.equals(that.mItem) : that.mItem != null);
    }

    @Override
    public int hashCode() {
        return mItem != null ? mItem.hashCode() : 0;
    }
}
