package karsai.laszlo.bringcloser.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.adapter.AllUsersAdapter;
import karsai.laszlo.bringcloser.background.AllUsersFilterAsyncTask;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.User;
import timber.log.Timber;

/**
 * Created by Laci on 07/06/2018.
 * Activity to perform adding a new connection
 */

public class AddNewConnectionActivity extends CommonActivity {

    @BindView(R.id.et_filter_users)
    TextInputEditText mFilterUsersEditText;
    @BindView(R.id.til_add_new_connection)
    TextInputLayout mFilterTextInputLayout;
    @BindView(R.id.rv_all_users)
    RecyclerView mAllUsersRecyclerView;
    @BindView(R.id.tv_info_connection)
    TextView mInfoAboutConnectionsTextView;
    @BindView(R.id.pb_filter_all_users)
    ProgressBar mFilterProgressBar;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;

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

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_new_connection);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();

        if (savedInstanceState != null) {
            mUserList = savedInstanceState.getParcelableArrayList(SAVE_USER_LIST_KEY);
            mFilterUsersEditText.setText(savedInstanceState.getString(SAVE_FILTER_KEY));
            mIsFilterEditTextFocused = savedInstanceState.getBoolean(SAVE_FILTER_FOCUSED_KEY);
            mPos = savedInstanceState.getInt(ApplicationUtils.SAVE_RECYCLERVIEW_POS_KEY, -1);
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
                .child(ApplicationUtils.USERS_NODE);
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CONNECTIONS_NODE);

        mUsersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot usersDataSnapshot) {
                mConnectionsDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot connDataSnapshot) {
                        mUserList = new ArrayList<>();
                        List<User> initializedUserList = new ArrayList<>();
                        for (DataSnapshot userSnapshot : usersDataSnapshot.getChildren()) {
                            String key = userSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null adding new connection getting connection data");
                                continue;
                            }
                            if (!key.equals(mCurrentUserUid)) {
                                User user = userSnapshot.getValue(User.class);
                                if (user == null) {
                                    Timber.wtf("user null adding new connection getting user data");
                                    continue;
                                }
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
                                if (connection == null) {
                                    Timber.wtf("connection null getting information for collecting user list");
                                    continue;
                                }
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

        mFilterUsersEditText.addTextChangedListener(new TextWatcher() {
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
        });

        mFilterUsersEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mAllUsersRecyclerView.requestFocus();
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    onSupportNavigateUp();
                }
                return false;
            }
        });
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
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
        int userListSize = mUserList.size();
        mInfoAboutConnectionsTextView.setVisibility(View.VISIBLE);
        if (userListSize != 0) {
            mFilterTextInputLayout.setVisibility(View.VISIBLE);
            mAllUsersRecyclerView.setVisibility(View.VISIBLE);
            mFilterProgressBar.setVisibility(View.GONE);
            mInitFilter = mFilterUsersEditText.getText().toString();
            if (mInitFilter.isEmpty()) {
                showFoundResultsNumber(
                        getResources().getString(R.string.add_connection_possible_size),
                        userListSize
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
            mFilterTextInputLayout.setVisibility(View.GONE);
            mAllUsersRecyclerView.setVisibility(View.GONE);
            mFilterProgressBar.setVisibility(View.GONE);
            mInfoAboutConnectionsTextView.setText(
                    getResources().getString(R.string.add_connection_no_available)
            );
        }
    }

    private void showFoundResultsNumber(String title, int size) {
        mInfoAboutConnectionsTextView.setText(
                new StringBuilder()
                        .append(title)
                        .append(" (")
                        .append(size)
                        .append(")")
                        .toString()
        );
    }

    private void applySearch(String filter) {
        if (filter.length() == 0) {
            mFilteredUserList = mUserList;
            showFoundResultsNumber(
                    getResources().getString(R.string.add_connection_possible_size),
                    mUserList.size()
            );
            applyResults(
                    AddNewConnectionActivity.this,
                    getApplicationContext(),
                    mUserList,
                    mAllUsersRecyclerView
            );
        } else {
            new AllUsersFilterAsyncTask(
                    new WeakReference<Context>(AddNewConnectionActivity.this),
                    new WeakReference<Context>(getApplicationContext()),
                    mUserList,
                    new WeakReference<RecyclerView>(mAllUsersRecyclerView),
                    new WeakReference<TextView>(mInfoAboutConnectionsTextView),
                    new WeakReference<ProgressBar>(mFilterProgressBar),
                    mFilteredUserList).execute(filter);
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
                ApplicationUtils.SAVE_RECYCLERVIEW_POS_KEY,
                ((GridLayoutManager)mAllUsersRecyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
}
