package co.onlini.beacome.util.comparator;

import java.util.Comparator;

import co.onlini.beacome.model.CardUser;


public class CardUserComparator implements Comparator<CardUser> {

    public static final int COMPARE_BY_ID = 0x1;

    private int mCompareType;

    public CardUserComparator(int compareType) {
        mCompareType = compareType;
    }

    @Override
    public int compare(CardUser lhs, CardUser rhs) {
        int compareResult;
        switch (mCompareType) {
            case COMPARE_BY_ID:
            default:
                compareResult = rhs.getName() != null && lhs.getName() != null ? rhs.getName().compareTo(lhs.getUuid()) : 0;
        }
        return compareResult;
    }

}