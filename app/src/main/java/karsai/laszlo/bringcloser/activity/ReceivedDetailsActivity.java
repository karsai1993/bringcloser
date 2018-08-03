package karsai.laszlo.bringcloser.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ReceivedDetailAdapter;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.EventDetail;
import karsai.laszlo.bringcloser.model.ReceivedDetail;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.ThoughtDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.model.WishDetail;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import timber.log.Timber;

/**
 * Activity to show the received memories of the user
 */
public class ReceivedDetailsActivity extends CommonActivity implements Comparator<ReceivedDetail>{

    @BindView(R.id.cb_wish)
    CheckBox mWishCheckBox;
    @BindView(R.id.cb_event)
    CheckBox mEventCheckBox;
    @BindView(R.id.cb_thought)
    CheckBox mThoughtCheckBox;
    @BindView(R.id.ll_layout)
    LinearLayout mLayout;
    @BindView(R.id.ll_received_details_layout)
    LinearLayout mDetailsLayout;
    @BindView(R.id.ll_received_details_sorting)
    LinearLayout mSortingLayout;
    @BindView(R.id.tv_received_details_sorted_by_value)
    TextView mSortedByValueTextView;
    @BindView(R.id.rv_received_details)
    RecyclerView mRecyclerView;
    @BindView(R.id.pb_received_details)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_received_details_no_result)
    TextView mNoResultTextView;
    @BindView(R.id.tv_received_details_no_selection)
    TextView mNoSelectionTextView;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;

    private String mCurrentUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private ValueEventListener mValueEventListener;
    private DatabaseReference mConnectionsDatabaseRef;
    private DatabaseReference mUsersDatabaseRef;
    private List<ReceivedDetail> mReceivedWishDetailList;
    private List<ReceivedDetail> mReceivedEventDetailList;
    private List<ReceivedDetail> mReceivedThoughtDetailList;
    private List<ReceivedDetail> mDisplayedReceivedDetailList;
    private List<Wish> mWishList;
    private List<Event> mEventList;
    private List<Thought> mThoughtList;
    private ReceivedDetailAdapter mReceivedDetailAdapter;
    private String mStatus;
    private int mPositionToSeek = -1;

    private final static String NONE = "000";
    private final static String WISH = "100";
    private final static String EVENT = "010";
    private final static String THOUGHT = "001";
    private final static String WISH_AND_EVENT = "110";
    private final static String WISH_AND_THOUGHT = "101";
    private final static String EVENT_AND_THOUGHT = "011";
    private static final String INSTANCE_SAVE_SORT_BY_VALUE = "instance_save_sort_by_value";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_received_details);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mStatus = "000";
        Intent receivedData = getIntent();
        if (savedInstanceState == null && receivedData != null) {
            String action = receivedData.getAction();
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_VIEW:
                        mPositionToSeek = receivedData
                                .getIntExtra(ApplicationHelper.FROM_WIDGET_POS_KEY, -1);
                        if (mPositionToSeek != -1) {
                            mStatus = "111";
                            mWishCheckBox.setChecked(true);
                            mEventCheckBox.setChecked(true);
                            mThoughtCheckBox.setChecked(true);
                            mSortedByValueTextView.setText(
                                    getResources().getString(R.string.sorted_by_time_descending)
                            );
                        }
                        break;
                    case ApplicationHelper.NOTIFICATION_INTENT_ACTION_WISH:
                        mStatus = "100";
                        mWishCheckBox.setChecked(true);
                        mEventCheckBox.setChecked(false);
                        mThoughtCheckBox.setChecked(false);
                        mSortedByValueTextView.setText(
                                getResources().getString(R.string.sorted_by_time_descending)
                        );
                        break;
                    case ApplicationHelper.NOTIFICATION_INTENT_ACTION_EVENT:
                        mStatus = "010";
                        mWishCheckBox.setChecked(false);
                        mEventCheckBox.setChecked(true);
                        mThoughtCheckBox.setChecked(false);
                        mSortedByValueTextView.setText(
                                getResources().getString(R.string.sorted_by_time_descending)
                        );
                        break;
                    case ApplicationHelper.NOTIFICATION_INTENT_ACTION_THOUGHT:
                        mStatus = "001";
                        mWishCheckBox.setChecked(false);
                        mEventCheckBox.setChecked(false);
                        mThoughtCheckBox.setChecked(true);
                        mSortedByValueTextView.setText(
                                getResources().getString(R.string.sorted_by_time_descending)
                        );
                        break;
                    default:
                        break;
                }
            }
        }
        if (savedInstanceState != null) {
            mSortedByValueTextView.setText(savedInstanceState.getString(INSTANCE_SAVE_SORT_BY_VALUE));
        }

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mStatus = "";
                mStatus += mWishCheckBox.isChecked() ? "1" : "0";
                mStatus += mEventCheckBox.isChecked() ? "1" : "0";
                mStatus += mThoughtCheckBox.isChecked() ? "1" : "0";
                displayDataBasedOnStatus();
            }
        };
        mWishCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
        mEventCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
        mThoughtCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);

        mSortingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getResources().getString(R.string.sorted_by_title);
                final String [] options
                        = getResources().getStringArray(R.array.received_detail_sort_options);
                DialogInterface.OnClickListener onClickListener
                        = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String chosenOrder = options[i];
                        mSortedByValueTextView.setText(chosenOrder);
                        applyNewOrder();
                        dialogInterface.dismiss();
                    }
                };
                DialogUtils.onDialogRequestForSorting(
                        ReceivedDetailsActivity.this,
                        title,
                        options,
                        onClickListener
                );
            }
        });

        mReceivedWishDetailList = new ArrayList<>();
        mReceivedEventDetailList = new ArrayList<>();
        mReceivedThoughtDetailList = new ArrayList<>();
        mDisplayedReceivedDetailList = new ArrayList<>();
        mReceivedDetailAdapter = new ReceivedDetailAdapter(
                this,
                mDisplayedReceivedDetailList
        );
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.rv_connection_detail_span_count),
                StaggeredGridLayoutManager.VERTICAL
        ));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mReceivedDetailAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mWishList = new ArrayList<>();
                mEventList = new ArrayList<>();
                mThoughtList = new ArrayList<>();
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null getting information about wishes, events and thoughts");
                        continue;
                    }
                    String fromUid = dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                            .getValue(String.class);
                    String toUid = dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (fromUid == null || toUid == null) return;
                    if (fromUid.equals(mCurrentUserUid) || toUid.equals(mCurrentUserUid)) {
                        GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator
                                = new GenericTypeIndicator<HashMap<String, Object>>() {};
                        DataSnapshot wishesSnapshot = dataSnapshot
                                .child(key)
                                .child(ApplicationHelper.WISHES_NODE);
                        DataSnapshot eventsSnapshot = dataSnapshot
                                .child(key)
                                .child(ApplicationHelper.EVENTS_NODE);
                        DataSnapshot thoughtsSnapshot = dataSnapshot
                                .child(key)
                                .child(ApplicationHelper.THOUGHTS_NODE);
                        List<HashMap<String, Object>> wishMapList = new ArrayList<>();
                        List<HashMap<String, Object>> eventMapList = new ArrayList<>();
                        List<HashMap<String, Object>> thoughtMapList = new ArrayList<>();
                        for (DataSnapshot wishDetailSnapshot : wishesSnapshot.getChildren()) {
                            HashMap<String, Object> wishMap
                                    = wishDetailSnapshot.getValue(genericTypeIndicator);
                            wishMapList.add(wishMap);
                        }
                        for (DataSnapshot eventDetailSnapshot : eventsSnapshot.getChildren()) {
                            HashMap<String, Object> eventMap
                                    = eventDetailSnapshot.getValue(genericTypeIndicator);
                            eventMapList.add(eventMap);
                        }
                        for (DataSnapshot thoughtDetailSnapshot : thoughtsSnapshot.getChildren()) {
                            HashMap<String, Object> thoughtMap
                                    = thoughtDetailSnapshot.getValue(genericTypeIndicator);
                            thoughtMapList.add(thoughtMap);
                        }
                        ListIterator<HashMap<String, Object>> wishListIterator
                                = wishMapList.listIterator();
                        while (wishListIterator.hasNext()) {
                            HashMap<String, Object> currWish = wishListIterator.next();
                            Object hasArrived = currWish.get(
                                    ApplicationHelper.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                            Wish wish = new Wish(
                                    (String) currWish.get(
                                            ApplicationHelper.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_WHEN_TO_ARRIVE_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper.RECEIVED_DETAIL_OCCASION_IDENTIFIER),
                                    (String) currWish.get(
                                            ApplicationHelper.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                    hasArrived != null && (boolean) hasArrived,
                                    (String) currWish.get(
                                            ApplicationHelper.RECEIVED_DETAIL_KEY_IDENTIFIER)
                            );
                            if (!wish.getFromUid().equals(mCurrentUserUid)
                                    && ApplicationHelper.isSent(wish)) {
                                mWishList.add(wish);
                            }
                        }
                        ListIterator<HashMap<String, Object>> eventListIterator
                                = eventMapList.listIterator();
                        while (eventListIterator.hasNext()) {
                            HashMap<String, Object> currEvent = eventListIterator.next();
                            Object hasArrived = currEvent.get(
                                    ApplicationHelper.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                            Event event = new Event(
                                    (String) currEvent.get(
                                            ApplicationHelper.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_WHEN_TO_ARRIVE_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper.RECEIVED_DETAIL_TITLE_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper.RECEIVED_DETAIL_PLACE_IDENTIFIER),
                                    (String) currEvent.get(
                                            ApplicationHelper.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                    hasArrived != null && (boolean) hasArrived,
                                    (String) currEvent.get(
                                            ApplicationHelper.RECEIVED_DETAIL_KEY_IDENTIFIER)
                            );
                            if (!event.getFromUid().equals(mCurrentUserUid)
                                    && ApplicationHelper.isSent(event)) {
                                mEventList.add(event);
                            }
                        }
                        ListIterator<HashMap<String, Object>> thoughtListIterator
                                = thoughtMapList.listIterator();
                        while (thoughtListIterator.hasNext()) {
                            HashMap<String, Object> currThought = thoughtListIterator.next();
                            Object hasArrived = currThought.get(
                                    ApplicationHelper.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                            Thought thought = new Thought(
                                    (String) currThought.get(
                                            ApplicationHelper.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                    (String) currThought.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                    (String) currThought.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                    (String) currThought.get(
                                            ApplicationHelper
                                                    .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                    (String) currThought.get(
                                            ApplicationHelper.RECEIVED_DETAIL_TIMESTAMP_IDENTIFIER),
                                    (String) currThought.get(
                                            ApplicationHelper.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                    hasArrived != null && (boolean) hasArrived,
                                    (String) currThought.get(
                                            ApplicationHelper.RECEIVED_DETAIL_KEY_IDENTIFIER)
                            );
                            if (!thought.getFromUid().equals(mCurrentUserUid)
                                    && thought.hasArrived()) {
                                mThoughtList.add(thought);
                            }
                        }
                    }
                }
                matchDataWithUserInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    private void applyNewOrder() {
        List<ReceivedDetail> receivedDetailList = new ArrayList<>(mDisplayedReceivedDetailList);
        mDisplayedReceivedDetailList.clear();
        Collections.sort(receivedDetailList, this);
        mDisplayedReceivedDetailList.addAll(receivedDetailList);
        mReceivedDetailAdapter.notifyDataSetChanged();
    }

    private void matchDataWithUserInfo() {
        final List<String> uidList = getRelevantUidList();
        mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReceivedWishDetailList.clear();
                mReceivedEventDetailList.clear();
                mReceivedThoughtDetailList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    if (uid == null) {
                        Timber.wtf("uid null information about user for wishes, events and thoughts");
                        continue;
                    }
                    if (uidList.contains(uid)) {
                        User user = snapshot.getValue(User.class);
                        if (user == null) {
                            Timber.wtf("user null failed to get user information");
                            return;
                        }
                        String fromPhotoUrl = user.getPhotoUrl();
                        String fromName = user.getUsername();
                        String fromUid = user.getUid();
                        for (Wish wish : mWishList) {
                            if (wish.getFromUid().equals(uid)) {
                                mReceivedWishDetailList.add(
                                        new ReceivedDetail(
                                                new WishDetail(
                                                        fromUid,
                                                        fromPhotoUrl,
                                                        fromName,
                                                        wish.getExtraPhotoUrl(),
                                                        wish.getWhenToArrive(),
                                                        wish.getOccasion(),
                                                        wish.getText()
                                                ), ApplicationHelper.TYPE_WISH_IDENTIFIER
                                        )
                                );
                            }
                        }
                        for (Event event : mEventList) {
                            if (event.getFromUid().equals(uid)) {
                                mReceivedEventDetailList.add(
                                        new ReceivedDetail(
                                                new EventDetail(
                                                        fromUid,
                                                        fromPhotoUrl,
                                                        fromName,
                                                        event.getExtraPhotoUrl(),
                                                        event.getWhenToArrive(),
                                                        event.getTitle(),
                                                        event.getPlace(),
                                                        event.getText()
                                                ), ApplicationHelper.TYPE_EVENT_IDENTIFIER
                                        )
                                );
                            }
                        }
                        for (Thought thought : mThoughtList) {
                            if (thought.getFromUid().equals(uid)) {
                                mReceivedThoughtDetailList.add(
                                        new ReceivedDetail(
                                                new ThoughtDetail(
                                                        fromUid,
                                                        fromPhotoUrl,
                                                        fromName,
                                                        thought.getExtraPhotoUrl(),
                                                        thought.getTimestamp(),
                                                        thought.getText()
                                                ), ApplicationHelper.TYPE_THOUGHT_IDENTIFIER
                                        )
                                );
                            }
                        }
                    }
                }
                mProgressBar.setVisibility(View.GONE);
                mLayout.setVisibility(View.VISIBLE);
                displayDataBasedOnStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<String> getRelevantUidList() {
        List<String> uidList = new ArrayList<>();
        for (Wish wish : mWishList) {
            String uid = wish.getFromUid();
            if (!uidList.contains(uid)) {
                uidList.add(uid);
            }
        }
        for (Event event : mEventList) {
            String uid = event.getFromUid();
            if (!uidList.contains(uid)) {
                uidList.add(uid);
            }
        }
        for (Thought thought : mThoughtList) {
            String uid = thought.getFromUid();
            if (!uidList.contains(uid)) {
                uidList.add(uid);
            }
        }
        return uidList;
    }

    private void displayDataBasedOnStatus() {
        if (mStatus.equals(NONE)) {
            mDetailsLayout.setVisibility(View.GONE);
            mNoSelectionTextView.setVisibility(View.VISIBLE);
        } else {
            mNoSelectionTextView.setVisibility(View.GONE);
            mDetailsLayout.setVisibility(View.VISIBLE);
            mDisplayedReceivedDetailList.clear();
            switch (mStatus) {
                case WISH:
                    mDisplayedReceivedDetailList.addAll(mReceivedWishDetailList);
                    break;
                case EVENT:
                    mDisplayedReceivedDetailList.addAll(mReceivedEventDetailList);
                    break;
                case THOUGHT:
                    mDisplayedReceivedDetailList.addAll(mReceivedThoughtDetailList);
                    break;
                case WISH_AND_EVENT:
                    mDisplayedReceivedDetailList.addAll(mReceivedWishDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedEventDetailList);
                    break;
                case WISH_AND_THOUGHT:
                    mDisplayedReceivedDetailList.addAll(mReceivedWishDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedThoughtDetailList);
                    break;
                case EVENT_AND_THOUGHT:
                    mDisplayedReceivedDetailList.addAll(mReceivedEventDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedThoughtDetailList);
                    break;
                default:
                    mDisplayedReceivedDetailList.addAll(mReceivedWishDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedEventDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedThoughtDetailList);
                    break;
            }
            if (mDisplayedReceivedDetailList.isEmpty()) {
                mRecyclerView.setVisibility(View.GONE);
                mNoResultTextView.setVisibility(View.VISIBLE);
            } else {
                Collections.sort(mDisplayedReceivedDetailList, this);
                mNoResultTextView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mReceivedDetailAdapter.notifyDataSetChanged();
                if (mPositionToSeek != -1) {
                    mRecyclerView.scrollToPosition(mPositionToSeek);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectionsDatabaseRef.addValueEventListener(mValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mValueEventListener != null) {
            mConnectionsDatabaseRef.removeEventListener(mValueEventListener);
        }
    }

    @Override
    public int compare(ReceivedDetail receivedDetailOne, ReceivedDetail receivedDetailTwo) {
        WishDetail wishDetailOne = receivedDetailOne.getWishDetail();
        WishDetail wishDetailTwo = receivedDetailTwo.getWishDetail();
        EventDetail eventDetailOne = receivedDetailOne.getEventDetail();
        EventDetail eventDetailTwo = receivedDetailTwo.getEventDetail();
        ThoughtDetail thoughtDetailOne = receivedDetailOne.getThoughtDetail();
        ThoughtDetail thoughtDetailTwo = receivedDetailTwo.getThoughtDetail();
        String order = mSortedByValueTextView.getText().toString();
        if (order.equals(getResources().getString(R.string.sorted_by_default))) {
            String dateOneAsText;
            String dateTwoAsText;
            if (wishDetailOne != null) {
                dateOneAsText = wishDetailOne.getWhenToArrive();
            } else if (eventDetailOne != null) {
                dateOneAsText = eventDetailOne.getWhenToArrive();
            } else {
                dateOneAsText = thoughtDetailOne.getTimestamp();
            }
            if (wishDetailTwo != null) {
                dateTwoAsText = wishDetailTwo.getWhenToArrive();
            } else if (eventDetailTwo != null) {
                dateTwoAsText = eventDetailTwo.getWhenToArrive();
            } else {
                dateTwoAsText = thoughtDetailTwo.getTimestamp();
            }
            Date dateOne = ApplicationHelper.getDateAndTime(dateOneAsText);
            Date dateTwo = ApplicationHelper.getDateAndTime(dateTwoAsText);
            if (dateOne == null || dateTwo == null) {
                return dateOneAsText.compareTo(dateTwoAsText);
            }
            return dateOne.compareTo(dateTwo);
        } else if (order.equals(getResources().getString(R.string.sorted_by_time_descending))) {
            String dateOneAsText;
            String dateTwoAsText;
            if (wishDetailOne != null) {
                dateOneAsText = wishDetailOne.getWhenToArrive();
            } else if (eventDetailOne != null) {
                dateOneAsText = eventDetailOne.getWhenToArrive();
            } else {
                dateOneAsText = thoughtDetailOne.getTimestamp();
            }
            if (wishDetailTwo != null) {
                dateTwoAsText = wishDetailTwo.getWhenToArrive();
            } else if (eventDetailTwo != null) {
                dateTwoAsText = eventDetailTwo.getWhenToArrive();
            } else {
                dateTwoAsText = thoughtDetailTwo.getTimestamp();
            }
            Date dateOne = ApplicationHelper.getDateAndTime(dateOneAsText);
            Date dateTwo = ApplicationHelper.getDateAndTime(dateTwoAsText);
            if (dateOne == null || dateTwo == null) {
                return dateTwoAsText.compareTo(dateOneAsText);
            }
            return dateTwo.compareTo(dateOne);
        } else if (order.equals(getResources().getString(R.string.received_detail_sorted_by_sender))) {
            String senderOne;
            String senderTwo;
            if (wishDetailOne != null) {
                senderOne = wishDetailOne.getFromName();
            } else if (eventDetailOne != null) {
                senderOne = eventDetailOne.getFromName();
            } else {
                senderOne = thoughtDetailOne.getFromName();
            }
            if (wishDetailTwo != null) {
                senderTwo = wishDetailTwo.getFromName();
            } else if (eventDetailTwo != null) {
                senderTwo = eventDetailTwo.getFromName();
            } else {
                senderTwo = thoughtDetailTwo.getFromName();
            }
            return senderOne.compareTo(senderTwo);
        } else {
            return receivedDetailOne.getType().compareTo(receivedDetailTwo.getType());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(INSTANCE_SAVE_SORT_BY_VALUE, mSortedByValueTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
