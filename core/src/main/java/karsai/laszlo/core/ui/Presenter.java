package karsai.laszlo.core.ui;

import rx.Subscription;

/**
 * Created by Laci on 04/07/2018.
 */

public interface Presenter<V extends BaseView> {

    void attachView(V view);

    void detachView();

    void subscribe(Subscription subscription);
}
