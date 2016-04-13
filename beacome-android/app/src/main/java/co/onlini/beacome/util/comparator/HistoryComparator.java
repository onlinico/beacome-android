package co.onlini.beacome.util.comparator;

import java.util.Comparator;

import co.onlini.beacome.model.HistoryCardBase;
import co.onlini.beacome.util.SortVariants;

public class HistoryComparator implements Comparator<HistoryCardBase> {

    private int mCompareType;

    public HistoryComparator(int compareType) {
        mCompareType = compareType;
    }

    @Override
    public int compare(HistoryCardBase lhs, HistoryCardBase rhs) {
        int compareResult;
        switch (mCompareType) {
            case SortVariants.BY_NAME_DESC:
                compareResult = rhs.getTitle().compareTo(lhs.getTitle());
                break;
            case SortVariants.BY_DATE:
                compareResult = (int) (rhs.getLastDiscoveryDate() - lhs.getLastDiscoveryDate());
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
