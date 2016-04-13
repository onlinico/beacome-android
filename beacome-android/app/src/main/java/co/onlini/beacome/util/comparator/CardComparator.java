package co.onlini.beacome.util.comparator;

import java.util.Comparator;

import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.util.SortVariants;

public class CardComparator implements Comparator<CardByUserItem> {

    private int mCompareType;

    public CardComparator(int compareType) {
        mCompareType = compareType;
    }

    @Override
    public int compare(CardByUserItem lhs, CardByUserItem rhs) {
        int compareResult;
        switch (mCompareType) {
            case SortVariants.BY_NAME_DESC:
                compareResult = rhs.getTitle().compareTo(lhs.getTitle());
                break;
            case SortVariants.BY_BEACONS_COUNT:
                compareResult = lhs.getBeaconsCount() - rhs.getBeaconsCount();
                if (compareResult == 0) {
                    compareResult = lhs.getTitle().compareTo(rhs.getTitle());
                }
                break;
            case SortVariants.BY_NAME:
            default:
                compareResult = lhs.getTitle().compareTo(rhs.getTitle());
        }
        return compareResult;
    }
}
