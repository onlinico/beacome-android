package co.onlini.beacome.model;

public class ContactType implements CharSequence {
    private int mCode;
    private String mName;

    public ContactType(int code, String name) {
        mCode = code;
        mName = name;
    }

    public int getCode() {
        return mCode;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public int length() {
        return getName().length();
    }

    @Override
    public char charAt(int index) {
        return getName().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return getName().subSequence(start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactType type = (ContactType) o;

        if (mCode != type.mCode) return false;
        return true;

    }

    @Override
    public int hashCode() {
        return mCode;
    }
}
