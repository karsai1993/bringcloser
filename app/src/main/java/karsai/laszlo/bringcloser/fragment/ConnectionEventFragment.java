package karsai.laszlo.bringcloser.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.EventAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.ui.screens.addneweventactivity.AddNewEventActivity;
import karsai.laszlo.bringcloser.ui.screens.addnewwishactivity.AddNewWishActivity;
import karsai.laszlo.bringcloser.utils.DialogUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionEventFragment extends Fragment implements Comparator<Event>{

    private LinearLayout mEventDataLinearLayout;
    private TextView mEventSortedByValueTextView;
    private LinearLayout mEventSortingLinearLayout;
    private RecyclerView mEventRecyclerView;
    private TextView mEventNoDataTextView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlusOneEventFab;
    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ConnectionDetail mConnectionDetail;
    private List<Event> mEventList;
    private EventAdapter mEventAdapter;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionEventsValueEventListener;
    private DatabaseReference mEventsDatabaseRef;
    private Activity mActivity;
    private Context mSavedContext;

    private static final String INSTANCE_SAVE_SORT_BY_VALUE = "instance_save_sort_by_value";

    public ConnectionEventFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView
                = inflater.inflate(R.layout.fragment_connection_event, container, false);
        mEventDataLinearLayout = rootView.findViewById(R.id.ll_event_layout);
        mEventSortedByValueTextView = rootView.findViewById(R.id.tv_event_sorted_by_value);
        mEventSortingLinearLayout = rootView.findViewById(R.id.ll_event_sorting);
        mEventRecyclerView = rootView.findViewById(R.id.rv_event);
        mProgressBar = rootView.findViewById(R.id.pb_event);
        mEventNoDataTextView = rootView.findViewById(R.id.tv_event_no_data);

        mSavedContext = getContext();

        if (savedInstanceState != null) {
            mEventSortedByValueTextView.setText(savedInstanceState.getString(INSTANCE_SAVE_SORT_BY_VALUE));
        }

        mEventRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getContext().getResources().getInteger(R.integer.rv_connection_detail_span_count),
                StaggeredGridLayoutManager.VERTICAL
        ));
        mEventRecyclerView.setHasFixedSize(true);
        mEventRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0
                        && mPlusOneEventFab != null
                        && mPlusOneEventFab.getVisibility() == View.VISIBLE) {
                    mPlusOneEventFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0
                        && mPlusOneEventFab != null
                        && mPlusOneEventFab.getVisibility() != View.VISIBLE) {
                    mPlusOneEventFab.setVisibility(View.VISIBLE);
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

        mEventList = new ArrayList<>();
        mEventAdapter = new EventAdapter(
                getContext(),
                mEventList
        );
        mEventRecyclerView.setAdapter(mEventAdapter);

        mActivity = getActivity();
        if (mActivity != null) {
            mPlusOneEventFab = mActivity.findViewById(R.id.fab_plus_one_event);
            mPlusOneEventFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), AddNewEventActivity.class);
                    intent.putExtra(ApplicationHelper.EXTRA_DATA, mConnectionDetail);
                    startActivity(intent);
                }
            });
        }
        mEventSortingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String title = getContext().getString(R.string.sorted_by_title);
                final String [] options
                        = getContext().getResources().getStringArray(R.array.event_sort_options);
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String chosenOrder = options[i];
                        mEventSortedByValueTextView.setText(chosenOrder);
                        applyNewOrder();
                        dialogInterface.dismiss();
                    }
                };
                DialogUtils.onDialogRequestForSorting(
                        getContext(),
                        title,
                        options,
                        onClickListener
                );
            }
        });
        mConnectionsQuery = mConnectionsDatabaseReference
                .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid());
        mConnectionEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class)
                            .equals(mConnectionDetail.getToUid())) {
                        mEventsDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.EVENTS_NODE);
                        mEventsDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int prevEventNum = mEventList.size();
                                if (prevEventNum > 1 && !dataSnapshot.exists()) {
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
                                    if (otherGender.equals(mSavedContext
                                            .getResources().getString(R.string.gender_female))) {
                                        genderRepresentative
                                                = mSavedContext.getResources()
                                                .getString(R.string.gender_representative_female);
                                    } else if (otherGender.equals(mSavedContext
                                            .getResources().getString(R.string.gender_male))) {
                                        genderRepresentative
                                                = mSavedContext.getResources()
                                                .getString(R.string.gender_representative_male);
                                    } else {
                                        genderRepresentative
                                                = mSavedContext.getResources()
                                                .getString(R.string.gender_representative_none);
                                    }
                                    Toast.makeText(
                                            mSavedContext,
                                            new StringBuilder()
                                                    .append(mSavedContext.getResources()
                                                            .getString(R.string
                                                            .other_people_delete_common_1))
                                                    .append(otherName)
                                                    .append(mSavedContext.getResources()
                                                            .getString(R.string
                                                            .other_people_delete_common_2))
                                                    .append(genderRepresentative)
                                                    .append(mSavedContext.getResources()
                                                            .getString(R.string
                                                            .other_people_delete_common_3))
                                                    .toString(),
                                            Toast.LENGTH_LONG).show();
                                    if (mActivity != null) {
                                        mActivity.finish();
                                    }
                                }
                                mEventList.clear();
                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                    Event event = eventSnapshot.getValue(Event.class);
                                    mEventList.add(event);
                                }

                                Collections.sort(mEventList, ConnectionEventFragment.this);

                                mProgressBar.setVisibility(View.GONE);
                                int eventNum = mEventList.size();
                                if (eventNum > 0) {
                                    mEventAdapter.notifyDataSetChanged();
                                    mEventNoDataTextView.setVisibility(View.GONE);
                                    mEventDataLinearLayout.setVisibility(View.VISIBLE);
                                } else {
                                    mEventDataLinearLayout.setVisibility(View.GONE);
                                    mEventNoDataTextView.setVisibility(View.VISIBLE);
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
        mEventDataLinearLayout.setVisibility(View.GONE);

        return rootView;
    }

    private void applyNewOrder() {
        List<Event> eventList = new ArrayList<>(mEventList);
        mEventList.clear();
        Collections.sort(eventList, this);
        mEventList.addAll(eventList);
        mEventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionEventsValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mConnectionEventsValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionEventsValueEventListener);
        }
    }

    @Override
    public int compare(Event eventOne, Event eventTwo) {
        String order = mEventSortedByValueTextView.getText().toString();
        Context context = getContext();
        if (context == null) {
            context = mSavedContext;
        }
        if (order.equals(context.getResources().getString(R.string.event_sorted_by_default))) {
            return eventOne.getWhenToArrive().compareTo(eventTwo.getWhenToArrive());
        } else if (order.equals(context.getResources().getString(R.string.event_sorted_by_title))) {
            return eventOne.getTitle().compareToIgnoreCase(eventTwo.getTitle());
        } else {
            return eventOne.getPlace().compareToIgnoreCase(eventTwo.getPlace());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(INSTANCE_SAVE_SORT_BY_VALUE, mEventSortedByValueTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
