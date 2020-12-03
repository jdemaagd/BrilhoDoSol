package com.jdemaagd.brilhodosol.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public final class OpenWeatherJsonUtils {

    private static final String LOG_TAG = OpenWeatherJsonUtils.class.getSimpleName();

    private static final String OWM_DESCRIPTION = "description";
    private static final String OWM_LIST = "list";
    private static final String OWM_MAIN = "main";
    private static final String OWM_MAX = "temp_max";
    private static final String OWM_MESSAGE_CODE = "cod";
    private static final String OWM_MIN = "temp_min";
    private static final String OWM_WEATHER = "weather";

    /**
     * Parse JSON from web response
     *
     * @param forecastJsonStr JSON response from server
     * @return Array of Strings describing weather data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String[] getSimpleWeatherStringsFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        String[] parsedWeatherData = null;

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.e(LOG_TAG, "Invalid location. Please try again after verifying location.");
                    return null;
                default:
                    Log.e(LOG_TAG, "Network Error. Please try again later.");
                    return null;
            }
        }

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        parsedWeatherData = new String[weatherArray.length()];

        long localDate = System.currentTimeMillis();
        long utcDate = AppDateUtils.getUTCDateFromLocal(localDate);
        long startDay = AppDateUtils.normalizeDate(utcDate);

        for (int i = 0; i < weatherArray.length(); i++) {
            String date;
            String highAndLow;

            long dateTimeMillis;
            double high;
            double low;
            String description;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            dateTimeMillis = startDay + AppDateUtils.DAY_IN_MILLIS * i;
            date = AppDateUtils.getFriendlyDateString(context, dateTimeMillis, false);

            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject mainObject = dayForecast.getJSONObject(OWM_MAIN);
            high = mainObject.getDouble(OWM_MAX);
            low = mainObject.getDouble(OWM_MIN);
            highAndLow = WeatherUtils.formatHighLows(context, high, low);

            parsedWeatherData[i] = date + ":  " + description + ",  Temps: " + highAndLow;
        }

        return parsedWeatherData;
    }

    /**
     * Parse JSON and convert it into ContentValues that can be inserted into our database
     *
     * @param context         An application context, such as a service or activity context
     * @param forecastJsonStr The JSON to parse into ContentValues
     * @return An array of ContentValues parsed from the JSON
     */
    public static ContentValues[] getFullWeatherDataFromJson(Context context, String forecastJsonStr) {
        return null;
    }
}
