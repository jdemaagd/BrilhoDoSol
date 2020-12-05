package com.jdemaagd.brilhodosol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jdemaagd.brilhodosol.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.utils.NetworkUtils;
import com.jdemaagd.brilhodosol.utils.JsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements ForecastAdapterOnClickHandler, LoaderCallbacks<String[]> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;

    private static final int FORECAST_LOADER_ID = 0;

    /**
     * Handle RecyclerView item clicks
     *
     * @param weatherForDay The weather for the day that was clicked
     */
    @Override
    public void onClick(String weatherForDay) {
        Context context = this;

        Class destinationClass = DetailsActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rv_forecast);

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change child layout size in RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        int loaderId = FORECAST_LOADER_ID;

        /*
         * Implemented LoaderCallbacks interface with type of String array (implements LoaderCallbacks<String[]>)
         * The variable callback is passed to call to initLoader below
         * When loaderManager has something to notify, it will do so through this callback
         */
        LoaderCallbacks<String[]> callback = MainActivity.this;

        /*
         * The second parameter of the initLoader method below is a Bundle
         * Optionally can pass a Bundle to initLoader that can be accessed from within onCreateLoader callback
         */
        Bundle bundleForLoader = null;

        // Ensures a loader is initialized and active
        LoaderManager.getInstance(this).initLoader(loaderId, bundleForLoader, callback);
    }

    /**
     * Instantiate and return a new Loader for given ID
     *
     * @param id The ID whose loader is to be created
     * @param loaderArgs Any arguments supplied by the caller
     * @return Return a new Loader instance that is ready to start loading
     */
    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<String[]>(this) {

            // Cache weather data
            String[] mWeatherData = null;

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data
             */
            @Override
            protected void onStartLoading() {
                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * Load and parse JSON data from OpenWeatherMap in background
             *
             * @return Weather data from OpenWeatherMap as an array of Strings
             */
            @Override
            public String[] loadInBackground() {

                String locationQuery = AppPreferences
                        .getPreferredWeatherLocation(MainActivity.this);

                URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = JsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Send result of load to registered listener
             *
             * @param data The result of the load
             */
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast, menu);

        return true;
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable
     * Application should at this point remove any references it has to Loader data
     *
     * @param loader The Loader that is being reset
     */
    @Override
    public void onLoaderReset(Loader<String[]> loader) {
        // Required to Override this to implement LoaderCallbacks<String> interface
    }

    /**
     * Called when a previously created loader has finished its load
     *
     * @param loader The Loader that has finished
     * @param data The data generated by the Loader
     */
    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);

        if (null == data) {
            showErrorMessage();
        } else {
            showWeatherDataView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            invalidateData();
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);

            return true;
        }

        if (id == R.id.action_map) {
            openLocationInMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void invalidateData() {
        mForecastAdapter.setWeatherData(null);
    }

    /**
     * Use URI scheme for showing a location found on map
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     */
    private void openLocationInMap() {
        String addressString = "343 South Fifth Avenue, Ann Arbor, MI 48104";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Could not call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }

    /**
     * Show error message and hide weather data
     * <p>
     * Since it is okay to redundantly set visibility of a View
     * do not need to check whether each view is currently in/visible
     */
    private void showErrorMessage() {
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    /**
     * Show View for weather data visible and hide error message view
     * <p>
     * Since it is okay to redundantly set visibility of a View
     * do not need to check whether each view is currently in/visible
     */
    private void showWeatherDataView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }
}