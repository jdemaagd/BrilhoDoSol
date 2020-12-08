package com.jdemaagd.brilhodosol.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

public class BrilhodoSolFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    /**
     * Offload work to another thread of execution as soon as possible
     * <p>
     * Called by Job Dispatcher to tell us we should start our job
     * This is run on app main thread, need to offload work to background thread
     *
     * @return whether there is more work remaining
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mFetchWeatherTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                BrilhodoSolSyncTask.syncWeather(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }

    /**
     * Called when scheduling engine has decided to interrupt execution of running job
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchWeatherTask != null) {
            mFetchWeatherTask.cancel(true);
        }

        return true;
    }
}