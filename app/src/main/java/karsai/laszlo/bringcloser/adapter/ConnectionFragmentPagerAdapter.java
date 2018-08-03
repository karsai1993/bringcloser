package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.fragment.ConnectedPeopleFragment;
import karsai.laszlo.bringcloser.fragment.RequestFromPeopleFragment;
import karsai.laszlo.bringcloser.fragment.RequestToPeopleFragment;

/**
 * Created by Laci on 28/05/2018.
 * Adapter to handle connection view pager related information
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
            return mContext.getResources().getString(R.string.connected_people_fragment_title);
        } else if (position == 1){
            return mContext.getResources().getString(R.string.request_to_people_fragment_title);
        } else {
            return mContext.getResources().getString(R.string.request_from_people_fragment_title);
        }
    }
}
