package com.jdemaagd.brilhodosol.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;

import java.util.concurrent.TimeUnit;

public class BrilhodoSolSyncUtils {

    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized;

    private static final String BRILHODOSOL_SYNC_TAG = "brilhodosol-sync";

    /**
     * Schedules repeating sync of weather data via FirebaseJobDispatcher
     * @param context Context used to create GooglePlayDriver that powers FirebaseJobDispatcher
     */
    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job syncJob = dispatcher.newJobBuilder()
                .setService(BrilhodoSolFirebaseJobService.class)
                .setTag(BRILHODOSOL_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncJob);
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(@NonNull final Context context) {

        if (sInitialized) return;

        sInitialized = true;

        scheduleFirebaseJobDispatcherSync(context);

        Thread checkForEmpty = new Thread(() -> {
            Uri forecastQueryUri = WeatherEntry.CONTENT_URI;

            String[] projectionColumns = {WeatherEntry._ID};
            String selectionStatement = WeatherEntry
                    .getSqlSelectForTodayOnwards();

            Cursor cursor = context.getContentResolver().query(
                    forecastQueryUri,
                    projectionColumns,
                    selectionStatement,
                    null,
                    null);

            if (null == cursor || cursor.getCount() == 0) {
                startImmediateSync(context);
            }

            cursor.close();
        });

        checkForEmpty.start();
    }

    /**
     * Sync immediately using an IntentService for asynchronous execution
     *
     * @param context Context used to start IntentService for sync
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, BrilhodoSolSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}