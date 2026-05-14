package com.maple.daily.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateKeys {
    private static final Locale LOCALE = Locale.CHINA;

    private DateKeys() {
    }

    public static String todayKey() {
        return formatDate(new Date());
    }

    public static String monthKey() {
        return new SimpleDateFormat("yyyy-MM", LOCALE).format(new Date());
    }

    public static String yearKey() {
        return new SimpleDateFormat("yyyy", LOCALE).format(new Date());
    }

    public static String monthStartKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return formatDate(calendar.getTime());
    }

    public static String monthEndKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return formatDate(calendar.getTime());
    }

    public static String yearStartKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return formatDate(calendar.getTime());
    }

    public static String yearEndKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        return formatDate(calendar.getTime());
    }

    public static String formatMinutes(int minutes) {
        return String.format(LOCALE, "%02d:%02d", minutes / 60, minutes % 60);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", LOCALE).format(date);
    }

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE).format(date);
    }

    public static Date parseDate(String dateKey) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", LOCALE).parse(dateKey);
        } catch (ParseException ignored) {
            return null;
        }
    }

    public static long millisAtStartOfDay(String dateKey) {
        Date parsed = parseDate(dateKey);
        return parsed == null ? 0L : parsed.getTime();
    }
}
