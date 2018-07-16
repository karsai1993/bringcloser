package karsai.laszlo.bringcloser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import karsai.laszlo.bringcloser.module.DaggerMainComponent;
import karsai.laszlo.bringcloser.module.MainComponent;
import karsai.laszlo.bringcloser.module.MainModule;

public class BringCloserApp extends MultiDexApplication {

    private MainComponent mMyComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mMyComponent = createMyComponent();
    }

    public MainComponent getMyComponent() {
        return mMyComponent;
    }

    private MainComponent createMyComponent() {
        return DaggerMainComponent
                .builder()
                .mainModule(new MainModule())
                .build();
    }
}