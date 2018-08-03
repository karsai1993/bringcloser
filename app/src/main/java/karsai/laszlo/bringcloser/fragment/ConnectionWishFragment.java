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
import karsai.laszlo.bringcloser.adapter.WishAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.activity.AddNewWishActivity;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import timber.log.Timber;

/**
 * Fragment to handle connection wishes related information
 */
public class ConnectionWishFragment extends Fragment implements Comparator<Wish>{

    private LinearLayout mWishDataLinearLayout;
    private TextView mWishSortedByValueTextView;
    private LinearLayout mWishSortingLinearLayout;
    private RecyclerView mWishRecyclerView;
    private TextView mWishNoDataTextView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlusOneWishFab;
    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ConnectionDetail mConnectionDetail;
    private List<Wish> mWishList;
    private WishAdapter mWishAdapter;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionWishesValueEventListener;
    private DatabaseReference mWishesDatabaseRef;
    private Activity mActivity;
    private Context mSavedContext;

    private static final String INSTANCE_SAVE_SORT_BY_VALUE = "instance_save_sort_by_value";

    public ConnectionWishFragment() {}

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView
                = inflater.inflate(R.layout.fragment_connection_wish, container, false);
        mWishDataLinearLayout = rootView.findViewById(R.id.ll_wish_layout);
        mWishSortedByValueTextView = rootView.findViewById(R.id.tv_wish_sorted_by_value);
        mWishSortingLinearLayout = rootView.findViewById(R.id.ll_wish_sorting);
        mWishRecyclerView = rootView.findViewById(R.id.rv_wish);
        mProgressBar = rootView.findViewById(R.id.pb_wish);
        mWishNoDataTextView = rootView.findViewById(R.id.tv_wish_no_data);

        mSavedContext = getContext();

        if (savedInstanceState != null) {
            mWishSortedByValueTextView.setText(savedInstanceState.getString(INSTANCE_SAVE_SORT_BY_VALUE));
        }

        mWishRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                Objects.requireNonNull(getContext()).getResources().getInteger(R.integer.rv_connection_detail_span_count),
                StaggeredGridLayoutManager.VERTICAL
        ));
        mWishRecyclerView.setHasFixedSize(true);
        mWishRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0
                        && mPlusOneWishFab != null
                        && mPlusOneWishFab.getVisibility() == View.VISIBLE) {
                    mPlusOneWishFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0
                        && mPlusOneWishFab != null
                        && mPlusOneWishFab.getVisibility() != View.VISIBLE) {
                    mPlusOneWishFab.setVisibility(View.VISIBLE);
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

        mWishList = new ArrayList<>();
        mWishAdapter = new WishAdapter(
                getContext(),
                mWishList
        );
        mWishRecyclerView.setAdapter(mWishAdapter);

        mActivity = getActivity();
        if (mActivity != null) {
            mPlusOneWishFab = mActivity.findViewById(R.id.fab_plus_one_wish);
            mPlusOneWishFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String type = mConnectionDetail.getType();
                    Intent intent = new Intent(getContext(), AddNewWishActivity.class);
                    intent.putExtra(ApplicationHelper.EXTRA_DATA, mConnectionDetail);
                    intent.putExtra(ApplicationHelper.EXTRA_TYPE, type);
                    startActivity(intent);
                }
            });
            mPlusOneWishFab.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        mWishRecyclerView.requestFocus();
                    }
                    return false;
                }
            });
        }

        mWishRecyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                        && mPlusOneWishFab != null) {
                    mPlusOneWishFab.requestFocus();
                }
                return false;
            }
        });

        mWishSortingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String title = getContext().getString(R.string.sorted_by_title);
                final String [] options
                        = getContext().getResources().getStringArray(R.array.wish_sort_options);
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String chosenOrder = options[i];
                        mWishSortedByValueTextView.setText(chosenOrder);
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
        mConnectionWishesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null connection wish from uid");
                        continue;
                    }
                    String toUidValue = dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) {
                        Timber.wtf("to uid null connection wish");
                        continue;
                    }
                    if (toUidValue.equals(mConnectionDetail.getToUid())) {
                        mWishesDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.WISHES_NODE);
                        mWishesDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mWishList.clear();
                                for (DataSnapshot wishSnapshot : dataSnapshot.getChildren()) {
                                    Wish wish = wishSnapshot.getValue(Wish.class);
                                    if (wish == null) {
                                        Timber.wtf("connection wish null");
                                        continue;
                                    }
                                    if (wish.getFromUid().equals(mCurrentUserUid)) {
                                        mWishList.add(wish);
                                    }
                                }

                                Collections.sort(mWishList, ConnectionWishFragment.this);

                                mProgressBar.setVisibility(View.GONE);
                                int wishNum = mWishList.size();
                                if (wishNum > 0) {
                                    mWishAdapter.notifyDataSetChanged();
                                    mWishNoDataTextView.setVisibility(View.GONE);
                                    mWishDataLinearLayout.setVisibility(View.VISIBLE);
                                } else {
                                    mWishDataLinearLayout.setVisibility(View.GONE);
                                    mWishNoDataTextView.setVisibility(View.VISIBLE);
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
        mWishDataLinearLayout.setVisibility(View.GONE);

        return rootView;
    }

    private void applyNewOrder() {
        List<Wish> wishList = new ArrayList<>(mWishList);
        mWishList.clear();
        Collections.sort(wishList, this);
        mWishList.addAll(wishList);
        mWishAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionWishesValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mConnectionWishesValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionWishesValueEventListener);
        }
    }

    @Override
    public int compare(Wish wishOne, Wish wishTwo) {
        String order = mWishSortedByValueTextView.getText().toString();
        Context context = getContext();
        if (context == null) {
            context = mSavedContext;
        }
        if (order.equals(context.getResources().getString(R.string.sorted_by_default))) {
            Date dateOne = ApplicationHelper.getDateAndTime(
                    wishOne.getWhenToArrive()
            );
            Date dateTwo = ApplicationHelper.getDateAndTime(
                    wishTwo.getWhenToArrive()
            );
            if (dateOne == null || dateTwo == null) {
                return wishOne.getWhenToArrive().compareTo(wishTwo.getWhenToArrive());
            }
            return dateOne.compareTo(dateTwo);
        } else if (order.equals(context.getResources()
                .getString(R.string.sorted_by_time_descending))) {
            Date dateOne = ApplicationHelper.getDateAndTime(
                    wishOne.getWhenToArrive()
            );
            Date dateTwo = ApplicationHelper.getDateAndTime(
                    wishTwo.getWhenToArrive()
            );
            if (dateOne == null || dateTwo == null) {
                return wishTwo.getWhenToArrive().compareTo(wishOne.getWhenToArrive());
            }
            return dateTwo.compareTo(dateOne);
        } else {
            return wishOne.getOccasion().compareToIgnoreCase(wishTwo.getOccasion());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(INSTANCE_SAVE_SORT_BY_VALUE, mWishSortedByValueTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
