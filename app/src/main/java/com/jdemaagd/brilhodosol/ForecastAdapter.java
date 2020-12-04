package com.jdemaagd.brilhodosol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts to
 *      {@link androidx.recyclerview.widget.RecyclerView}
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private String[] mWeatherData;

    public ForecastAdapter() { }

    /**
     * Cache of children views for forecast list
     */
    public class ForecastViewHolder extends RecyclerView.ViewHolder {
        public final TextView mWeatherTextView;

        public ForecastViewHolder(View view) {
            super(view);
            mWeatherTextView = (TextView) view.findViewById(R.id.tv_weather_data);
        }
    }

    /**
     * Called when each new ViewHolder is created
     * Happens when RecyclerView is laid out
     * Enough ViewHolders will be created to fill screen and allow for scrolling
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within
     * @param viewType  If RecyclerView has more than one type of item,
     *                  can use this viewType integer to provide a different layout
     *     See {@link androidx.recyclerview.widget.RecyclerView.Adapter#getItemViewType(int)}
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.forecast_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new ForecastViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at specified position
     * Update contents of ViewHolder to display weather details for this position
     *
     * @param viewHolder ViewHolder which should be updated
     * @param position                  position of item within adapter data set
     */
    @Override
    public void onBindViewHolder(ForecastViewHolder viewHolder, int position) {
        String weatherForThisDay = mWeatherData[position];
        viewHolder.mWeatherTextView.setText(weatherForThisDay);
    }

    /**
     * Return count of items to display
     * Used behind scenes to help layoutViews and for animations
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mWeatherData) return 0;
        return mWeatherData.length;
    }

    /**
     * Set weather forecast on ForecastAdapter
     * When getting new data from web but do not need create new ForecastAdapter
     *
     * @param weatherData The new weather data to be displayed
     */
    public void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }
}
