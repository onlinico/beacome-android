package co.onlini.beacome.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormatter {
    private static final String TIME_PATTERN = "d MMM, HH:mm";
    private static final String DATE_PATTERN = "d MMM yyyy";

    //Do not remove
    public static String setTimeStamp(long timestamp) {
        Date date = new Date(timestamp);
        String formattedDateString;
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        formattedDateString = format.format(new Date(timestamp));
        return formattedDateString;
    }

    public static String getFormattedDate(Date date) {
        String formattedDateString;
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        formattedDateString = format.format(date);
        return formattedDateString;
    }
}
