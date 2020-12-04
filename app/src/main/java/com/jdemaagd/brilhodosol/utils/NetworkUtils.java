package com.jdemaagd.brilhodosol.utils;

import android.net.Uri;
import android.util.Log;

import com.jdemaagd.brilhodosol.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String OWM_BASE_URL = BuildConfig.OWM_BASE_URL;

    private static final String format = "json";
    private static final String units = "metric";
    private static final int numDays = 14;

    private static final String FORMAT_PARAM = "mode";
    private static final String UNITS_PARAM = "units";
    private static final String DAYS_PARAM = "cnt";

    final static String QUERY_PARAM = "q";
    final static String LAT_PARAM = "lat";
    final static String LON_PARAM = "lon";
    final static String API_PARAM = "appid";
    final static String API_KEY = BuildConfig.OWM_API_KEY;

    /**
     * Build URL via location
     *
     * @param locationQuery The location that will be queried for
     * @return The URL to use to query the weather server
     */
    public static URL buildUrl(String locationQuery) {

        Uri builtUri = Uri.parse(OWM_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(API_PARAM, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Log.v(TAG, "Built URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response
     *
     * @param url The URL to fetch the HTTP response from
     * @return The contents of the HTTP response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
