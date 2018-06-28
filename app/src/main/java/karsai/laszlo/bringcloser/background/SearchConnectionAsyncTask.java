package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.adapter.ConnectedPeopleAdapter;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.adapter.RequestFromUsersAdapter;
import karsai.laszlo.bringcloser.adapter.RequestToUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;

/**
 * Created by Laci on 17/06/2018.
 */

public class SearchConnectionAsyncTask extends AsyncTask<String, Void, List<List<Connection>>> {

    private List<Connection> mConnectionList;
    private RadioButton mSearchByNameRadioBtn;
    private Context mContext;
    private ProgressBar mProgressBar;
    private FragmentManager mFragmentManager;
    private int mFragmentId;
    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseRef;

    public SearchConnectionAsyncTask(
            FragmentManager fm,
            List<Connection> connectionList,
            RadioButton searchByNameRadioBtn,
            Context context,
            ProgressBar progressBar,
            int id) {
        this.mFragmentManager = fm;
        this.mConnectionList = connectionList;
        this.mSearchByNameRadioBtn = searchByNameRadioBtn;
        this.mContext = context;
        this.mProgressBar = progressBar;
        this.mFragmentId = id;
        //this.mCurrentUserUid = MainActivity.sFirebaseUser.getUid();
        this.mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.mUsersDatabaseRef = mFirebaseDatabase.getReference().child(ApplicationHelper.USERS_NODE);
    }

    @Override
    protected void onPreExecute() {
        //MainActivity.sViewPager.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<List<Connection>> doInBackground(String... inputs) {
        /*String filter = inputs[0];
        List<Connection> filteredConnectedConnectionsList = new ArrayList<>();
        List<Connection> filteredRequestToConnectionsList = new ArrayList<>();
        List<Connection> filteredRequestFromConnectionsList = new ArrayList<>();
        List<List<Connection>> connectionAllList = new ArrayList<>();

        for (Connection connection : mConnectionList) {
            String from = connection.getFromUid();
            String to = connection.getToUid();
            DatabaseReference toName = mUsersDatabaseRef.child(to).child(ApplicationHelper.USER_NAME_IDENTIFIER);
            String name = "";
            String type = "";
            if (connection.getConnectionBit() == ApplicationHelper.CONNECTION_BIT_POS &&
                    (from.equals(mCurrentUserUid) || to.equals(mCurrentUserUid))) {
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
                if (name.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredConnectedConnectionsList.add(connection);
                } else if (type.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredConnectedConnectionsList.add(connection);
                }
            } else if (from.equals(mCurrentUserUid)) {
                if (mSearchByNameRadioBtn.isChecked()) {
                    name = connection.getToName();
                } else {
                    type = ApplicationHelper.getPersonalizedRelationshipType(
                            mContext,
                            connection.getType(),
                            connection.getToGender(),
                            connection.getFromGender(),
                            false
                    );
                }
                if (name.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredRequestToConnectionsList.add(connection);
                } else if (type.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredRequestToConnectionsList.add(connection);
                }
            }
            else if (to.equals(mCurrentUserUid)) {
                if (mSearchByNameRadioBtn.isChecked()) {
                    name = connection.getFromName();
                } else {
                    type = ApplicationHelper.getPersonalizedRelationshipType(
                            mContext,
                            connection.getType(),
                            connection.getToGender(),
                            connection.getFromGender(),
                            true
                    );
                }
                if (name.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredRequestFromConnectionsList.add(connection);
                } else if (type.toLowerCase(Locale.getDefault())
                        .contains(filter.toLowerCase(Locale.getDefault()))) {
                    filteredRequestFromConnectionsList.add(connection);
                }
            }
        }
        connectionAllList.add(filteredConnectedConnectionsList);
        connectionAllList.add(filteredRequestFromConnectionsList);
        connectionAllList.add(filteredRequestToConnectionsList);
        return connectionAllList;*/
        return null;
    }

    @Override
    protected void onPostExecute(List<List<Connection>> collectionAllList) {
        /*mProgressBar.setVisibility(View.GONE);
        MainActivity.sViewPager.setVisibility(View.VISIBLE);
        MainActivity.sConnectionFragmentPagerAdapter = new ConnectionFragmentPagerAdapter(
                mFragmentManager,
                mContext,
                collectionAllList,
                MainActivity.sFirebaseUser.getUid(),
                true
        );
        MainActivity.sViewPager.setAdapter(MainActivity.sConnectionFragmentPagerAdapter);
        MainActivity.sViewPager.setCurrentItem(mFragmentId);*/
    }
}
