package co.onlini.beacome.util.comparator;

import java.util.Comparator;

import co.onlini.beacome.model.Vcard;

public class VcardComparator implements Comparator<Vcard> {

    public static final int COMPARE_BY_TIMESTAMP = 0x1;

    private int mCompareType;

    public VcardComparator(int compareType) {
        mCompareType = compareType;
    }

    @Override
    public int compare(Vcard lhs, Vcard rhs) {
        int compareResult;
        switch (mCompareType) {
            case COMPARE_BY_TIMESTAMP:
            default:
                compareResult = (int) (lhs.getTimestamp() - rhs.getTimestamp());
        }
        return compareResult;
    }

}