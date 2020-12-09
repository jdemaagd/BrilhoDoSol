package com.jdemaagd.brilhodosol;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;
import com.jdemaagd.brilhodosol.databinding.ActivityDetailsBinding;
import com.jdemaagd.brilhodosol.utils.AppDateUtils;
import com.jdemaagd.brilhodosol.utils.WeatherUtils;

public class DetailsActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SBrilhoDoSol";

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    private static final int ID_DETAIL_LOADER = 353;

    private String mForecastSummary;

    private Uri mUri;

    private ActivityDetailsBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        mUri = getIntent().getData();
        if (mUri == null)
            throw new NullPointerException("URI for DetailActivity cannot be null");

        LoaderManager.getInstance(this).initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create Forecast intent for sharing
     *
     * @return The Intent to use to start our share
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        return shareIntent;
    }

    /**
     * Creates/returns CursorLoader that loads data for our URI and stores it in Cursor
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
        switch (loaderId) {
            case ID_DETAIL_LOADER:
                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Runs on  main thread when a load is complete
     * If initLoader is called and LoaderManager already has completed a previous load
     * for this Loader, onLoadFinished will be called immediately
     * Within onLoadFinished, bind data to our views so user can see
     * details of the weather on the date they selected from forecast
     *
     * @param loader The cursor loader that finished
     * @param data   The cursor that is being returned
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }

        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
        mDetailBinding.primaryInfo.ivWeatherIcon.setImageResource(weatherImageId);

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = AppDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDetailBinding.primaryInfo.tvDate.setText(dateText);

        String description = WeatherUtils.getStringForWeatherCondition(this, weatherId);
        String descriptionA11y = getString(R.string.a11y_forecast, description);
        mDetailBinding.primaryInfo.tvWeatherDesc.setText(description);
        mDetailBinding.primaryInfo.tvWeatherDesc.setContentDescription(descriptionA11y);
        mDetailBinding.primaryInfo.ivWeatherIcon.setContentDescription(descriptionA11y);

        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = WeatherUtils.formatTemperature(this, highInCelsius);
        String highA11y = getString(R.string.a11y_high_temp, highString);
        mDetailBinding.primaryInfo.tvHighTemp.setText(highString);
        mDetailBinding.primaryInfo.tvHighTemp.setContentDescription(highA11y);

        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = WeatherUtils.formatTemperature(this, lowInCelsius);
        String lowA11y = getString(R.string.a11y_low_temp, lowString);
        mDetailBinding.primaryInfo.tvLowTemp.setText(lowString);
        mDetailBinding.primaryInfo.tvLowTemp.setContentDescription(lowA11y);

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);
        mDetailBinding.extraDetails.tvHumidity.setText(humidityString);
        mDetailBinding.extraDetails.tvHumidity.setContentDescription(humidityA11y);
        mDetailBinding.extraDetails.tvHumidityLabel.setContentDescription(humidityA11y);

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = WeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        String windA11y = getString(R.string.a11y_wind, windString);
        mDetailBinding.extraDetails.tvWindMeasurement.setText(windString);
        mDetailBinding.extraDetails.tvWindMeasurement.setContentDescription(windA11y);
        mDetailBinding.extraDetails.tvWindLabel.setContentDescription(windA11y);

        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);
        mDetailBinding.extraDetails.tvPressure.setText(pressureString);
        mDetailBinding.extraDetails.tvPressure.setContentDescription(pressureA11y);
        mDetailBinding.extraDetails.tvPressureLabel.setContentDescription(pressureA11y);

        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    /**
     * Called when a previously created loader is being reset
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}