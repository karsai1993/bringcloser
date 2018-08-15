package karsai.laszlo.bringcloser.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.EventDetail;
import karsai.laszlo.bringcloser.model.ReceivedDetail;
import karsai.laszlo.bringcloser.model.ThoughtDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.WishDetail;
import karsai.laszlo.bringcloser.activity.ConnectionActivity;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Adapter to handle received memories related information
 */
public class ReceivedDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int WISH_BASIC_VIEW_TYPE = 1;
    private static final int WISH_EXTRA_PHOTO_VIEW_TYPE = 2;
    private static final int EVENT_BASIC_VIEW_TYPE = 3;
    private static final int EVENT_EXTRA_PHOTO_VIEW_TYPE = 4;
    private static final int THOUGHT_BASIC_VIEW_TYPE = 5;
    private static final int THOUGHT_EXTRA_PHOTO_VIEW_TYPE = 6;

    private Context mContext;
    private List<ReceivedDetail> mReceivedDetailList;

    public ReceivedDetailAdapter(Context context, List<ReceivedDetail> receivedDetailList) {
        this.mContext = context;
        this.mReceivedDetailList = receivedDetailList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case WISH_BASIC_VIEW_TYPE:
                return new WishBasicViewHolder(
                        LayoutInflater.from(mContext)
                        .inflate(R.layout.list_item_received_wish_basic, parent, false)
                );
            case WISH_EXTRA_PHOTO_VIEW_TYPE:
                return new WishExtraPhotoViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_received_wish_extra_photo, parent, false)
                );
            case EVENT_BASIC_VIEW_TYPE:
                return new EventBasicViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_received_event_basic, parent, false)
                );
            case EVENT_EXTRA_PHOTO_VIEW_TYPE:
                return new EventExtraPhotoViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_received_event_extra_photo, parent, false)
                );
            case THOUGHT_BASIC_VIEW_TYPE:
                return new ThoughtBasicViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_received_thought_basic, parent, false)
                );
            default:
                return new ThoughtExtraPhotoViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_received_thought_extra_photo, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReceivedDetail receivedDetail = mReceivedDetailList.get(position);
        final WishDetail wishDetail = receivedDetail.getWishDetail();
        final EventDetail eventDetail = receivedDetail.getEventDetail();
        final ThoughtDetail thoughtDetail = receivedDetail.getThoughtDetail();
        final int viewType = holder.getItemViewType();
        switch (viewType) {
            case WISH_BASIC_VIEW_TYPE:
                WishBasicViewHolder wishBasicViewHolder = (WishBasicViewHolder) holder;
                wishBasicViewHolder.infoHeaderLayout.setAlpha(0.75F);
                wishBasicViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wishDetail.getOccasion())
                );
                wishBasicViewHolder.occasionTextView.setAlpha(1F);
                final String wishBasicDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        wishDetail.getWhenToArrive()
                );
                wishBasicViewHolder.whenTextView.setText(wishBasicDate);
                wishBasicViewHolder.whenTextView.setAlpha(1F);
                wishBasicViewHolder.messageTextView.setText(wishDetail.getText());
                wishBasicViewHolder.fromNameTextView.setText(wishDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        wishDetail.getFromPhotoUrl(),
                        wishBasicViewHolder.fromPhotoImageView,
                        true
                );
                wishBasicViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareReceivedItem(
                                viewType,
                                wishDetail.getExtraPhotoUrl(),
                                wishDetail.getText());
                    }
                });
                wishBasicViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(wishDetail.getFromUid());
                            }
                        }
                );
                break;
            case WISH_EXTRA_PHOTO_VIEW_TYPE:
                WishExtraPhotoViewHolder wishExtraPhotoViewHolder = (WishExtraPhotoViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        wishDetail.getExtraPhotoUrl(),
                        wishExtraPhotoViewHolder.imageView,
                        false
                );
                wishExtraPhotoViewHolder.infoHeaderLayout.setAlpha(0.75F);
                wishExtraPhotoViewHolder.typeHeaderLayout.setAlpha(0.75F);
                wishExtraPhotoViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wishDetail.getOccasion())
                );
                wishExtraPhotoViewHolder.occasionTextView.setAlpha(1F);
                final String wishExtraDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        wishDetail.getWhenToArrive()
                );
                wishExtraPhotoViewHolder.whenTextView.setText(wishExtraDate);
                wishExtraPhotoViewHolder.whenTextView.setAlpha(1F);
                wishExtraPhotoViewHolder.messageTextView.setText(wishDetail.getText());
                wishExtraPhotoViewHolder.fromNameTextView.setText(wishDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        wishDetail.getFromPhotoUrl(),
                        wishExtraPhotoViewHolder.fromPhotoImageView,
                        true
                );
                wishExtraPhotoViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareReceivedItem(
                                        viewType,
                                        wishDetail.getExtraPhotoUrl(),
                                        wishDetail.getText());
                            }
                        });
                wishExtraPhotoViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(wishDetail.getFromUid());
                            }
                        }
                );
                break;
            case EVENT_BASIC_VIEW_TYPE:
                EventBasicViewHolder eventBasicViewHolder = (EventBasicViewHolder) holder;
                eventBasicViewHolder.infoHeaderLayout.setAlpha(0.75F);
                eventBasicViewHolder.titleTextView.setText(eventDetail.getTitle());
                eventBasicViewHolder.titleTextView.setAlpha(1F);
                eventBasicViewHolder.placeTextView.setText(eventDetail.getPlace());
                eventBasicViewHolder.placeTextView.setAlpha(1F);
                final String eventBasicDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        eventDetail.getWhenToArrive()
                );
                eventBasicViewHolder.whenTextView.setText(eventBasicDate);
                eventBasicViewHolder.whenTextView.setAlpha(1F);
                eventBasicViewHolder.messageTextView.setText(eventDetail.getText());
                eventBasicViewHolder.fromNameTextView.setText(eventDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        eventDetail.getFromPhotoUrl(),
                        eventBasicViewHolder.fromPhotoImageView,
                        true
                );
                eventBasicViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareReceivedItem(
                                        viewType,
                                        null,
                                        eventDetail.getText());
                            }
                        });
                eventBasicViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(eventDetail.getFromUid());
                            }
                        }
                );
                break;
            case EVENT_EXTRA_PHOTO_VIEW_TYPE:
                EventExtraPhotoViewHolder eventExtraPhotoViewHolder = (EventExtraPhotoViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        eventDetail.getExtraPhotoUrl(),
                        eventExtraPhotoViewHolder.imageView,
                        false
                );
                eventExtraPhotoViewHolder.infoHeaderLayout.setAlpha(0.75F);
                eventExtraPhotoViewHolder.typeHeaderLayout.setAlpha(0.75F);
                eventExtraPhotoViewHolder.titleTextView.setText(eventDetail.getTitle());
                eventExtraPhotoViewHolder.titleTextView.setAlpha(1F);
                eventExtraPhotoViewHolder.placeTextView.setText(eventDetail.getPlace());
                eventExtraPhotoViewHolder.placeTextView.setAlpha(1F);
                final String eventExtraDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        eventDetail.getWhenToArrive()
                );
                eventExtraPhotoViewHolder.whenTextView.setText(eventExtraDate);
                eventExtraPhotoViewHolder.whenTextView.setAlpha(1F);
                eventExtraPhotoViewHolder.messageTextView.setText(eventDetail.getText());
                eventExtraPhotoViewHolder.fromNameTextView.setText(eventDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        eventDetail.getFromPhotoUrl(),
                        eventExtraPhotoViewHolder.fromPhotoImageView,
                        true
                );
                eventExtraPhotoViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareReceivedItem(
                                        viewType,
                                        eventDetail.getExtraPhotoUrl(),
                                        eventDetail.getText());
                            }
                        });
                eventExtraPhotoViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(eventDetail.getFromUid());
                            }
                        }
                );
                break;
            case THOUGHT_BASIC_VIEW_TYPE:
                ThoughtBasicViewHolder thoughtBasicViewHolder = (ThoughtBasicViewHolder) holder;
                thoughtBasicViewHolder.infoHeaderLayout.setAlpha(0.75F);
                final String thoughtBasicDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        thoughtDetail.getTimestamp()
                );
                thoughtBasicViewHolder.timestampTextView.setText(thoughtBasicDate);
                thoughtBasicViewHolder.timestampTextView.setAlpha(1F);
                thoughtBasicViewHolder.messageTextView.setText(thoughtDetail.getText());
                thoughtBasicViewHolder.fromNameTextView.setText(thoughtDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        thoughtDetail.getFromPhotoUrl(),
                        thoughtBasicViewHolder.fromPhotoImageView,
                        true
                );
                thoughtBasicViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareReceivedItem(
                                        viewType,
                                        null,
                                        thoughtDetail.getText());
                            }
                        });
                thoughtBasicViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(thoughtDetail.getFromUid());
                            }
                        }
                );
                break;
            default:
                ThoughtExtraPhotoViewHolder thoughtExtraPhotoViewHolder
                        = (ThoughtExtraPhotoViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        thoughtDetail.getExtraPhotoUrl(),
                        thoughtExtraPhotoViewHolder.imageView,
                        false
                );
                thoughtExtraPhotoViewHolder.infoHeaderLayout.setAlpha(0.75F);
                thoughtExtraPhotoViewHolder.typeHeaderLayout.setAlpha(0.75F);
                final String thoughtExtraDate = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        thoughtDetail.getTimestamp()
                );
                thoughtExtraPhotoViewHolder.timestampTextView.setText(thoughtExtraDate);
                thoughtExtraPhotoViewHolder.timestampTextView.setAlpha(1F);
                thoughtExtraPhotoViewHolder.messageTextView.setText(thoughtDetail.getText());
                thoughtExtraPhotoViewHolder.fromNameTextView.setText(thoughtDetail.getFromName());
                ImageUtils.setPhoto(
                        mContext,
                        thoughtDetail.getFromPhotoUrl(),
                        thoughtExtraPhotoViewHolder.fromPhotoImageView,
                        true
                );
                thoughtExtraPhotoViewHolder.shareReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareReceivedItem(
                                        viewType,
                                        thoughtDetail.getExtraPhotoUrl(),
                                        thoughtDetail.getText());
                            }
                        });
                thoughtExtraPhotoViewHolder.chatReceivedItemImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openChat(thoughtDetail.getFromUid());
                            }
                        }
                );
                break;
        }
    }

    private void openChat(final String fromUid) {
        final String currentUid = FirebaseAuth.getInstance().getUid();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference connectionsDatabaseRef = firebaseDatabase.getReference()
                .child(ApplicationUtils.CONNECTIONS_NODE);
        connectionsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Connection currentConnection = null;
                for (DataSnapshot connSnapshot : dataSnapshot.getChildren()) {
                    Connection connection = connSnapshot.getValue(Connection.class);
                    if (connection == null) {
                        Timber.wtf("connection null openchat");
                        continue;
                    }
                    if (connection.getConnectionBit() == 1
                            && ((connection.getToUid().equals(currentUid)
                            && connection.getFromUid().equals(fromUid)))
                            || (connection.getToUid().equals(fromUid)
                            && connection.getFromUid().equals(currentUid))) {
                        currentConnection = connection;
                    }
                }
                if (currentConnection != null) {
                    DatabaseReference usersDatabaseRef = firebaseDatabase.getReference()
                            .child(ApplicationUtils.USERS_NODE);
                    final Connection finalCurrentConnection = currentConnection;
                    usersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String fromUid = finalCurrentConnection.getFromUid();
                            String toUid = finalCurrentConnection.getToUid();
                            String type = finalCurrentConnection.getType();
                            String timestamp = finalCurrentConnection.getTimestamp();
                            ConnectionDetail connectionDetail = new ConnectionDetail();
                            boolean isFromDataRead = false;
                            boolean isToDataRead = false;
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                User user = userSnapshot.getValue(User.class);
                                if (user == null) {
                                    Timber.wtf("user null openchat");
                                    continue;
                                }
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
                                    Intent intent = new Intent(mContext, ConnectionActivity.class);
                                    intent.putExtra(
                                            ApplicationUtils.CONNECTION_KEY, connectionDetail);
                                    mContext.startActivity(intent);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void shareReceivedItem(
            int viewType,
            String extraPhotoUrl,
            String message) {
        String mimeType = "text/plain";
        String type;
        if (viewType == WISH_BASIC_VIEW_TYPE || viewType == WISH_EXTRA_PHOTO_VIEW_TYPE) {
            type = mContext.getResources().getString(R.string.received_detail_wish);
        } else if (viewType == EVENT_BASIC_VIEW_TYPE || viewType == EVENT_EXTRA_PHOTO_VIEW_TYPE) {
            type = mContext.getResources().getString(R.string.received_detail_event);
        } else {
            type = mContext.getResources().getString(R.string.received_detail_thought);
        }
        String lowerCaseType = type.toLowerCase(Locale.getDefault());
        String title = new StringBuilder()
                .append(mContext.getResources().getString(R.string.share_title_received_item_1))
                .append(lowerCaseType)
                .append(mContext.getResources().getString(R.string.share_title_received_item_2))
                .toString();
        Activity activity = (Activity) mContext;
        String content;
        if (extraPhotoUrl == null) {
            content = new StringBuilder()
                    .append(message)
                    .append("\n\n")
                    .append(mContext.getResources().getString(R.string.share_content_received_item))
                    .toString();
        } else {
            content = new StringBuilder()
                    .append(message)
                    .append("\n")
                    .append(extraPhotoUrl)
                    .append("\n\n")
                    .append(mContext.getResources().getString(R.string.share_content_received_item))
                    .toString();
        }
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(activity)
                .setChooserTitle(title)
                .setType(mimeType)
                .setSubject(type)
                .setText(content)
                .createChooserIntent();
        if (shareIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(shareIntent);
        }
    }

    @Override
    public int getItemCount() {
        return mReceivedDetailList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ReceivedDetail receivedDetail = mReceivedDetailList.get(position);
        String type = receivedDetail.getType();
        switch (type) {
            case ApplicationUtils.TYPE_WISH_IDENTIFIER:
                WishDetail wishDetail = receivedDetail.getWishDetail();
                if (wishDetail.getExtraPhotoUrl() == null) {
                    return WISH_BASIC_VIEW_TYPE;
                } else {
                    return WISH_EXTRA_PHOTO_VIEW_TYPE;
                }
            case ApplicationUtils.TYPE_EVENT_IDENTIFIER:
                EventDetail eventDetail = receivedDetail.getEventDetail();
                if (eventDetail.getExtraPhotoUrl() == null) {
                    return EVENT_BASIC_VIEW_TYPE;
                } else {
                    return EVENT_EXTRA_PHOTO_VIEW_TYPE;
                }
            default:
                ThoughtDetail thoughtDetail = receivedDetail.getThoughtDetail();
                if (thoughtDetail.getExtraPhotoUrl() == null) {
                    return THOUGHT_BASIC_VIEW_TYPE;
                } else {
                    return THOUGHT_EXTRA_PHOTO_VIEW_TYPE;
                }
        }
    }

    class WishBasicViewHolder extends RecyclerView.ViewHolder {

        LinearLayout infoHeaderLayout;
        TextView occasionTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public WishBasicViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            infoHeaderLayout = itemView.findViewById(R.id.ll_wish_header);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            messageTextView = itemView.findViewById(R.id.tv_wish_text);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
    class WishExtraPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        LinearLayout infoHeaderLayout;
        TextView occasionTextView;
        TextView whenTextView;
        TextView messageTextView;
        LinearLayout typeHeaderLayout;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public WishExtraPhotoViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            imageView = itemView.findViewById(R.id.iv_wish_extra_photo);
            infoHeaderLayout = itemView.findViewById(R.id.ll_wish_header);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            typeHeaderLayout = itemView.findViewById(R.id.ll_wish_type_header);
            messageTextView = itemView.findViewById(R.id.tv_wish_text);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
    class EventBasicViewHolder extends RecyclerView.ViewHolder {

        LinearLayout infoHeaderLayout;
        TextView titleTextView;
        TextView placeTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public EventBasicViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            infoHeaderLayout = itemView.findViewById(R.id.ll_event_header);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
    class EventExtraPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        LinearLayout infoHeaderLayout;
        TextView titleTextView;
        TextView placeTextView;
        TextView whenTextView;
        LinearLayout typeHeaderLayout;
        TextView messageTextView;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public EventExtraPhotoViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            imageView = itemView.findViewById(R.id.iv_event_extra_photo);
            infoHeaderLayout = itemView.findViewById(R.id.ll_event_header);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            typeHeaderLayout = itemView.findViewById(R.id.ll_event_type_header);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
    class ThoughtBasicViewHolder extends RecyclerView.ViewHolder {

        LinearLayout infoHeaderLayout;
        TextView timestampTextView;
        TextView messageTextView;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public ThoughtBasicViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            infoHeaderLayout = itemView.findViewById(R.id.ll_thought_header);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
    class ThoughtExtraPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        LinearLayout infoHeaderLayout;
        TextView timestampTextView;
        LinearLayout typeHeaderLayout;
        TextView messageTextView;
        TextView fromNameTextView;
        ImageView fromPhotoImageView;
        ImageView shareReceivedItemImageView;
        ImageView chatReceivedItemImageView;

        public ThoughtExtraPhotoViewHolder(View itemView) {
            super(itemView);
            shareReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_share);
            chatReceivedItemImageView = itemView.findViewById(R.id.iv_received_item_chat);
            imageView = itemView.findViewById(R.id.iv_thought_extra_photo);
            infoHeaderLayout = itemView.findViewById(R.id.ll_thought_header);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            typeHeaderLayout = itemView.findViewById(R.id.ll_thought_type_header);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            fromNameTextView = itemView.findViewById(R.id.tv_received_name);
            fromPhotoImageView = itemView.findViewById(R.id.iv_received_photo);
        }
    }
}
