package karsai.laszlo.bringcloser.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.adapter.EventAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.activity.AddNewEventActivity;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Fragment to handle connection events related information
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
    private ConnectionDetail mConnectionDetail;
    private List<Event> mEventList;
    private EventAdapter mEventAdapter;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionEventsValueEventListener;
    private DatabaseReference mEventsDatabaseRef;
    private Activity mActivity;
    private Context mSavedAppContext;

    private static final String INSTANCE_SAVE_SORT_BY_VALUE = "instance_save_sort_by_value";

    public ConnectionEventFragment() {}

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView
                = inflater.inflate(R.layout.fragment_connection_event, container, false);
        mEventDataLinearLayout = rootView.findViewById(R.id.ll_event_layout);
        mEventSortedByValueTextView = rootView.findViewById(R.id.tv_event_sorted_by_value);
        mEventSortingLinearLayout = rootView.findViewById(R.id.ll_event_sorting);
        mEventRecyclerView = rootView.findViewById(R.id.rv_event);
        mProgressBar = rootView.findViewById(R.id.pb_event);
        mEventNoDataTextView = rootView.findViewById(R.id.tv_event_no_data);

        mSavedAppContext = getContext();

        if (savedInstanceState != null) {
            mEventSortedByValueTextView.setText(savedInstanceState.getString(INSTANCE_SAVE_SORT_BY_VALUE));
        }

        mEventRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                Objects.requireNonNull(getContext()).getResources().getInteger(R.integer.rv_connection_detail_span_count),
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
                .child(ApplicationUtils.CONNECTIONS_NODE);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mConnectionDetail = bundle.getParcelable(ApplicationUtils.CONNECTION_DETAIL_KEY);
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
                    intent.putExtra(ApplicationUtils.EXTRA_DATA, mConnectionDetail);
                    startActivity(intent);
                }
            });
            mPlusOneEventFab.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        mEventRecyclerView.requestFocus();
                    }
                    return false;
                }
            });
        }

        mEventRecyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                        && mPlusOneEventFab != null) {
                    mPlusOneEventFab.requestFocus();
                }
                return false;
            }
        });

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
                .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid());
        mConnectionEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null connection from event");
                        continue;
                    }
                    String toUidValue = dataSnapshot
                            .child(key)
                            .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) {
                        Timber.wtf("to uid null event");
                        continue;
                    }
                    if (toUidValue.equals(mConnectionDetail.getToUid())) {
                        mEventsDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationUtils.EVENTS_NODE);
                        mEventsDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mEventList.clear();
                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                    Event event = eventSnapshot.getValue(Event.class);
                                    if (event == null) {
                                        Timber.wtf("event null");
                                        continue;
                                    }
                                    if (event.getFromUid().equals(mCurrentUserUid)) {
                                        mEventList.add(event);
                                    }
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
            context = mSavedAppContext;
        }
        if (order.equals(context.getResources().getString(R.string.sorted_by_default))) {
            Date dateOne = ApplicationUtils.getDateAndTime(
                    eventOne.getWhenToArrive()
            );
            Date dateTwo = ApplicationUtils.getDateAndTime(
                    eventTwo.getWhenToArrive()
            );
            if (dateOne == null || dateTwo == null) {
                return eventOne.getWhenToArrive().compareTo(eventTwo.getWhenToArrive());
            }
            return dateOne.compareTo(dateTwo);
        } else if (order.equals(context.getResources()
                .getString(R.string.sorted_by_time_descending))) {
            Date dateOne = ApplicationUtils.getDateAndTime(
                    eventOne.getWhenToArrive()
            );
            Date dateTwo = ApplicationUtils.getDateAndTime(
                    eventTwo.getWhenToArrive()
            );
            if (dateOne == null || dateTwo == null) {
                return eventTwo.getWhenToArrive().compareTo(eventOne.getWhenToArrive());
            }
            return dateTwo.compareTo(dateOne);
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
