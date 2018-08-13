package karsai.laszlo.bringcloser.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.EventDetail;
import karsai.laszlo.bringcloser.model.ReceivedDetail;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.ThoughtDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.model.WishDetail;
import karsai.laszlo.bringcloser.activity.WelcomeActivity;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Service to populate widget
 */
public class ReceivedBringCloserItemsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ReceivedBringCloserItemsRemoteViewFactory(getApplicationContext());
    }

    public class ReceivedBringCloserItemsRemoteViewFactory
            implements RemoteViewsService.RemoteViewsFactory{

        private Context mContext;
        private String mCurrentUserUid;
        private FirebaseDatabase mFirebaseDatabase;
        private DatabaseReference mConnectionsDatabaseRef;
        private DatabaseReference mUsersDatabaseRef;
        private List<ReceivedDetail> mReceivedWishDetailList;
        private List<ReceivedDetail> mReceivedEventDetailList;
        private List<ReceivedDetail> mReceivedThoughtDetailList;
        private List<ReceivedDetail> mDisplayedReceivedDetailList;
        private List<Wish> mWishList;
        private List<Event> mEventList;
        private List<Thought> mThoughtList;
        private CountDownLatch mCountDownLatch;
        private ValueEventListener mConnectionsValueEventListener;
        private ValueEventListener mUsersValueEventListener;

        public ReceivedBringCloserItemsRemoteViewFactory(Context context) {
            this.mContext = context;
            mDisplayedReceivedDetailList = new ArrayList<>();
        }

        @Override
        public void onCreate() {
            mCurrentUserUid = FirebaseAuth.getInstance().getUid();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mConnectionsDatabaseRef = mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.CONNECTIONS_NODE);
            mUsersDatabaseRef = mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.USERS_NODE);
            mConnectionsValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mWishList = new ArrayList<>();
                    mEventList = new ArrayList<>();
                    mThoughtList = new ArrayList<>();
                    mDisplayedReceivedDetailList.clear();
                    for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                        String key = connectionSnapshot.getKey();
                        if (key == null) {
                            Timber.wtf("key null connections node widget");
                            continue;
                        }
                        String fromUid = dataSnapshot
                                .child(key)
                                .child(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                                .getValue(String.class);
                        String toUid = dataSnapshot
                                .child(key)
                                .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                .getValue(String.class);
                        if (fromUid == null || toUid == null) {
                            Timber.wtf("widget connection pair not found");
                            continue;
                        }
                        if (fromUid.equals(mCurrentUserUid) || toUid.equals(mCurrentUserUid)) {
                            GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator
                                    = new GenericTypeIndicator<HashMap<String, Object>>() {};
                            DataSnapshot wishesSnapshot = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.WISHES_NODE);
                            DataSnapshot eventsSnapshot = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.EVENTS_NODE);
                            DataSnapshot thoughtsSnapshot = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.THOUGHTS_NODE);
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
                                        ApplicationUtils.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                                Wish wish = new Wish(
                                        (String) currWish.get(
                                                ApplicationUtils.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_WHEN_TO_ARRIVE_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils.RECEIVED_DETAIL_OCCASION_IDENTIFIER),
                                        (String) currWish.get(
                                                ApplicationUtils.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                        hasArrived != null && (boolean) hasArrived,
                                        (String) currWish.get(
                                                ApplicationUtils.RECEIVED_DETAIL_KEY_IDENTIFIER)
                                );
                                if (!wish.getFromUid().equals(mCurrentUserUid)
                                        && ApplicationUtils.isSent(wish)) {
                                    mWishList.add(wish);
                                }
                            }
                            ListIterator<HashMap<String, Object>> eventListIterator
                                    = eventMapList.listIterator();
                            while (eventListIterator.hasNext()) {
                                HashMap<String, Object> currEvent = eventListIterator.next();
                                Object hasArrived = currEvent.get(
                                        ApplicationUtils.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                                Event event = new Event(
                                        (String) currEvent.get(
                                                ApplicationUtils.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_WHEN_TO_ARRIVE_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils.RECEIVED_DETAIL_TITLE_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils.RECEIVED_DETAIL_PLACE_IDENTIFIER),
                                        (String) currEvent.get(
                                                ApplicationUtils.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                        hasArrived != null && (boolean) hasArrived,
                                        (String) currEvent.get(
                                                ApplicationUtils.RECEIVED_DETAIL_KEY_IDENTIFIER)
                                );
                                if (!event.getFromUid().equals(mCurrentUserUid)
                                        && ApplicationUtils.isSent(event)) {
                                    mEventList.add(event);
                                }
                            }
                            ListIterator<HashMap<String, Object>> thoughtListIterator
                                    = thoughtMapList.listIterator();
                            while (thoughtListIterator.hasNext()) {
                                HashMap<String, Object> currThought = thoughtListIterator.next();
                                Object hasArrived = currThought.get(
                                        ApplicationUtils.RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER);
                                Thought thought = new Thought(
                                        (String) currThought.get(
                                                ApplicationUtils.RECEIVED_DETAIL_FROM_UID_IDENTIFIER),
                                        (String) currThought.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER),
                                        (String) currThought.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER),
                                        (String) currThought.get(
                                                ApplicationUtils
                                                        .RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER),
                                        (String) currThought.get(
                                                ApplicationUtils.RECEIVED_DETAIL_TIMESTAMP_IDENTIFIER),
                                        (String) currThought.get(
                                                ApplicationUtils.RECEIVED_DETAIL_TEXT_IDENTIFIER),
                                        hasArrived != null && (boolean) hasArrived,
                                        (String) currThought.get(
                                                ApplicationUtils.RECEIVED_DETAIL_KEY_IDENTIFIER)
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
            mUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mReceivedWishDetailList.clear();
                    mReceivedEventDetailList.clear();
                    mReceivedThoughtDetailList.clear();
                    mDisplayedReceivedDetailList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String uid = snapshot.getKey();
                        if (uid == null) {
                            Timber.wtf("uid null widget getting user info");
                            continue;
                        }
                        List<String> uidList = getRelevantUidList();
                        if (uidList.contains(uid)) {
                            User user = snapshot.getValue(User.class);
                            if (user == null) {
                                Timber.wtf("user null widget");
                                continue;
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
                                                    ), ApplicationUtils.TYPE_WISH_IDENTIFIER
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
                                                    ), ApplicationUtils.TYPE_EVENT_IDENTIFIER
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
                                                    ), ApplicationUtils.TYPE_THOUGHT_IDENTIFIER
                                            )
                                    );
                                }
                            }
                        }
                    }
                    mDisplayedReceivedDetailList.addAll(mReceivedWishDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedEventDetailList);
                    mDisplayedReceivedDetailList.addAll(mReceivedThoughtDetailList);
                    Collections.sort(mDisplayedReceivedDetailList, new Comparator<ReceivedDetail>() {
                        @Override
                        public int compare(
                                ReceivedDetail receivedDetailOne,
                                ReceivedDetail receivedDetailTwo) {
                            WishDetail wishDetailOne = receivedDetailOne.getWishDetail();
                            WishDetail wishDetailTwo = receivedDetailTwo.getWishDetail();
                            EventDetail eventDetailOne = receivedDetailOne.getEventDetail();
                            EventDetail eventDetailTwo = receivedDetailTwo.getEventDetail();
                            ThoughtDetail thoughtDetailOne = receivedDetailOne.getThoughtDetail();
                            ThoughtDetail thoughtDetailTwo = receivedDetailTwo.getThoughtDetail();
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
                            Date dateOne = ApplicationUtils.getDateAndTime(dateOneAsText);
                            Date dateTwo = ApplicationUtils.getDateAndTime(dateTwoAsText);
                            if (dateOne == null || dateTwo == null) {
                                return dateTwoAsText.compareTo(dateOneAsText);
                            }
                            return dateTwo.compareTo(dateOne);
                        }
                    });
                    onLoadFinished();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        private void init() {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                mCountDownLatch.countDown();
                Intent welcomeIntent = new Intent(mContext, WelcomeActivity.class);
                mContext.startActivity(welcomeIntent);
            } else {
                if (mConnectionsDatabaseRef != null && mConnectionsValueEventListener != null) {
                    mConnectionsDatabaseRef.removeEventListener(mConnectionsValueEventListener);
                }
                if (mUsersDatabaseRef != null && mUsersValueEventListener != null) {
                    mUsersDatabaseRef.removeEventListener(mUsersValueEventListener);
                }
                loadData();
            }
        }

        private void loadData() {
            mReceivedWishDetailList = new ArrayList<>();
            mReceivedEventDetailList = new ArrayList<>();
            mReceivedThoughtDetailList = new ArrayList<>();
            mConnectionsDatabaseRef.addValueEventListener(mConnectionsValueEventListener);
        }

        private void matchDataWithUserInfo() {
            mUsersDatabaseRef.addValueEventListener(mUsersValueEventListener);
        }

        private void onLoadFinished() {
            if (mCountDownLatch.getCount() == 0) {
                Intent updateWidgetIntent
                        = new Intent(mContext, ReceivedBringCloserItemsWidgetProvider.class);
                updateWidgetIntent.setAction(ApplicationUtils.UPDATE_WIDGET_KEY);
                mContext.sendBroadcast(updateWidgetIntent);
            } else {
                mCountDownLatch.countDown();
            }
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

        @Override
        public void onDataSetChanged() {
            mCountDownLatch = new CountDownLatch(1);
            init();
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            if (mConnectionsDatabaseRef != null && mConnectionsValueEventListener != null) {
                mConnectionsDatabaseRef.removeEventListener(mConnectionsValueEventListener);
            }
            if (mUsersDatabaseRef != null && mUsersValueEventListener != null) {
                mUsersDatabaseRef.removeEventListener(mUsersValueEventListener);
            }
        }

        @Override
        public int getCount() {
            return mDisplayedReceivedDetailList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.list_item_widget);
            ReceivedDetail receivedDetail = mDisplayedReceivedDetailList.get(position);
            String type = receivedDetail.getType();
            switch (type) {
                case ApplicationUtils.TYPE_WISH_IDENTIFIER:
                    WishDetail wishDetail = receivedDetail.getWishDetail();
                    dataHandler(
                            remoteViews,
                            wishDetail.getFromName(),
                            mContext.getResources().getString(
                                    R.string.received_detail_wish).toUpperCase(Locale.getDefault()
                            ),
                            wishDetail.getText()
                    );
                    break;
                case ApplicationUtils.TYPE_EVENT_IDENTIFIER:
                    EventDetail eventDetail = receivedDetail.getEventDetail();
                    dataHandler(
                            remoteViews,
                            eventDetail.getFromName(),
                            mContext.getResources().getString(
                                    R.string.received_detail_event).toUpperCase(Locale.getDefault()
                            ),
                            eventDetail.getText()
                    );
                    break;
                default:
                    ThoughtDetail thoughtDetail = receivedDetail.getThoughtDetail();
                    dataHandler(
                            remoteViews,
                            thoughtDetail.getFromName(),
                            mContext.getResources().getString(
                                    R.string.received_detail_thought).toUpperCase(Locale.getDefault()
                            ),
                            thoughtDetail.getText()
                    );
                    break;
            }
            Intent intent = new Intent();
            intent.putExtra(ApplicationUtils.FROM_WIDGET_POS_KEY, position);
            remoteViews.setOnClickFillInIntent(R.id.rl_widget_item, intent);
            return remoteViews;
        }

        private void dataHandler(RemoteViews remoteViews, String fromName, String type, String message) {
            remoteViews.setTextViewText(R.id.tv_widget_item_type, type);
            remoteViews.setTextViewText(R.id.tv_widget_item_from_name, fromName);
            remoteViews.setTextViewText(R.id.tv_widget_item_message, message);
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
