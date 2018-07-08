package karsai.laszlo.bringcloser.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import karsai.laszlo.bringcloser.ui.screens.addnewconnection.AddNewConnectionActivity;
import karsai.laszlo.bringcloser.adapter.RequestToUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestToPeopleFragment extends Fragment {


    public RequestToPeopleFragment() {
        // Required empty public constructor
    }

    private RecyclerView mRequestToUsersRecyclerView;
    private List<ConnectionDetail> mRequestToConnectionDetailList;
    private List<Connection> mRequestToConnectionList;
    private TextView mEmptyListTextView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mAddFab;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;
    private DatabaseReference mUsersDatabaseRef;
    private ValueEventListener mConnectionsEventListener;
    private int mPos = -1;
    private CustomFastScroller mFastScroller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(R.layout.fragment_request_to_people, container, false);
        mRequestToUsersRecyclerView = rootView.findViewById(R.id.rv_request_to_users);
        mEmptyListTextView = rootView.findViewById(R.id.tv_request_to_empty);
        mProgressBar = rootView.findViewById(R.id.pb_search_request_to);
        mAddFab = rootView.findViewById(R.id.fab_add_new_connection_request_to);
        mFastScroller = rootView.findViewById(R.id.fast_scroll_rq_to);

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

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getContext(),
                getContext().getResources().getInteger(R.integer.requested_rv_span_count));
        mRequestToUsersRecyclerView.setLayoutManager(layoutManager);
        mRequestToUsersRecyclerView.setHasFixedSize(true);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mConnectionsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                mRequestToConnectionList = new ArrayList<>();
                for (DataSnapshot connSnapshot : dataSnapshot.getChildren()) {
                    Connection connection = connSnapshot.getValue(Connection.class);
                    if (connection.getConnectionBit() == 0
                            && connection.getFromUid().equals(mFirebaseUser.getUid())) {
                        mRequestToConnectionList.add(connection);
                    }
                }
                mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mRequestToConnectionDetailList = new ArrayList<>();
                        for (Connection connection : mRequestToConnectionList) {
                            String fromUid = connection.getFromUid();
                            String toUid = connection.getToUid();
                            String type = connection.getType();
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
                                    mRequestToConnectionDetailList.add(connectionDetail);
                                    break;
                                }
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                        if (mRequestToConnectionDetailList.size() == 0) {
                            mRequestToUsersRecyclerView.setVisibility(View.GONE);
                            mEmptyListTextView.setVisibility(View.VISIBLE);
                            mAddFab.setVisibility(View.VISIBLE);
                        } else {
                            mRequestToUsersRecyclerView.setVisibility(View.VISIBLE);
                            mEmptyListTextView.setVisibility(View.GONE);
                            mAddFab.setVisibility(View.GONE);
                            Collections.sort(mRequestToConnectionDetailList,
                                    new Comparator<ConnectionDetail>() {
                                        @Override
                                        public int compare(
                                                ConnectionDetail detailOne, ConnectionDetail detailTwo) {
                                            return detailOne
                                                    .getToName()
                                                    .toLowerCase(Locale.getDefault())
                                                    .compareTo(
                                                            detailTwo.getToName()
                                                                    .toLowerCase(
                                                                            Locale.getDefault()
                                                                    )
                                                    );
                                        }
                                    });
                            int pos = ((GridLayoutManager)mRequestToUsersRecyclerView
                                    .getLayoutManager())
                                    .findFirstVisibleItemPosition();
                            RequestToUsersAdapter requestToUsersAdapter = new RequestToUsersAdapter(
                                    getContext(),
                                    mRequestToConnectionDetailList
                            );
                            mRequestToUsersRecyclerView.setAdapter(requestToUsersAdapter);
                            if (mPos == -1) {
                                mRequestToUsersRecyclerView.scrollToPosition(pos);
                            } else {
                                mRequestToUsersRecyclerView.scrollToPosition(mPos);
                                mPos = -1;
                            }
                            mFastScroller.setRecyclerView(mRequestToUsersRecyclerView);
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
                ((GridLayoutManager)mRequestToUsersRecyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
}
