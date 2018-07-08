package karsai.laszlo.bringcloser.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import karsai.laszlo.bringcloser.adapter.RequestFromUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFromPeopleFragment extends Fragment {


    public RequestFromPeopleFragment() {
        // Required empty public constructor
    }

    private RecyclerView mRequestFromUsersRecyclerView;
    private List<ConnectionDetail> mRequestFromConnectionDetailList;
    private List<Connection> mRequestFromConnectionList;
    private TextView mEmptyListTextView;
    private ProgressBar mProgressBar;
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
        View rootView = inflater.inflate(R.layout.fragment_request_from_people, container, false);
        mRequestFromUsersRecyclerView = rootView.findViewById(R.id.rv_request_from_users);
        mEmptyListTextView = rootView.findViewById(R.id.tv_request_from_empty);
        mProgressBar = rootView.findViewById(R.id.pb_search_request_from);
        mFastScroller = rootView.findViewById(R.id.fast_scroll_rq_from);

        if (savedInstanceState != null) {
            mPos = savedInstanceState.getInt(ApplicationHelper.SAVE_RECYCLERVIEW_POS_KEY, -1);
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getContext(),
                getContext().getResources().getInteger(R.integer.requested_rv_span_count));
        mRequestFromUsersRecyclerView.setLayoutManager(layoutManager);
        mRequestFromUsersRecyclerView.setHasFixedSize(true);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mConnectionsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                mRequestFromConnectionList = new ArrayList<>();
                for (DataSnapshot connSnapshot : dataSnapshot.getChildren()) {
                    Connection connection = connSnapshot.getValue(Connection.class);
                    if (connection.getConnectionBit() == 0
                            && connection.getToUid().equals(mFirebaseUser.getUid())) {
                        mRequestFromConnectionList.add(connection);
                    }
                }
                mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mRequestFromConnectionDetailList = new ArrayList<>();
                        for (Connection connection : mRequestFromConnectionList) {
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
                                    mRequestFromConnectionDetailList.add(connectionDetail);
                                    break;
                                }
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                        if (mRequestFromConnectionDetailList.size() == 0) {
                            mEmptyListTextView.setVisibility(View.VISIBLE);
                            mRequestFromUsersRecyclerView.setVisibility(View.GONE);
                        } else {
                            mEmptyListTextView.setVisibility(View.GONE);
                            mRequestFromUsersRecyclerView.setVisibility(View.VISIBLE);
                            Collections.sort(mRequestFromConnectionDetailList,
                                    new Comparator<ConnectionDetail>() {
                                @Override
                                public int compare(
                                        ConnectionDetail detailOne, ConnectionDetail detailTwo) {
                                    return detailOne
                                            .getFromName()
                                            .toLowerCase(Locale.getDefault())
                                            .compareTo(
                                                    detailTwo.getFromName()
                                                            .toLowerCase(
                                                                    Locale.getDefault()
                                                            )
                                            );
                                }
                            });
                            int pos = ((GridLayoutManager)mRequestFromUsersRecyclerView
                                    .getLayoutManager())
                                    .findFirstVisibleItemPosition();
                            RequestFromUsersAdapter requestFromUsersAdapter
                                    = new RequestFromUsersAdapter(
                                    getContext(),
                                    mRequestFromConnectionDetailList
                            );
                            mRequestFromUsersRecyclerView.setAdapter(requestFromUsersAdapter);
                            if (mPos == -1) {
                                mRequestFromUsersRecyclerView.scrollToPosition(pos);
                            } else {
                                mRequestFromUsersRecyclerView.scrollToPosition(mPos);
                                mPos = -1;
                            }
                            mFastScroller.setRecyclerView(mRequestFromUsersRecyclerView);
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
                ((GridLayoutManager)mRequestFromUsersRecyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
}
