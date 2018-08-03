package karsai.laszlo.bringcloser;

import android.support.multidex.MultiDexApplication;

import timber.log.Timber;

/**
 * Class to optimize the application start
 */
public class BringCloserApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseLog());
        }
    }
}