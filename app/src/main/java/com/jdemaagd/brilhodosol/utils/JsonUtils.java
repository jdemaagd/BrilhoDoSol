package com.jdemaagd.brilhodosol.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public final class JsonUtils {

    private static final String LOG_TAG = JsonUtils.class.getSimpleName();

    private static final String OWM_CITY = "city";
    private static final String OWM_COORD = "coord";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LIST = "list";
    private static final String OWM_LONGITUDE = "lon";
    private static final String OWM_MAIN = "main";
    private static final String OWM_MAX = "temp_max";
    private static final String OWM_MESSAGE_CODE = "cod";
    private static final String OWM_MIN = "temp_min";
    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";
    private static final String OWM_WIND = "wind";
    private static final String OWM_WIND_DIRECTION = "deg";
    private static final String OWM_WINDSPEED = "speed";

    /**
     * Parse JSON from web response and returns array of Strings of weather over some days
     * @param forecastJsonStr JSON response from server
     * @return Array of Strings describing weather data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // Log.d(LOG_TAG, "Please verify endpoint.");
                    return null;
                default:
                    Log.d(LOG_TAG, "Network error.");
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        AppPreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        long normalizedUtcStartDay = AppDateUtils.getNormalizedUtcDateForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double high;
            int humidity;
            double low;
            double pressure;
            int weatherId;
            double windDirection;
            double windSpeed;

            dateTimeMillis = normalizedUtcStartDay + AppDateUtils.DAY_IN_MILLIS * i;

            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);
            JSONObject mainObject = dayForecast.getJSONObject(OWM_MAIN);
            JSONObject windObject = dayForecast.getJSONObject(OWM_WIND);
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            high = mainObject.getDouble(OWM_MAX);
            humidity = mainObject.getInt(OWM_HUMIDITY);
            low = mainObject.getDouble(OWM_MIN);
            pressure = mainObject.getDouble(OWM_PRESSURE);
            windDirection = windObject.getDouble(OWM_WIND_DIRECTION);
            windSpeed = windObject.getDouble(OWM_WINDSPEED);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);

            Log.v(LOG_TAG, "Send to Content Resolver: " + weatherValues);
            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }
}
