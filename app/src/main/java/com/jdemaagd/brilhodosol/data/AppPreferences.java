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
     * Handle setting location details in Preferences
     *
     * @param c        Context used to get the SharedPreferences
     * @param cityName A human-readable city name, i.e. 'Ann Arbor'
     * @param lat      The latitude of the city
     * @param lon      The longitude of the city
     */
    static public void setLocationDetails(Context c, String cityName, double lat, double lon) {

    }

    /**
     * Handle setting a new location in preferences
     *
     * @param c               Context used to get the SharedPreferences
     * @param locationSetting The location string used to request updates from the server.
     * @param lat             The latitude of the city
     * @param lon             The longitude of the city
     */
    static public void setLocation(Context c, String locationSetting, double lat, double lon) {

    }

    /**
     * Resets the stored location coordinates
     *
     * @param c Context used to get the SharedPreferences
     */
    static public void resetLocationCoordinates(Context c) {

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
     * Returns true if latitude/longitude values are available
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are set
     */
    public static boolean isLocationLatLonAvailable(Context context) {
        return false;
    }

    private static String getDefaultWeatherLocation() {
        return DEFAULT_WEATHER_LOCATION;
    }

    public static double[] getDefaultWeatherCoordinates() {
        return DEFAULT_WEATHER_COORDINATES;
    }
}
