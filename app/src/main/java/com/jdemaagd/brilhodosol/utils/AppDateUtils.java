package com.jdemaagd.brilhodosol.utils;

import android.content.Context;
import android.text.format.DateUtils;

import com.jdemaagd.brilhodosol.R;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class AppDateUtils {

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    /**
     * Returns number of days since epoch (January 01, 1970, 12:00 Midnight UTC)
     *
     * @param date A date in milliseconds in local time
     * @return The number of days in UTC time from the epoch
     */
    public static long getDayNumber(long date) {
        TimeZone tz = TimeZone.getDefault();
        long gmtOffset = tz.getOffset(date);
        return (date + gmtOffset) / DAY_IN_MILLIS;
    }

    /**
     * Convert database representation of date into something to display to users
     * <p/>
     * For today: "Today, June 8"
     * For tomorrow:  "Tomorrow"
     * For the next 5 days: "Wednesday" (just the day name)
     * For all days after that: "Mon, Jun 8" (Mon, 8 Jun in UK, for example)
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds (UTC)
     * @param showFullDate Used to show a fuller-version of the date
     * @return A user-friendly representation of the date
     */
    public static String getFriendlyDateString(Context context, long dateInMillis, boolean showFullDate) {

        long localDate = getLocalDateFromUTC(dateInMillis);
        long dayNumber = getDayNumber(localDate);
        long currentDayNumber = getDayNumber(System.currentTimeMillis());

        if (dayNumber == currentDayNumber || showFullDate) {
            // current day format: "Today, June 24"
            String dayName = getDayName(context, localDate);
            String readableDate = getReadableDateString(context, localDate);
            if (dayNumber - currentDayNumber < 2) {
                /*
                 * Since there is no localized format that returns "Today" or "Tomorrow" in the API
                 * levels we have to support, we take the name of the day (from SimpleDateFormat)
                 * and use it to replace the date from DateUtils. This isn't guaranteed to work,
                 * but our testing so far has been conclusively positive.
                 *
                 * For information on a simpler API to use (on API > 18), please check out the
                 * documentation on DateFormat#getBestDateTimePattern(Locale, String)
                 * https://developer.android.com/reference/android/text/format/DateFormat.html#getBestDateTimePattern
                 */
                String localizedDayName = new SimpleDateFormat("EEEE").format(localDate);
                return readableDate.replace(localizedDayName, dayName);
            } else {
                return readableDate;
            }
        } else if (dayNumber < currentDayNumber + 7) {
            // date is less than a week format: name of day (i.e. "Tuesday")
            return getDayName(context, localDate);
        } else {
            int flags = DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_NO_YEAR
                    | DateUtils.FORMAT_ABBREV_ALL
                    | DateUtils.FORMAT_SHOW_WEEKDAY;

            return DateUtils.formatDateTime(context, localDate, flags);
        }
    }

    /**
     * Convert given date (in UTC timezone) to date in local timezone
     *
     * @param utcDate The UTC datetime to convert to a local datetime, in milliseconds
     * @return The local date (the UTC datetime - the TimeZone offset) in milliseconds
     */
    public static long getLocalDateFromUTC(long utcDate) {
        TimeZone tz = TimeZone.getDefault();
        long gmtOffset = tz.getOffset(utcDate);

        return utcDate - gmtOffset;
    }

    /**
     * Returns milliseconds (UTC time) for today at midnight in local time zone
     * i.e., in California on September 20th, 2016 at 6:30 PM will return 1474329600000
     * Epoch converter gives time stamp at 8:00 PM on September 19th local time
     * But in GMT, September 20th, 2016 at midnight is correct
     *
     * i.e., in Hong Kong on September 20th, 2016 at 6:30 PM will return 1474329600000
     * An Epoch time converter will not get midnight for your local time zone
     * Keep in mind looking at GMT date
     *
     * ALWAYS returns date at midnight (in GMT time) for time zone you are currently in
     * GMT date will always represent your date
     *
     * Since UTC / GMT time are standard for all time zones in the world,
     * use it to normalize dates that are stored in database
     * Adjust for current time zone using time zone offsets when getting from database
     *
     * @return milliseconds (UTC / GMT) for today at midnight in local time zone
     */
    public static long getNormalizedUtcDateForToday() {
        /*
         * Milliseconds that have elapsed since January 1st, 1970 at midnight in GMT time zone
         */
        long utcNowMillis = System.currentTimeMillis();

        /*
         * TimeZone represents device current time zone
         * Way to acquire offset for local time from a UTC time stamp
         */
        TimeZone currentTimeZone = TimeZone.getDefault();

        /*
         * getOffset returns ms to add to UTC to get elapsed time since epoch for current time zone
         * Pass current UTC time to account for daylight savings time
         */
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);

        /*
         * Number of milliseconds since January 1, 1970 (GMT)
         * UTC time is measured in milliseconds from January 1, 1970 at midnight from GMT time zone
         * Depending time zone, time since January 1, 1970 at midnight (GMT) will be greater or smaller
         */
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;

        // Convert milliseconds to days disregarding fractional days
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);

        /*
         * Convert back to milliseconds, time stamp for today at midnight in GMT
         * Need to account for local time zone offsets when extracting this info from database
         */
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);

        return normalizedUtcMidnightMillis;
    }

    /**
     * Convert local date to date in UTC time
     *
     * @param localDate The local datetime to convert to a UTC datetime, in milliseconds
     * @return The UTC date (the local datetime + the TimeZone offset) in milliseconds
     */
    public static long getUTCDateFromLocal(long localDate) {
        TimeZone tz = TimeZone.getDefault();
        long gmtOffset = tz.getOffset(localDate);

        return localDate + gmtOffset;
    }

    /**
     * In order to ensure consistent inserts into WeatherProvider, we check that dates have been
     * normalized before they are inserted. If they are not normalized, we don't want to accept
     * them, and leave it up to the caller to throw an IllegalArgumentException.
     *
     * @param millisSinceEpoch Milliseconds since January 1, 1970 at midnight
     *
     * @return true if the date represents the beginning of a day in Unix time, false otherwise
     */
    public static boolean isDateNormalized(long millisSinceEpoch) {
        boolean isDateNormalized = false;
        if (millisSinceEpoch % DAY_IN_MILLIS == 0) {
            isDateNormalized = true;
        }

        return isDateNormalized;
    }

    /**
     * Normalize all dates that go into database to start of day in UTC time
     *
     * @param date The UTC date to normalize
     * @return The UTC date at 12 midnight
     */
    public static long normalizeDate(long date) {
        return date / DAY_IN_MILLIS * DAY_IN_MILLIS;
    }

    /**
     * Given a day, returns just the name to use for that day
     * i.e. "today", "tomorrow", "Wednesday"
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds (local time)
     * @return the string day of the week
     */
    private static String getDayName(Context context, long dateInMillis) {
        /*
         * If the date is today, return the localized version of "Today" instead of the actual
         * day name.
         */
        long dayNumber = getDayNumber(dateInMillis);
        long currentDayNumber = getDayNumber(System.currentTimeMillis());
        if (dayNumber == currentDayNumber) {
            return context.getString(R.string.today);
        } else if (dayNumber == currentDayNumber + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            /*
             * Otherwise, if the day is not today, the format is just the day of the week
             * (i.e. "Wednesday")
             */
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");

            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Returns a date string in the format specified, which shows a date, without a year,
     * abbreviated, showing the full weekday
     *
     * @param context      Used by DateUtils to formate the date in the current locale
     * @param timeInMillis Time in milliseconds since the epoch (local time)
     * @return The formatted date string
     */
    private static String getReadableDateString(Context context, long timeInMillis) {
        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY;

        return DateUtils.formatDateTime(context, timeInMillis, flags);
    }
}
