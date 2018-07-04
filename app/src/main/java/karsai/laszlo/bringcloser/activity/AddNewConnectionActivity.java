package karsai.laszlo.bringcloser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.AllUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.User;

/**
 * Created by Laci on 07/06/2018.
 */

public class AddNewConnectionActivity extends AppCompatActivity {

    @BindView(R.id.et_filter_users)
    EditText mFilterUsersEditText;
    @BindView(R.id.rv_all_users)
    RecyclerView mAllUsersRecyclerView;
    @BindView(R.id.tv_info_connection)
    TextView mInfoAboutConnectionsTextView;
    @BindView(R.id.pb_filter_all_users)
    ProgressBar mFilterProgressBar;

    private static final String SAVE_USER_LIST_KEY = "save_user_list";
    private static final String SAVE_FILTER_KEY = "save_filter";
    private static final String SAVE_FILTER_FOCUSED_KEY = "save_filter_focused";

    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mConnectionsDatabaseReference;
    private ValueEventListener mUsersValueEventListener;
    private List<User> mUserList;
    private boolean mIsFilterEditTextFocused;
    private String mInitFilter;
    private List<User> mFilteredUserList;
    private int mPos;
    private TextWatcher mTextWatcher;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_connection);
        ButterKnife.bind(this);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (savedInstanceState != null) {
            mUserList = savedInstanceState.getParcelableArrayList(SAVE_USER_LIST_KEY);
            mFilterUsersEditText.setText(savedInstanceState.getString(SAVE_FILTER_KEY));
            mIsFilterEditTextFocused = savedInstanceState.getBoolean(SAVE_FILTER_FOCUSED_KEY);
            mPos = savedInstanceState.getInt(ApplicationHelper.SAVE_RECYCLERVIEW_POS_KEY, -1);
        }

        mInitFilter = mFilterUsersEditText.getText().toString();
        if (!mInitFilter.isEmpty()) {
            if (mIsFilterEditTextFocused) mFilterUsersEditText.setSelection(mInitFilter.length());
            applySearch(mInitFilter);
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                this,
                getResources().getInteger(R.integer.rv_span_count));
        mAllUsersRecyclerView.setLayoutManager(layoutManager);
        mAllUsersRecyclerView.setHasFixedSize(true);

        mAllUsersRecyclerView.setVisibility(View.GONE);
        mInfoAboutConnectionsTextView.setVisibility(View.GONE);
        mFilterProgressBar.setVisibility(View.VISIBLE);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);

        mUsersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot usersDataSnapshot) {
                mConnectionsDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot connDataSnapshot) {
                        mUserList = new ArrayList<>();
                        List<User> initializedUserList = new ArrayList<>();
                        for (DataSnapshot userSnapshot : usersDataSnapshot.getChildren()) {
                            if (!userSnapshot.getKey().equals(mCurrentUserUid)) {
                                User user = userSnapshot.getValue(User.class);
                                if (user.getIsEmailVerified()) {
                                    initializedUserList.add(user);
                                }
                            }
                        }
                        for (ListIterator<User> listIterator = initializedUserList.listIterator();
                             listIterator.hasNext();) {
                            boolean contain = false;
                            User user = listIterator.next();
                            String userUid = user.getUid();
                            for (DataSnapshot connectionSnapshot : connDataSnapshot.getChildren()) {
                                Connection connection = connectionSnapshot.getValue(Connection.class);
                                String fromUid = connection.getFromUid();
                                String toUid = connection.getToUid();
                                if ((fromUid.equals(mCurrentUserUid) && toUid.equals(userUid))
                                        || (toUid.equals(mCurrentUserUid)
                                        && fromUid.equals(userUid))) {
                                    contain = true;
                                    break;
                                }
                            }
                            if (!contain) {
                                mUserList.add(user);
                            }
                        }
                        Collections.sort(mUserList, new Comparator<User>() {
                            @Override
                            public int compare(User userOne, User userTwo) {
                                return userOne
                                        .getUsername()
                                        .toLowerCase(Locale.getDefault())
                                        .compareTo(
                                                userTwo.getUsername()
                                                        .toLowerCase(
                                                                Locale.getDefault()
                                                        )
                                        );
                            }
                        });
                        displayUsersToChoose();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String filter = charSequence.toString();
                applySearch(filter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsersDatabaseReference.addValueEventListener(mUsersValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUsersValueEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mUsersValueEventListener);
        }
    }

    private void displayUsersToChoose() {
        Log.d("hopp_s", String.valueOf(mPos));
        int userListSize = mUserList.size();
        mInfoAboutConnectionsTextView.setVisibility(View.VISIBLE);
        if (userListSize != 0) {
            mFilterUsersEditText.setVisibility(View.VISIBLE);
            mAllUsersRecyclerView.setVisibility(View.VISIBLE);
            mFilterProgressBar.setVisibility(View.GONE);
            mFilterUsersEditText.addTextChangedListener(mTextWatcher);
            mInitFilter = mFilterUsersEditText.getText().toString();
            if (mInitFilter.isEmpty()) {
                ApplicationHelper.showFoundResultsNumber(
                        getResources().getString(R.string.add_connection_possible_size),
                        userListSize,
                        mInfoAboutConnectionsTextView
                );
                applyResults(
                        AddNewConnectionActivity.this,
                        getApplicationContext(),
                        mUserList,
                        mAllUsersRecyclerView
                );
            } else {
                applySearch(mInitFilter);
            }
        } else {
            mFilterUsersEditText.setVisibility(View.GONE);
            mAllUsersRecyclerView.setVisibility(View.GONE);
            mFilterProgressBar.setVisibility(View.GONE);
            mInfoAboutConnectionsTextView.setText(
                    getResources().getString(R.string.add_connection_no_available)
            );
        }
    }

    private void applySearch(String filter) {
        if (filter.length() == 0) {
            mFilteredUserList = mUserList;
            ApplicationHelper.showFoundResultsNumber(
                    getResources().getString(R.string.add_connection_possible_size),
                    mUserList.size(),
                    mInfoAboutConnectionsTextView
            );
            applyResults(
                    AddNewConnectionActivity.this,
                    getApplicationContext(),
                    mUserList,
                    mAllUsersRecyclerView
            );
        } else {
            new AllUsersFilterAsyncTask(
                    AddNewConnectionActivity.this,
                    getApplicationContext(),
                    mUserList,
                    mAllUsersRecyclerView,
                    mInfoAboutConnectionsTextView,
                    mFilterProgressBar,
                    mFilteredUserList).execute(filter);
        }
    }

    static class AllUsersFilterAsyncTask extends AsyncTask<String, Void, List<User>> {

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
            ApplicationHelper.showFoundResultsNumber(
                    mContext.getResources().getString(R.string.add_connection_found_possible_size),
                    mFilteredUserList.size(),
                    mInfoAboutConnectionsTextView
            );
        }
    }

    private void applyResults(
            Context context,
            Context appContext,
            List<User> userList,
            RecyclerView recyclerView) {
        int position = ((GridLayoutManager)recyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        AllUsersAdapter allUsersAdapter = new AllUsersAdapter(
                context,
                appContext,
                userList
        );
        recyclerView.setAdapter(allUsersAdapter);
        if (mPos == -1) {
            recyclerView.scrollToPosition(position);
        } else {
            recyclerView.scrollToPosition(mPos);
            mPos = -1;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVE_USER_LIST_KEY, (ArrayList<User>) mUserList);
        outState.putString(SAVE_FILTER_KEY, mFilterUsersEditText.getText().toString());
        outState.putBoolean(SAVE_FILTER_FOCUSED_KEY, mFilterUsersEditText.isFocused());
        outState.putInt(
                ApplicationHelper.SAVE_RECYCLERVIEW_POS_KEY,
                ((GridLayoutManager)mAllUsersRecyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
}
