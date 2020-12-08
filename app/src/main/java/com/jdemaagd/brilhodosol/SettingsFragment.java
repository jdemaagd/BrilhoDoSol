package com.jdemaagd.brilhodosol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.jdemaagd.brilhodosol.data.AppPreferences;
import com.jdemaagd.brilhodosol.data.WeatherContract.WeatherEntry;
import com.jdemaagd.brilhodosol.sync.BrilhodoSolSyncUtils;

/**
 * SettingsFragment serves as display for all of user settings
 * User will be able to change their preference for units of measurement
 *      from metric to imperial,
 *      set their preferred weather location,
 *      and indicate whether or not they would like to see notifications
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = prefScreen.getPreference(i);
            if (!(pref instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, value);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        if (key.equals(getString(R.string.pref_location_key))) {
            AppPreferences.resetLocationCoordinates(activity);
            BrilhodoSolSyncUtils.startImmediateSync(activity);
        } else if (key.equals(getString(R.string.pref_units_key))) {
            activity.getContentResolver().notifyChange(WeatherEntry.CONTENT_URI, null);
        }

        Preference preference = findPreference(key);

        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }
}