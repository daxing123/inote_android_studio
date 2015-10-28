package org.dayup.inotes.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.dayup.common.Log;

import android.content.Context;
import android.text.format.Time;

public class DateUtils {
    private static final String TAG = DateUtils.class.getSimpleName();
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATE_FORMAT_TRANS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_YMDHM = "yyyy-MM-dd' 'HH:mm";
    public static final String DATE_FORMAT_HM = "HH:mm";
    public static final String DATE_FORMAT_HMS = "HH:mm:ss";
    public static final String DATE_FORMAT_EXPORT = "yyyyMMddHHmm";
    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_YMD);
    private static SimpleDateFormat hmsFormat = new SimpleDateFormat(DATE_FORMAT_HMS);
    static TimeZone utc = TimeZone.getTimeZone("UTC");
    static {
        sdf.setTimeZone(utc);
        hmsFormat.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
    }

    public static class DatePattern {
        public static final String YMD = "yyyyMMdd";
        public static final String YMD_YMD = "yyyy-MM-dd ";
        public static final String YMD_MDY = "MM-dd-yyyy";
        public static final String YMD_DMY = "dd-MM-yyyy";
        public static final String HM_COLON_24 = "HH:mm";
        public static final String HM_COLON_12 = "hh:mm aa";
    }

    public static String formatReminderHourAndMinute(Date date, boolean is24hours) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat gnotesRemiderDisplayFormet = new SimpleDateFormat(
                is24hours ? DatePattern.HM_COLON_24 : DatePattern.HM_COLON_12);
        return gnotesRemiderDisplayFormet.format(date);
    }

    public static boolean isWeekdayEvent(Time time) {
        if (time.weekDay != Time.SUNDAY && time.weekDay != Time.SATURDAY) {
            return true;
        }
        return false;
    }

    public static Date parse(String str, String pattern) {
        Date date = null;
        try {
            if (str == null || str.trim().length() == 0) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            date = sdf.parse(str);
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
        return date;
    }

    // public static Date parse(String str) {
    // Date date = null;
    // try {
    // if (str == null || str.trim().length() == 0) {
    // return null;
    // }
    // date = sdf.parse(str);
    // } catch (ParseException e) {
    // Log.e(TAG, "", e);
    // }
    // return date;
    // }

    public static String formatUTC(Date date) {
        sdf.setTimeZone(utc);
        return sdf.format(date);
    }

    public static String formatUTC(long date) {
        return formatUTC(new Date(date));
    }

    public static Date parseUTC(String date) {
        try {
            if (!StringUtils.isEmpty(date) && !StringUtils.equals("null", date)) {
                return sdf.parse(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "can't parse [" + date + "] to Date, format=" + sdf.toPattern());
            try {
                return new Date(Long.parseLong(date));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "can't parse [" + date + "] to Date, it isn't a Long.");
            }
        }
        return null;
    }

    public static long parse2longUTC(String value) {
        Date date = parseUTC(value);
        return date == null ? 0 : date.getTime();
    }

    public static long parse2longUTC(String value, long def) {
        Date date = parseUTC(value);
        return date == null ? def : date.getTime();
    }

    public static long parse2longUTC(String value, SimpleDateFormat sdf) {
        try {
            if (!StringUtils.isEmpty(value) && !StringUtils.equals("null", value)) {
                return sdf.parse(value).getTime();
            }
        } catch (ParseException e) {
            Log.e(TAG, "can't parse [" + value + "] to Date, format=" + sdf.toPattern());
            try {
                return new Date(Long.parseLong(value)).getTime();
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "can't parse [" + value + "] to Date, it isn't a Long.");
            }
        }
        return 0;
    }

    public static String toString(Date date) {
        if (date == null)
            return "";
        return sdf.format(date);
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String formatDate(Date date) {
        if (date == null)
            return "";
        return dateFormat.format(date);
    }

    public static String formatTime(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date.getTime());
    }

    public static String formatTime(long time, String pattern) {
        time = time == 0 ? System.currentTimeMillis() : time;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(time);
    }

    public static String formatMS(long date) {
        long time = date / 1000;
        int m = (int) time / 60;
        int s = (int) time % 60;
        DecimalFormat df = new DecimalFormat("#00");
        return df.format(m) + ":" + df.format(s);
    }

    public static String formatMS(String dateStr) {
        long date = 0;
        try {
            date = Long.parseLong(dateStr);
        } catch (NumberFormatException e) {
        }
        return formatMS(date);
    }

    public static boolean getWhetherTody(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YMD);
        if (StringUtils.equals(sdf.format(date), sdf.format(getCurrentDate()))) {
            return true;
        }
        return false;
    }

    private static long offset = 86400000L * 1000000;
    private static long offsetDay = offset / 86400000L;

    public static int getCurrentDiff(Date date) {
        return (int) ((date.getTime() - DateUtils.getCurrentDate().getTime() + offset) / 86400000 - offsetDay);
    }

    public static Date getCurrentDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false, true);
    }

    public static String formatTimeStampString(Context context, long when, boolean withWeekday,
            boolean showTime) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = android.text.format.DateUtils.FORMAT_NO_NOON_MIDNIGHT
                | android.text.format.DateUtils.FORMAT_ABBREV_ALL
                | android.text.format.DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR
                    | android.text.format.DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay || !showTime) {
            // If it is from a different day than today, show only the date.
            format_flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
        } else if (showTime) {
            // Otherwise, if the message is from today, show the time.
            format_flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make
        // showing
        // the year only happen if it is a different year from today).
        if (withWeekday) {
            format_flags |= android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
        }

        return android.text.format.DateUtils.formatDateTime(context, when, format_flags);
    }
}
