package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.fragment.ConnectedPeopleFragment;
import karsai.laszlo.bringcloser.fragment.RequestFromPeopleFragment;
import karsai.laszlo.bringcloser.fragment.RequestToPeopleFragment;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;

/**
 * Created by Laci on 28/05/2018.
 */

public class ConnectionFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private static final int CONNECTION_TYPE_NUMBER = 3;
    private Context mContext;

    public ConnectionFragmentPagerAdapter(
            FragmentManager fm,
            Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ConnectedPeopleFragment();
        } else if (position == 1){
            return new RequestToPeopleFragment();
        } else {
            return new RequestFromPeopleFragment();
        }
    }

    @Override
    public int getCount() {
        return CONNECTION_TYPE_NUMBER;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            /*return getPageTitleWithCount(
                    mContext.getResources().getString(R.string.connected_people_fragment_title),
                    mConnectedConnectionDetailList.size()
            );*/
            return mContext.getResources().getString(R.string.connected_people_fragment_title);
        } else if (position == 1){
            /*return getPageTitleWithCount(
                    mContext.getResources().getString(R.string.request_to_people_fragment_title),
                    mRequestToConnectionDetailList.size()
            );*/
            return mContext.getResources().getString(R.string.request_to_people_fragment_title);
        } else {
            /*return getPageTitleWithCount(
                    mContext.getResources().getString(R.string.request_from_people_fragment_title),
                    mRequestFromConnectionDetailList.size()
            );*/
            return mContext.getResources().getString(R.string.request_from_people_fragment_title);
        }
    }

    /*private String getPageTitleWithCount(String title, int count) {
        return new StringBuilder().append(title).append(" (").append(count).append(")").toString();
    }*/
}
