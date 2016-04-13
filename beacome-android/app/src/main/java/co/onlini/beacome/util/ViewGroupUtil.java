package co.onlini.beacome.util;

import android.view.View;
import android.widget.Adapter;

public class ViewGroupUtil {
    public static void fillList(android.view.ViewGroup viewGroup, Adapter adapter) {
        viewGroup.removeAllViewsInLayout();
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, viewGroup);
            viewGroup.addView(view);
        }
        if (adapter.getCount() == 0) {
            viewGroup.requestLayout();
            viewGroup.invalidate();
        }
    }
}
