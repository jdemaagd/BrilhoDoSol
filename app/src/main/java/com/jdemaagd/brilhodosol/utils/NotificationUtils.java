package com.jdemaagd.brilhodosol.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.jdemaagd.brilhodosol.DetailsActivity;
import com.jdemaagd.brilhodosol.R;
import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;

public class NotificationUtils {

    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
    };

    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    private static final int WEATHER_NOTIFICATION_ID = 3004;

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfNewWeather(Context context) {

        Uri todayWeatherUri = WeatherEntry
                .buildWeatherUriWithDate(AppDateUtils.normalizeDate(System.currentTimeMillis()));

        Cursor todayWeatherCursor = context.getContentResolver().query(
                todayWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);

        if (todayWeatherCursor.moveToFirst()) {
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = WeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);

            String notificationTitle = context.getString(R.string.app_name);

            String notificationText = getNotificationText(context, weatherId, high, low);

            int smallArtResourceId = WeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            Intent detailIntentForToday = new Intent(context, DetailsActivity.class);
            detailIntentForToday.setData(todayWeatherUri);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notificationBuilder = new Notification.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder);

            AppPreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }

        todayWeatherCursor.close();
    }

    /**
     * Get notification text
     * <p>
     * The String returned from this method will look something like this:
     * <p>
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day forecast
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {
        String shortDescription = WeatherUtils
                .getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        String notificationText = String.format(notificationFormat,
                shortDescription,
                WeatherUtils.formatTemperature(context, high),
                WeatherUtils.formatTemperature(context, low));

        return notificationText;
    }
}