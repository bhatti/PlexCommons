package com.plexobject.commons.utils;

import java.util.Date;

public class TimeUtils {
    public interface TimeSource {
        Date getCurrentTime();
    }

    private static TimeSource TIME_SOURCE = new TimeSource() {
        @Override
        public Date getCurrentTime() {
            return new Date();
        }
    };

    public static Date getCurrentTime() {
        return TIME_SOURCE.getCurrentTime();
    }

    public static long getCurrentTimeMillis() {
        return getCurrentTime().getTime();
    }

    public static void setTimeSource(final TimeSource source) {
        if (source == null) {
            throw new NullPointerException("source is null");
        }
        TIME_SOURCE = source;
    }
}
