package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.fragment.ConnectedPeopleFragment;
import karsai.laszlo.bringcloser.fragment.ConnectionChatFragment;
import karsai.laszlo.bringcloser.fragment.ConnectionWishFragment;
import karsai.laszlo.bringcloser.fragment.RequestFromPeopleFragment;
import karsai.laszlo.bringcloser.fragment.RequestToPeopleFragment;

/**
 * Created by Laci on 28/05/2018.
 */

public class ConnectionDetailFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private static final int CONNECTION_TYPE_NUMBER = 4;
    private Context mContext;

    public ConnectionDetailFragmentPagerAdapter(
            FragmentManager fm,
            Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ConnectionChatFragment();
        } else {
            return new ConnectionWishFragment();
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
            return "Chat";
        } else {
            /*return getPageTitleWithCount(
                    mContext.getResources().getString(R.string.request_to_people_fragment_title),
                    mRequestToConnectionDetailList.size()
            );*/
            return "Wish";
        }
    }

    /*private String getPageTitleWithCount(String title, int count) {
        return new StringBuilder().append(title).append(" (").append(count).append(")").toString();
    }*/
}
