package karsai.laszlo.bringcloser.module;

import dagger.Module;
import dagger.Provides;
import karsai.laszlo.bringcloser.ui.screens.main.MainActivityPresenter;
import karsai.laszlo.bringcloser.ui.screens.main.MainActivityPresenterImpl;

/**
 * Created by Laci on 04/07/2018.
 */
@Module
public class MainModule {

    @Provides
    public MainActivityPresenter provideMainActivityPresenter() {
        return new MainActivityPresenterImpl();
    }
}
