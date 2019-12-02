package com.mtecresults.raceentry.api.client.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormats {
    private static ThreadLocal<SimpleDateFormat> timestampFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
        }
    };

    public static String timestampToFormatted(final Date d){
        return timestampFormatThreadLocal.get().format(d);
    }

    public static String timestampToFormatted(final long time) {
        return timestampToFormatted(new Date(time));
    }

    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("MM/dd/YYYY");
        }
    };

    public static String dateToFormattedMMDDYYYY(final Date d){
        return dateFormatThreadLocal.get().format(d);
    }

}
