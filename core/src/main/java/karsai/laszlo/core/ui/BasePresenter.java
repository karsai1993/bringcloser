package karsai.laszlo.core.ui;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

/**
 * Created by Laci on 04/07/2018.
 */

public abstract class BasePresenter<V extends BaseView> implements Presenter<V> {

    private List<Subscription> subscriptions = new ArrayList<>();
    protected V view;

    @Override
    public void attachView(V view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        unsubscribeAll();
        view = null;
    }

    public void subscribe(Subscription subscription) {
        subscriptions.add(subscription);
    }

    protected void unsubscribeAll() {
        for (Subscription subscription : subscriptions) {
            if (!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
        subscriptions.clear();
    }
}
