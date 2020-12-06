package com.jdemaagd.brilhodosol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jdemaagd.brilhodosol.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;
import com.jdemaagd.brilhodosol.utils.FakeDataUtils;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapterOnClickHandler, LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    private static final int FORECAST_LOADER_ID = 11;

    private ForecastAdapter mForecastAdapter;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;

    private int mPosition = RecyclerView.NO_POSITION;

    /**
     * Respond to clicks from list
     *
     * @param date Normalized UTC time that represents the local date of the weather in GMT time
     * @see WeatherEntry#COLUMN_DATE
     */
    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailsActivity.class);

        Uri uriForDateClicked = WeatherEntry.buildWeatherUriWithDate(date);

        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0f);

        FakeDataUtils.insertFakeData(this);

        mRecyclerView = findViewById(R.id.rv_forecast);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change child layout size in RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this, this);
        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER_ID, null, this);
    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case FORECAST_LOADER_ID:
                // URI for all rows of weather data in our weather table
                Uri forecastQueryUri = WeatherEntry.CONTENT_URI;

                // Sort order: Ascending by date
                String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";

                // A SELECTION in SQL declares which rows to return
                // Get all weather data from today onwards that is stored in weather table
                String selection = WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast, menu);

        return true;
    }

    /**
     * Called when Loader has finished loading its data
     *
     * @param loader The Loader that has finished
     * @param data   The data generated by the Loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;

        mRecyclerView.smoothScrollToPosition(mPosition);

        if (data.getCount() != 0) showWeatherDataView();
    }

    /**
     * Called when previously created loader is being reset
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);
            return true;
        }
        if (id == R.id.action_map) {
            openLocationInMap();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Use URI scheme for showing a location found on map
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     */
    private void openLocationInMap() {
        String addressString = AppPreferences.getPreferredWeatherLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    /**
     * Show loading indicator and hide weather/error message
     * <p>
     * Since it is okay to redundantly set visibility of a View
     * do not need to check whether each view is currently in/visible
     */
    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Show View for weather data visible and hide error message view
     * <p>
     * Since it is okay to redundantly set visibility of a View
     * do not need to check whether each view is currently in/visible
     */
    private void showWeatherDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}