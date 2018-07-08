package karsai.laszlo.bringcloser.module;

import javax.inject.Singleton;

import dagger.Component;
import karsai.laszlo.bringcloser.ui.screens.main.MainActivity;

/**
 * Created by Laci on 04/07/2018.
 */

@Singleton
@Component(modules = {MainModule.class})
public interface MainComponent {
    void inject(MainActivity activity);
}
