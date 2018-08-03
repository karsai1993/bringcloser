package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.fragment.ConnectionChatFragment;
import karsai.laszlo.bringcloser.fragment.ConnectionEventFragment;
import karsai.laszlo.bringcloser.fragment.ConnectionThoughtFragment;
import karsai.laszlo.bringcloser.fragment.ConnectionWishFragment;
import karsai.laszlo.bringcloser.model.ConnectionDetail;

/**
 * Created by Laci on 28/05/2018.
 * Adapter to handle connection detail view pager related information
 */
public class ConnectionDetailFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private static final int CONNECTION_TYPE_NUMBER = 4;
    private Context mContext;
    private ConnectionDetail mConnectionDetail;

    public ConnectionDetailFragmentPagerAdapter(
            FragmentManager fm,
            Context context,
            ConnectionDetail connectionDetail) {
        super(fm);
        this.mContext = context;
        this.mConnectionDetail = connectionDetail;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        Bundle bundle;
        if (position == 0) {
            fragment = new ConnectionChatFragment();
            bundle = new Bundle();
            bundle.putParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY, mConnectionDetail);
            fragment.setArguments(bundle);
            return fragment;
        } else if (position == 1) {
            fragment = new ConnectionWishFragment();
            bundle = new Bundle();
            bundle.putParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY, mConnectionDetail);
            fragment.setArguments(bundle);
            return fragment;
        } else if (position == 2) {
            fragment = new ConnectionEventFragment();
            bundle = new Bundle();
            bundle.putParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY, mConnectionDetail);
            fragment.setArguments(bundle);
            return fragment;
        } else {
            fragment = new ConnectionThoughtFragment();
            bundle = new Bundle();
            bundle.putParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY, mConnectionDetail);
            fragment.setArguments(bundle);
            return fragment;
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
            return mContext.getResources().getString(R.string.connection_detail_chat_fragment_title);
        } else if (position == 1) {
            return mContext.getResources().getString(R.string.connection_detail_wish_fragment_title);
        } else if (position == 2) {
            return mContext.getResources().getString(R.string.connection_detail_event_fragment_title);
        } else {
            return mContext.getResources().getString(R.string.connection_detail_thought_fragment_title);
        }
    }
}
