package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
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

    private WeakReference<Context> mContext;
    private WeakReference<Context> mAppContext;
    private List<User> mUserList;
    private WeakReference<RecyclerView> mAllUsersRecyclerView;
    private WeakReference<TextView> mInfoAboutConnectionsTextView;
    private WeakReference<ProgressBar> mFilterProgressBar;
    private List<User> mFilteredUserList;

    public AllUsersFilterAsyncTask(
            WeakReference<Context> context,
            WeakReference<Context> appContext,
            List<User> userList,
            WeakReference<RecyclerView> recyclerView,
            WeakReference<TextView> textView,
            WeakReference<ProgressBar> progressBar,
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
        mAllUsersRecyclerView.get().setVisibility(View.GONE);
        mInfoAboutConnectionsTextView.get().setVisibility(View.GONE);
        mFilterProgressBar.get().setVisibility(View.VISIBLE);
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
        mAllUsersRecyclerView.get().setVisibility(View.VISIBLE);
        mInfoAboutConnectionsTextView.get().setVisibility(View.VISIBLE);
        mFilterProgressBar.get().setVisibility(View.GONE);
        int position = ((GridLayoutManager)mAllUsersRecyclerView.get().getLayoutManager())
                .findFirstVisibleItemPosition();
        AllUsersAdapter allUsersAdapter = new AllUsersAdapter(
                mContext.get(),
                mAppContext.get(),
                mFilteredUserList
        );
        mAllUsersRecyclerView.get().setAdapter(allUsersAdapter);
        mAllUsersRecyclerView.get().scrollToPosition(position);
        mInfoAboutConnectionsTextView.get().setText(
                new StringBuilder()
                        .append(mContext.get()
                                .getResources()
                                .getString(R.string.add_connection_found_possible_size)
                        ).append(" (")
                        .append(mFilteredUserList.size())
                        .append(")")
                        .toString()
        );
    }
}