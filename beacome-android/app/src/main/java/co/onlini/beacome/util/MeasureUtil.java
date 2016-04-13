package co.onlini.beacome.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;

public class MeasureUtil {

    public static int measureContentAreaHeight(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        int contentViewTop = getStatusBarHeight(activity);
        return point.y - contentViewTop;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
