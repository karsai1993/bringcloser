package karsai.laszlo.bringcloser.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ThoughtAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Thought;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionThoughtFragment extends Fragment {

    private LinearLayout mThoughtDataLinearLayout;
    private TextView mThoughtSortedByValueTextView;
    private RecyclerView mThoughtRecyclerView;
    private TextView mThoughtNoDataTextView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlusOneThoughtFab;
    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ConnectionDetail mConnectionDetail;
    private List<Thought> mThoughtList;
    private ThoughtAdapter mThoughtAdapter;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionThoughtsValueEventListener;
    private DatabaseReference mThoughtsDatabaseRef;
    private Activity mActivity;

    public ConnectionThoughtFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView
                = inflater.inflate(R.layout.fragment_connection_thought, container, false);
        mThoughtDataLinearLayout = rootView.findViewById(R.id.ll_thought_layout);
        mThoughtSortedByValueTextView = rootView.findViewById(R.id.tv_thought_sorted_by_value);
        mThoughtRecyclerView = rootView.findViewById(R.id.rv_thought);
        mProgressBar = rootView.findViewById(R.id.pb_thought);
        mThoughtNoDataTextView = rootView.findViewById(R.id.tv_thought_no_data);

        mThoughtRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false));
        mThoughtRecyclerView.setHasFixedSize(true);
        mThoughtRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0
                        && mPlusOneThoughtFab != null
                        && mPlusOneThoughtFab.getVisibility() == View.VISIBLE) {
                    mPlusOneThoughtFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0
                        && mPlusOneThoughtFab != null
                        && mPlusOneThoughtFab.getVisibility() != View.VISIBLE) {
                    mPlusOneThoughtFab.setVisibility(View.VISIBLE);
                }
            }
        });

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mConnectionDetail = bundle.getParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY);
        }

        mThoughtList = new ArrayList<>();
        mThoughtAdapter = new ThoughtAdapter(
                getContext(),
                mThoughtList
        );
        mThoughtRecyclerView.setAdapter(mThoughtAdapter);

        mActivity = getActivity();
        if (mActivity != null) {
            mPlusOneThoughtFab = mActivity.findViewById(R.id.fab_plus_one_thought);
            mPlusOneThoughtFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "Thought", Toast.LENGTH_SHORT).show();
                }
            });
        }

        mConnectionsQuery = mConnectionsDatabaseReference
                .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid());
        mConnectionThoughtsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class)
                            .equals(mConnectionDetail.getToUid())) {
                        mThoughtsDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.THOUGHTS_NODE);
                        mThoughtsDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int prevThoughtNum = mThoughtList.size();
                                if (prevThoughtNum > 1 && !dataSnapshot.exists()) {
                                    String otherName;
                                    String otherGender;
                                    if (mConnectionDetail.getFromUid().equals(mCurrentUserUid)) {
                                        otherName = mConnectionDetail.getToName();
                                        otherGender = mConnectionDetail.getToGender();
                                    } else {
                                        otherGender = mConnectionDetail.getFromGender();
                                        otherName = mConnectionDetail.getFromName();
                                    }
                                    String genderRepresentative;
                                    if (otherGender.equals(getString(R.string.gender_female))) {
                                        genderRepresentative
                                                = getString(R.string.gender_representative_female);
                                    } else if (otherGender.equals(getString(R.string.gender_male))) {
                                        genderRepresentative
                                                = getString(R.string.gender_representative_male);
                                    } else {
                                        genderRepresentative
                                                = getString(R.string.gender_representative_none);
                                    }
                                    Toast.makeText(
                                            getContext(),
                                            new StringBuilder()
                                                    .append(getString(R.string
                                                            .other_people_delete_common_1))
                                                    .append(otherName)
                                                    .append(getString(R.string
                                                            .other_people_delete_common_2))
                                                    .append(genderRepresentative)
                                                    .append(getString(R.string
                                                            .other_people_delete_common_3))
                                                    .toString(),
                                            Toast.LENGTH_LONG).show();
                                    if (mActivity != null) {
                                        mActivity.finish();
                                    }
                                }
                                mThoughtList.clear();
                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                    Thought thought = eventSnapshot.getValue(Thought.class);
                                    mThoughtList.add(thought);
                                }
                                mProgressBar.setVisibility(View.GONE);
                                int thoughtNum = mThoughtList.size();
                                if (thoughtNum > 0) {
                                    mThoughtAdapter.notifyDataSetChanged();
                                    mThoughtNoDataTextView.setVisibility(View.GONE);
                                    mThoughtDataLinearLayout.setVisibility(View.VISIBLE);
                                } else {
                                    mThoughtDataLinearLayout.setVisibility(View.GONE);
                                    mThoughtNoDataTextView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mProgressBar.setVisibility(View.VISIBLE);
        mThoughtDataLinearLayout.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionThoughtsValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mConnectionThoughtsValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionThoughtsValueEventListener);
        }
    }
}
