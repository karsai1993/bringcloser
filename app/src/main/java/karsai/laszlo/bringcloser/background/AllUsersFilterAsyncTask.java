package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.AllUsersAdapter;
import karsai.laszlo.bringcloser.model.User;

/**
 * Background task to handle filtering in all users
 */
public class AllUsersFilterAsyncTask extends AsyncTask<String, Void, List<User>> {

    private Context mContext;
    private Context mAppContext;
    private List<User> mUserList;
    private RecyclerView mAllUsersRecyclerView;
    private TextView mInfoAboutConnectionsTextView;
    private ProgressBar mFilterProgressBar;
    private List<User> mFilteredUserList;

    public AllUsersFilterAsyncTask(
            Context context,
            Context appContext,
            List<User> userList,
            RecyclerView recyclerView,
            TextView textView,
            ProgressBar progressBar,
            List<User> filteredUserList) {
        this.mContext = context;
        this.mAppContext = appContext;
        this.mUserList = userList;
        this.mAllUsersRecyclerView = recyclerView;
        this.mInfoAboutConnectionsTextView = textView;
        this.mFilterProgressBar = progressBar;
        this.mFilteredUserList = filteredUserList;
    }

    @Override
    protected void onPreExecute() {
        mAllUsersRecyclerView.setVisibility(View.GONE);
        mInfoAboutConnectionsTextView.setVisibility(View.GONE);
        mFilterProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<User> doInBackground(String... strings) {
        String filter = strings[0].toLowerCase(Locale.getDefault());
        List<User> resultUserList = new ArrayList<>();
        for (User user : mUserList) {
            String usernameLowerCase = user.getUsername().toLowerCase(Locale.getDefault());
            if (usernameLowerCase.contains(filter)) resultUserList.add(user);
        }
        return resultUserList;
    }

    @Override
    protected void onPostExecute(List<User> userList) {
        mFilteredUserList = userList;
        mAllUsersRecyclerView.setVisibility(View.VISIBLE);
        mInfoAboutConnectionsTextView.setVisibility(View.VISIBLE);
        mFilterProgressBar.setVisibility(View.GONE);
        int position = ((GridLayoutManager)mAllUsersRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        AllUsersAdapter allUsersAdapter = new AllUsersAdapter(
                mContext,
                mAppContext,
                mFilteredUserList
        );
        mAllUsersRecyclerView.setAdapter(allUsersAdapter);
        mAllUsersRecyclerView.scrollToPosition(position);
        mInfoAboutConnectionsTextView.setText(
                new StringBuilder()
                        .append(mContext
                                .getResources()
                                .getString(R.string.add_connection_found_possible_size)
                        ).append(" (")
                        .append(mFilteredUserList.size())
                        .append(")")
                        .toString()
        );
    }
}