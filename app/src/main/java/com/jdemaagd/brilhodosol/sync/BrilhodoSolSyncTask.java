package com.jdemaagd.brilhodosol.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;
import com.jdemaagd.brilhodosol.utils.JsonUtils;
import com.jdemaagd.brilhodosol.utils.NetworkUtils;
import com.jdemaagd.brilhodosol.utils.NotificationUtils;

import java.net.URL;

public class BrilhodoSolSyncTask {

    private static final String LOG_TAG = BrilhodoSolSyncTask.class.getSimpleName();

    /**
     * Performs network request to insert new weather info into ContentProvider
     * Notify user new weather has been loaded if enabled
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncWeather(Context context) {

        try {
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            ContentValues[] weatherValues = JsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if (weatherValues != null && weatherValues.length != 0) {
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.delete(
                        WeatherEntry.CONTENT_URI,
                        null,
                        null);
                contentResolver.bulkInsert(
                        WeatherEntry.CONTENT_URI,
                        weatherValues);

                boolean notificationsEnabled = AppPreferences.areNotificationsEnabled(context);

                long timeSinceLastNotification = AppPreferences
                        .getElapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "syncWeather Failed. Please try again later");
            e.printStackTrace();
        }
    }
}