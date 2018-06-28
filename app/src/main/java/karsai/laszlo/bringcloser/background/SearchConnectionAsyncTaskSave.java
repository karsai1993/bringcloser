package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.adapter.ConnectedPeopleAdapter;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.adapter.RequestFromUsersAdapter;
import karsai.laszlo.bringcloser.adapter.RequestToUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;

/**
 * Created by Laci on 17/06/2018.
 */

public class SearchConnectionAsyncTaskSave extends AsyncTask<String, Void, List<Connection>> {

    private List<Connection> mConnectionList;
    private RadioButton mSearchByNameRadioBtn;
    private RecyclerView.Adapter mAdapter;
    private String mCurrentUserUid;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mFoundInfo;

    public SearchConnectionAsyncTaskSave(
            List<Connection> connectionList,
            RadioButton searchByNameRadioBtn,
            RecyclerView.Adapter adapter,
            String currentUserUid,
            Context context,
            RecyclerView recyclerView,
            ProgressBar progressBar,
            TextView foundInfo) {
        this.mConnectionList = connectionList;
        this.mSearchByNameRadioBtn = searchByNameRadioBtn;
        this.mAdapter = adapter;
        this.mCurrentUserUid = currentUserUid;
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mProgressBar = progressBar;
        this.mFoundInfo = foundInfo;
    }

    public SearchConnectionAsyncTaskSave(
            List<Connection> connectionList,
            RadioButton searchByNameRadioBtn,
            RecyclerView.Adapter adapter,
            Context context,
            RecyclerView recyclerView,
            ProgressBar progressBar,
            TextView foundInfo) {
        this.mConnectionList = connectionList;
        this.mSearchByNameRadioBtn = searchByNameRadioBtn;
        this.mAdapter = adapter;
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mProgressBar = progressBar;
        this.mFoundInfo = foundInfo;
    }

    @Override
    protected void onPreExecute() {
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<Connection> doInBackground(String... inputs) {
        /*String filter = inputs[0];
        List<Connection> filteredList = new ArrayList<>();
        for (Connection connection : mConnectionList) {
            String from = connection.getFromUid();
            String name = "";
            String type = "";
            if (mAdapter instanceof ConnectedPeopleAdapter) {
                if (mSearchByNameRadioBtn.isChecked()) {
                    if (from.equals(mCurrentUserUid)) {
                        name = connection.getToName();
                    } else {
                        name = connection.getFromName();
                    }
                } else {
                    if (from.equals(mCurrentUserUid)) {
                        type = ApplicationHelper.getPersonalizedRelationshipType(
                                mContext,
                                connection.getType(),
                                connection.getToGender(),
                                connection.getFromGender(),
                                false
                        );
                    } else {
                        type = ApplicationHelper.getPersonalizedRelationshipType(
                                mContext,
                                connection.getType(),
                                connection.getToGender(),
                                connection.getFromGender(),
                                true
                        );
                    }
                }
            } else if (mAdapter instanceof RequestFromUsersAdapter) {
                name = connection.getFromName();
                type = ApplicationHelper.getPersonalizedRelationshipType(
                        mContext,
                        connection.getType(),
                        connection.getToGender(),
                        connection.getFromGender(),
                        true
                );
            } else if (mAdapter instanceof RequestToUsersAdapter) {
                name = connection.getToName();
                type = ApplicationHelper.getPersonalizedRelationshipType(
                        mContext,
                        connection.getType(),
                        connection.getToGender(),
                        connection.getFromGender(),
                        false
                );
            }
            if (type.isEmpty() && name.toLowerCase(Locale.getDefault())
                    .contains(filter.toLowerCase(Locale.getDefault()))) {
                filteredList.add(connection);
            } else if (name.isEmpty() && type.toLowerCase(Locale.getDefault())
                    .contains(filter.toLowerCase(Locale.getDefault()))) {
                filteredList.add(connection);
            }
        }
        return filteredList;*/
        return null;
    }

    @Override
    protected void onPostExecute(List<Connection> filteredList) {
        //mProgressBar.setVisibility(View.GONE);
        //mRecyclerView.setVisibility(View.VISIBLE);

        /*if (mAdapter instanceof ConnectedPeopleAdapter) {
            ApplicationHelper.showFoundResultsNumber(
                    mContext.getResources().getString(R.string.found_connections),
                    filteredList.size(),
                    mFoundInfo
            );
        } else if (mAdapter instanceof RequestToUsersAdapter) {
            ApplicationHelper.showFoundResultsNumber(
                    mContext.getResources().getString(R.string.found_sent_requests),
                    filteredList.size(),
                    mFoundInfo
            );
        } else if (mAdapter instanceof RequestFromUsersAdapter) {
            ApplicationHelper.showFoundResultsNumber(
                    mContext.getResources().getString(R.string.found_received_requests),
                    filteredList.size(),
                    mFoundInfo
            );
        }
        ApplicationHelper.applyConnectionResults(
                mContext,
                filteredList,
                mAdapter,
                mRecyclerView,
                mCurrentUserUid
        );*/
    }
}
