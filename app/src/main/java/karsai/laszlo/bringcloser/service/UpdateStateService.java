package karsai.laszlo.bringcloser.service;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.lang.ref.WeakReference;

import karsai.laszlo.bringcloser.background.UpdateStateAsyncTask;

/**
 * Service to update database with the new token
 */
public class UpdateStateService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        new UpdateStateAsyncTask(
                new WeakReference<UpdateStateService>(UpdateStateService.this),
                job).execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
