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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ThoughtAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.activity.AddNewThoughtActivity;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import timber.log.Timber;

/**
 * Fragment to handle connection thoughts related information
 */
public class ConnectionThoughtFragment extends Fragment implements Comparator<Thought>{

    private LinearLayout mThoughtDataLinearLayout;
    private TextView mThoughtSortedByValueTextView;
    private LinearLayout mThoughtSortingLinearLayout;
    private RecyclerView mThoughtRecyclerView;
    private TextView mThoughtNoDataTextView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlusOneThoughtFab;
    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private ConnectionDetail mConnectionDetail;
    private List<Thought> mThoughtList;
    private ThoughtAdapter mThoughtAdapter;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionThoughtsValueEventListener;
    private DatabaseReference mThoughtsDatabaseRef;
    private Activity mActivity;
    private Context mSavedAppContext;

    private static final String INSTANCE_SAVE_SORT_BY_VALUE = "instance_save_sort_by_value";

    public ConnectionThoughtFragment() {}


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView
                = inflater.inflate(R.layout.fragment_connection_thought, container, false);
        mThoughtDataLinearLayout = rootView.findViewById(R.id.ll_thought_layout);
        mThoughtSortedByValueTextView = rootView.findViewById(R.id.tv_thought_sorted_by_value);
        mThoughtSortingLinearLayout = rootView.findViewById(R.id.ll_thought_sorting);
        mThoughtRecyclerView = rootView.findViewById(R.id.rv_thought);
        mProgressBar = rootView.findViewById(R.id.pb_thought);
        mThoughtNoDataTextView = rootView.findViewById(R.id.tv_thought_no_data);

        mSavedAppContext = getContext();

        if (savedInstanceState != null) {
            mThoughtSortedByValueTextView.setText(savedInstanceState.getString(INSTANCE_SAVE_SORT_BY_VALUE));
        }

        mThoughtRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                Objects.requireNonNull(getContext()).getResources().getInteger(R.integer.rv_connection_detail_span_count),
                StaggeredGridLayoutManager.VERTICAL
        ));
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
                    Intent intent = new Intent(getContext(), AddNewThoughtActivity.class);
                    intent.putExtra(ApplicationHelper.EXTRA_DATA, mConnectionDetail);
                    startActivity(intent);
                }
            });
            mPlusOneThoughtFab.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        mThoughtRecyclerView.requestFocus();
                    }
                    return false;
                }
            });
        }

        mThoughtRecyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                        && mPlusOneThoughtFab != null) {
                    mPlusOneThoughtFab.requestFocus();
                }
                return false;
            }
        });

        mThoughtSortingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String title = getContext().getString(R.string.sorted_by_title);
                final String [] options
                        = getContext().getResources().getStringArray(R.array.thought_sort_options);
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String chosenOrder = options[i];
                        mThoughtSortedByValueTextView.setText(chosenOrder);
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
        mConnectionThoughtsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null connection from thought");
                        continue;
                    }
                    String toUidValue = dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) {
                        Timber.wtf("to uid null");
                        continue;
                    }
                    if (toUidValue.equals(mConnectionDetail.getToUid())) {
                        mThoughtsDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.THOUGHTS_NODE);
                        mThoughtsDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mThoughtList.clear();
                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                    Thought thought = eventSnapshot.getValue(Thought.class);
                                    if (thought == null) {
                                        Timber.wtf("thought null");
                                        continue;
                                    }
                                    if (thought.getFromUid().equals(mCurrentUserUid)) {
                                        mThoughtList.add(thought);
                                    }
                                }

                                Collections.sort(mThoughtList, ConnectionThoughtFragment.this);

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

    private void applyNewOrder() {
        List<Thought> thoughtList = new ArrayList<>(mThoughtList);
        mThoughtList.clear();
        Collections.sort(thoughtList, this);
        mThoughtList.addAll(thoughtList);
        mThoughtAdapter.notifyDataSetChanged();
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

    @Override
    public int compare(Thought thoughtOne, Thought thoughtTwo) {
        String order = mThoughtSortedByValueTextView.getText().toString();
        Context context = getContext();
        if (context == null) {
            context = mSavedAppContext;
        }
        if (order.equals(context.getResources().getString(R.string.sorted_by_default))) {
            Date dateOne = ApplicationHelper.getDateAndTime(
                    thoughtOne.getTimestamp()
            );
            Date dateTwo = ApplicationHelper.getDateAndTime(
                    thoughtTwo.getTimestamp()
            );
            if (dateOne == null || dateTwo == null) {
                return thoughtOne.getTimestamp().compareTo(thoughtTwo.getTimestamp());
            }
            return dateOne.compareTo(dateTwo);
        } else if (order.equals(context.getResources()
                .getString(R.string.sorted_by_time_descending))) {
            Date dateOne = ApplicationHelper.getDateAndTime(
                    thoughtOne.getTimestamp()
            );
            Date dateTwo = ApplicationHelper.getDateAndTime(
                    thoughtTwo.getTimestamp()
            );
            if (dateOne == null || dateTwo == null) {
                return thoughtTwo.getTimestamp().compareTo(thoughtOne.getTimestamp());
            }
            return dateTwo.compareTo(dateOne);
        } else {
            return thoughtOne.getText().compareToIgnoreCase(thoughtTwo.getText());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(INSTANCE_SAVE_SORT_BY_VALUE, mThoughtSortedByValueTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
