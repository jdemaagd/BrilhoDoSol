package com.jdemaagd.brilhodosol;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

/**
 * Load SettingsFragment and handle proper behavior of up button
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            // NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }
}