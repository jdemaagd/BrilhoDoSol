package com.jdemaagd.brilhodosol.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.jdemaagd.brilhodosol.R;

public class AppPreferences {

    public static final String PREF_CITY_NAME = "Ann Arbor";

    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";

    private static final String DEFAULT_WEATHER_LOCATION = "Ann Arbor, MI 48104";
    private static final double[] DEFAULT_WEATHER_COORDINATES = {42.278210, -83.745670};

    private static final String DEFAULT_MAP_LOCATION =
            "343 South Fifth Avenue, Ann Arbor, MI 48104";

    /**
     * Get user notifications preference
     *
     * @param context Used to access SharedPreferences
     * @return true if user prefers to see notifications
     */
    public static boolean areNotificationsEnabled(Context context) {
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);

        boolean shouldDisplayNotificationsByDefault = context
                .getResources()
                .getBoolean(R.bool.show_notifications_by_default);

        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);

        boolean shouldDisplayNotifications = prefs
                .getBoolean(displayNotificationsKey, shouldDisplayNotificationsByDefault);

        return shouldDisplayNotifications;
    }

    public static double[] getDefaultWeatherCoordinates() {
        return DEFAULT_WEATHER_COORDINATES;
    }

    /**
     * Returns elapsed time in milliseconds since last notification was shown
     *
     * @param context Used to access SharedPreferences as well as use other utility methods
     * @return Elapsed time in milliseconds since the last notification was shown
     */
    public static long getElapsedTimeSinceLastNotification(Context context) {
        long lastNotificationTimeMillis =
                AppPreferences.getLastNotificationTimeInMillis(context);
        long timeSinceLastNotification = System.currentTimeMillis() - lastNotificationTimeMillis;

        return timeSinceLastNotification;
    }

    /**
     * Returns the last time that a notification was shown (in UNIX time)
     *
     * @param context Used to access SharedPreferences
     * @return UNIX time of when the last notification was shown
     */
    public static long getLastNotificationTimeInMillis(Context context) {
        String lastNotificationKey = context.getString(R.string.pref_last_notification);

        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        long lastNotificationTime = prefs.getLong(lastNotificationKey, 0);

        return lastNotificationTime;
    }

    /**
     * Returns location coordinates associated with location
     * Note: coordinates may not be set, which results in (0,0) being returned
     * (0,0) is in middle of ocean off west coast of Africa
     *
     * @param context Used to get the SharedPreferences
     * @return An array containing the two coordinate values
     */
    public static double[] getLocationCoordinates(Context context) {
        return getDefaultWeatherCoordinates();
    }

    /**
     * Returns the location currently set in Preferences
     *
     * @param context Context used to get the SharedPreferences
     * @return Location The current user has set in SharedPreferences
     */
    public static String getPreferredWeatherLocation(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String keyForLocation = context.getString(R.string.pref_location_key);
        String defaultLocation = context.getString(R.string.pref_location_default);

        return prefs.getString(keyForLocation, defaultLocation);
    }

    /**
     * Returns true if latitude/longitude values are available
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are set
     */
    public static boolean isLocationLatLonAvailable(Context context) {
        return false;
    }

    /**
     * Returns true if the user has selected metric temperature display
     *
     * @param context Context used to get the SharedPreferences
     * @return true If metric display should be used
     */
    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_metric);
        String preferredUnits = prefs.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_metric);

        boolean userPrefersMetric;

        if (metric.equals(preferredUnits)) {
            userPrefersMetric = true;
        } else {
            userPrefersMetric = false;
        }

        return userPrefersMetric;
    }

    /**
     * Resets stored location coordinates
     *
     * @param context Context used to get the SharedPreferences
     */
    public static void resetLocationCoordinates(Context context) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREF_COORD_LAT);
        editor.remove(PREF_COORD_LONG);
        editor.apply();
    }

    /**
     * Saves time that notification is shown
     *
     * @param context Used to access SharedPreferences
     * @param timeOfNotification Time of last notification to save (in UNIX time)
     */
    public static void saveLastNotificationTime(Context context, long timeOfNotification) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        editor.putLong(lastNotificationKey, timeOfNotification);
        editor.apply();
    }

    /**
     * Handle setting location details in Preferences
     *
     * @param context  Context used to get the SharedPreferences
     * @param lat      the latitude of the city
     * @param lon      the longitude of the city
     */
    public static void setLocationDetails(Context context, double lat, double lon) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(PREF_COORD_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(PREF_COORD_LONG, Double.doubleToRawLongBits(lon));
        editor.apply();
    }
}
