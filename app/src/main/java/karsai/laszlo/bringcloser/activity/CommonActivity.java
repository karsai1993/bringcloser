package karsai.laszlo.bringcloser.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import karsai.laszlo.bringcloser.utils.NetworkUtils;

/**
 * Created by Laci on 04/07/2018.
 * Activity to give a common action for no internet connection
 */

public abstract class CommonActivity extends AppCompatActivity {

    private TextView mNoInternetAlertTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoInternetAlertTextView = getNoInternetAlertTextView();
    }

    protected abstract TextView getNoInternetAlertTextView();

    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNoInternetAlertTextView != null) {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    mNoInternetAlertTextView.setVisibility(View.VISIBLE);
                } else {
                    mNoInternetAlertTextView.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkChangeReceiver);
    }
}
