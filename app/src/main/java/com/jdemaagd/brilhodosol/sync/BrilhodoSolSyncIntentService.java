package com.jdemaagd.brilhodosol.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * {@link IntentService} subclass to handle async task requests in a service on separate handler thread
 */
public class BrilhodoSolSyncIntentService extends IntentService {

    public BrilhodoSolSyncIntentService() {
        super("BrilhodoSolSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BrilhodoSolSyncTask.syncWeather(this);
    }

}