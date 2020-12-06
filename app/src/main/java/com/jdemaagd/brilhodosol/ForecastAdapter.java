package com.jdemaagd.brilhodosol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jdemaagd.brilhodosol.utils.AppDateUtils;
import com.jdemaagd.brilhodosol.utils.WeatherUtils;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link androidx.recyclerview.widget.RecyclerView}.
 */
class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private final Context mContext;

    // interface to handle clicks on items within this Adapter
    final private ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages
     */
    public interface ForecastAdapterOnClickHandler {
        void onClick(long date);
    }

    private Cursor mCursor;

    /**
     * ForecastAdapter Constructor
     *
     * @param context Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * Called when each new ViewHolder is created
     * Happens when RecyclerView is laid out
     * Enough ViewHolders will be created to fill screen and allow for scrolling
     *
     * @param viewGroup ViewGroup that these ViewHolders are contained within
     * @param viewType  If your RecyclerView has more than one type of item
     *                  can use this viewType integer to provide a different layout
     *     See {@link androidx.recyclerview.widget.RecyclerView.Adapter#getItemViewType(int)}
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.forecast_item, viewGroup, false);

        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at specified position
     * Update contents of ViewHolder to display weather details for this position
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent
     *                                  contents of item at given position in data set
     * @param position                  The position of the item within the adapter data set
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = AppDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = WeatherUtils.getStringForWeatherCondition(mContext, weatherId);

        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        String highAndLowTemperature =
                WeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);

        String weatherSummary = dateString + " - " + description + " - " + highAndLowTemperature;

        forecastAdapterViewHolder.weatherSummary.setText(weatherSummary);
    }

    /**
     * Returns count of items to display
     * Used behind scenes to help layout our Views and for animations
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;

        return mCursor.getCount();
    }

    /**
     * Swaps cursor used by ForecastAdapter for its weather data
     * Called by MainActivity after a load has finished,
     *      as well as when Loader responsible for loading weather data is reset
     * When called we assume we have a completely new set of data
     *      so we call notifyDataSetChanged to tell RecyclerView to update
     *
     * @param newCursor new cursor to use as ForecastAdapter data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder is required part of pattern for RecyclerViews
     * Behaves as cache of child views for forecast item
     * Also convenient place to set an OnClickListener (has access to adapter and views)
     */
    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView weatherSummary;

        ForecastAdapterViewHolder(View view) {
            super(view);

            weatherSummary = view.findViewById(R.id.tv_weather_data);

            view.setOnClickListener(this);
        }

        /**
         * Called by child views during a click
         * Fetch date that has been selected then call onClick handler registered with this adapter
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
}