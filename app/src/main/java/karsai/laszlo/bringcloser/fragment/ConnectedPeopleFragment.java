package karsai.laszlo.bringcloser.fragment;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.RecyclerViewScrollListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.CustomFastScroller;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.activity.AddNewConnectionActivity;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.adapter.ConnectedPeopleAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectedPeopleFragment extends Fragment {

    public ConnectedPeopleFragment() {
        // Required empty public constructor
    }

    private RecyclerView mConnectedUsersRecyclerView;
    private List<ConnectionDetail> mConnectionDetailList;
    private List<Connection> mConnectionList;
    private TextView mEmptyListTextView;
    private ProgressBar mProgressBar;
    private ConnectedPeopleAdapter mConnectedPeopleAdapter;
    private FloatingActionButton mAddFab;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;
    private DatabaseReference mUsersDatabaseRef;
    private ValueEventListener mConnectionsEventListener;
    private int mPos = -1;
    private CustomFastScroller mFastScroller;
    private String mCurrentUserUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("hopp", "itt");
        View rootView = inflater
                .inflate(R.layout.fragment_connected_people, container, false);
        mConnectedUsersRecyclerView = rootView.findViewById(R.id.rv_connected_users);
        mFastScroller = rootView.findViewById(R.id.fast_scroll_connection);
        mEmptyListTextView = rootView.findViewById(R.id.tv_connected_empty);
        mProgressBar = rootView.findViewById(R.id.pb_search_connected);
        mAddFab = rootView.findViewById(R.id.fab_add_new_connection_connected);

        if (savedInstanceState != null) {
            mPos = savedInstanceState.getInt(ApplicationHelper.SAVE_RECYCLERVIEW_POS_KEY, -1);
        }

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mAddFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirebaseUser.isEmailVerified()) {
                    getContext().startActivity(
                            new Intent(getContext(), AddNewConnectionActivity.class));
                } else {
                    Snackbar.make(
                            view,
                            getContext().getResources()
                                    .getString(R.string.not_verified_email_address_message),
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(
                getContext(),
                getContext().getResources().getInteger(R.integer.connected_rv_span_count));
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mConnectedUsersRecyclerView.setLayoutManager(layoutManager);
        mConnectedUsersRecyclerView.setHasFixedSize(true);
        mConnectedUsersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mAddFab.getVisibility() == View.VISIBLE) {
                    mAddFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0 && mAddFab.getVisibility() != View.VISIBLE) {
                    mAddFab.setVisibility(View.VISIBLE);
                }
            }
        });
        mFastScroller.addScrollerListener(new RecyclerViewScrollListener.ScrollerListener() {

            float previousPos = 0F;

            @Override
            public void onScroll(float relativePos) {
                float dy = relativePos - previousPos;
                if (dy > 0 && mAddFab.getVisibility() == View.VISIBLE) {
                    mAddFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0 && mAddFab.getVisibility() != View.VISIBLE) {
                    mAddFab.setVisibility(View.VISIBLE);
                }
                previousPos = relativePos;
            }
        });

        mCurrentUserUid = mFirebaseUser.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mConnectionsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                mConnectionList = new ArrayList<>();
                for (DataSnapshot connSnapshot : dataSnapshot.getChildren()) {
                    Connection connection = connSnapshot.getValue(Connection.class);
                    if (connection.getConnectionBit() == 1
                            && (connection.getToUid().equals(mCurrentUserUid)
                            || connection.getFromUid().equals(mCurrentUserUid))) {
                        mConnectionList.add(connection);
                    }
                }
                mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mConnectionDetailList = new ArrayList<>();
                        for (Connection connection : mConnectionList) {
                            String fromUid = connection.getFromUid();
                            String toUid = connection.getToUid();
                            String type = connection.getType();
                            String timestamp = connection.getTimestamp();
                            ConnectionDetail connectionDetail = new ConnectionDetail();
                            boolean isFromDataRead = false;
                            boolean isToDataRead = false;
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                User user = userSnapshot.getValue(User.class);
                                if (fromUid.equals(uid)) {
                                    connectionDetail.setFromUid(uid);
                                    connectionDetail.setFromGender(user.getGender());
                                    connectionDetail.setFromName(user.getUsername());
                                    connectionDetail.setFromPhotoUrl(user.getPhotoUrl());
                                    connectionDetail.setFromBirthday(user.getBirthday());
                                    isFromDataRead = true;
                                } else if (toUid.equals(uid)) {
                                    connectionDetail.setToUid(uid);
                                    connectionDetail.setToGender(user.getGender());
                                    connectionDetail.setToName(user.getUsername());
                                    connectionDetail.setToPhotoUrl(user.getPhotoUrl());
                                    connectionDetail.setToBirthday(user.getBirthday());
                                    isToDataRead = true;
                                }
                                if (isFromDataRead && isToDataRead) {
                                    connectionDetail.setType(type);
                                    connectionDetail.setTimestamp(timestamp);
                                    mConnectionDetailList.add(connectionDetail);
                                    break;
                                }
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                        if (mConnectionDetailList.size() == 0) {
                            mEmptyListTextView.setVisibility(View.VISIBLE);
                            mConnectedUsersRecyclerView.setVisibility(View.GONE);
                        } else {
                            mEmptyListTextView.setVisibility(View.GONE);
                            mConnectedUsersRecyclerView.setVisibility(View.VISIBLE);
                            Collections.sort(mConnectionDetailList, new Comparator<ConnectionDetail>() {
                                @Override
                                public int compare(
                                        ConnectionDetail detailOne, ConnectionDetail detailTwo) {
                                    if (detailOne.getFromUid().equals(mCurrentUserUid)
                                            && detailTwo.getFromUid().equals(mCurrentUserUid)) {
                                        return detailOne
                                                .getToName()
                                                .toLowerCase(Locale.getDefault())
                                                .compareTo(
                                                        detailTwo.getToName()
                                                                .toLowerCase(
                                                                        Locale.getDefault()
                                                                )
                                                );
                                    } else if (detailOne.getFromUid().equals(mCurrentUserUid)
                                            && detailTwo.getToUid().equals(mCurrentUserUid)) {
                                        return detailOne
                                                .getToName()
                                                .toLowerCase(Locale.getDefault())
                                                .compareTo(
                                                        detailTwo.getFromName()
                                                                .toLowerCase(
                                                                        Locale.getDefault()
                                                                )
                                                );
                                    } else if (detailOne.getToUid().equals(mCurrentUserUid)
                                            && detailTwo.getToUid().equals(mCurrentUserUid)) {
                                        return detailOne
                                                .getFromName()
                                                .toLowerCase(Locale.getDefault())
                                                .compareTo(
                                                        detailTwo.getFromName()
                                                                .toLowerCase(
                                                                        Locale.getDefault()
                                                                )
                                                );
                                    } else if (detailOne.getToUid().equals(mCurrentUserUid)
                                            && detailTwo.getFromUid().equals(mCurrentUserUid)) {
                                        return detailOne
                                                .getFromName()
                                                .toLowerCase(Locale.getDefault())
                                                .compareTo(
                                                        detailTwo.getToName()
                                                                .toLowerCase(
                                                                        Locale.getDefault()
                                                                )
                                                );
                                    }
                                    return 0;
                                }
                            });
                            int pos = ((GridLayoutManager)mConnectedUsersRecyclerView
                                    .getLayoutManager())
                                    .findFirstVisibleItemPosition();
                            mConnectedPeopleAdapter = new ConnectedPeopleAdapter(
                                    getContext(),
                                    mConnectionDetailList
                            );
                            mConnectedUsersRecyclerView.setAdapter(mConnectedPeopleAdapter);
                            if (mPos == -1) {
                                mConnectedUsersRecyclerView.scrollToPosition(pos);
                            } else {
                                mConnectedUsersRecyclerView.scrollToPosition(mPos);
                                mPos = -1;
                            }
                            mFastScroller.setRecyclerView(mConnectedUsersRecyclerView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsDatabaseRef.addValueEventListener(mConnectionsEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mConnectionsEventListener != null) {
            mConnectionsDatabaseRef.removeEventListener(mConnectionsEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(
                ApplicationHelper.SAVE_RECYCLERVIEW_POS_KEY,
                ((GridLayoutManager)mConnectedUsersRecyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
}
