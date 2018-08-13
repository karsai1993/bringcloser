package karsai.laszlo.bringcloser.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Adapter to handle event related information
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASIC_ON = 1;
    private static final int BASIC_OFF = 2;
    private static final int EXTRA_PHOTO_ON = 3;
    private static final int EXTRA_PHOTO_OFF = 4;

    private Context mContext;
    private List<Event> mEventList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;

    public EventAdapter(Context context, List<Event> eventList) {
        this.mContext = context;
        this.mEventList = eventList;
        this.mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.mConnectionsDatabaseRef = this.mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CONNECTIONS_NODE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case EXTRA_PHOTO_ON:
                return new WithExtraPhotoOnViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_event_extra_photo_on, parent, false)
                );
            case EXTRA_PHOTO_OFF:
                return new WithExtraPhotoOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_event_extra_photo_off, parent, false)
                );
            case BASIC_ON:
                return new BasicOnViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_event_basic_on, parent, false)
                );
            default:
                return new BasicOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_event_basic_off, parent, false)
                );
        }
    }

    @Override
    public int getItemViewType(int position) {
        Event event = mEventList.get(position);
        String photoUrl = event.getExtraPhotoUrl();
        boolean isSent = ApplicationUtils.isSent(event);
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (isSent) {
                return EXTRA_PHOTO_OFF;
            } else {
                return EXTRA_PHOTO_ON;
            }
        } else {
            if (isSent) {
                return BASIC_OFF;
            } else {
                return BASIC_ON;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Event event = mEventList.get(position);
        switch (holder.getItemViewType()) {
            case BASIC_ON:
                BasicOnViewHolder basicOnViewHolder = (BasicOnViewHolder) holder;
                basicOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOnViewHolder.titleTextView.setText(event.getTitle());
                basicOnViewHolder.titleTextView.setAlpha(1F);
                basicOnViewHolder.placeTextView.setText(event.getPlace());
                basicOnViewHolder.placeTextView.setAlpha(1F);
                final String dateAndTimeBasicOn = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        event.getWhenToArrive()
                );
                basicOnViewHolder.whenTextView.setText(dateAndTimeBasicOn);
                basicOnViewHolder.whenTextView.setAlpha(1F);
                basicOnViewHolder.messageTextView.setText(event.getText());
                basicOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(event, dateAndTimeBasicOn);
                    }
                });
                basicOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(event);
                    }
                });
                break;
            case BASIC_OFF:
                BasicOffViewHolder basicOffViewHolder = (BasicOffViewHolder) holder;
                basicOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOffViewHolder.titleTextView.setText(event.getTitle());
                basicOffViewHolder.titleTextView.setAlpha(1F);
                basicOffViewHolder.placeTextView.setText(event.getPlace());
                basicOffViewHolder.placeTextView.setAlpha(1F);
                basicOffViewHolder.whenTextView.setText(
                        ApplicationUtils.getLocalDateAndTimeToDisplay(
                                mContext,
                                event.getWhenToArrive()
                        )
                );
                basicOffViewHolder.whenTextView.setAlpha(1F);
                basicOffViewHolder.messageTextView.setText(event.getText());
                break;
            case EXTRA_PHOTO_ON:
                WithExtraPhotoOnViewHolder withExtraPhotoOnViewHolder
                        = (WithExtraPhotoOnViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        event.getExtraPhotoUrl(),
                        withExtraPhotoOnViewHolder.imageView,
                        false
                );
                withExtraPhotoOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOnViewHolder.titleTextView.setText(event.getTitle());
                withExtraPhotoOnViewHolder.titleTextView.setAlpha(1F);
                withExtraPhotoOnViewHolder.placeTextView.setText(event.getPlace());
                withExtraPhotoOnViewHolder.placeTextView.setAlpha(1F);
                final String dateAndTimePhotoOn = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        event.getWhenToArrive()
                );
                withExtraPhotoOnViewHolder.whenTextView.setText(dateAndTimePhotoOn);
                withExtraPhotoOnViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOnViewHolder.messageTextView.setText(event.getText());
                withExtraPhotoOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(event, dateAndTimePhotoOn);
                    }
                });
                withExtraPhotoOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(event);
                    }
                });
                break;
            case EXTRA_PHOTO_OFF:
                WithExtraPhotoOffViewHolder withExtraPhotoOffViewHolder
                        = (WithExtraPhotoOffViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        event.getExtraPhotoUrl(),
                        withExtraPhotoOffViewHolder.imageView,
                        false
                );
                withExtraPhotoOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOffViewHolder.titleTextView.setText(event.getTitle());
                withExtraPhotoOffViewHolder.titleTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.placeTextView.setText(event.getPlace());
                withExtraPhotoOffViewHolder.placeTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.whenTextView.setText(
                        ApplicationUtils.getLocalDateAndTimeToDisplay(
                                mContext,
                                event.getWhenToArrive()
                        )
                );
                withExtraPhotoOffViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.messageTextView.setText(event.getText());
                break;
        }
    }

    private void withdraw(final Event event, String dateAndTime) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(mContext)
                .inflate(R.layout.withdraw_given_info, null, false);
        TextView textViewOne = view.findViewById(R.id.tv_info_one);
        TextView textViewTwo = view.findViewById(R.id.tv_info_two);
        TextView textViewThree = view.findViewById(R.id.tv_info_three);
        textViewOne.setText(event.getTitle());
        textViewTwo.setText(dateAndTime);
        textViewThree.setText(event.getText());
        DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deleteFromDB(event);
                deleteFromStorage(event);
                FirebaseJobDispatcher dispatcher
                        = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
                dispatcher.cancel(
                        ApplicationUtils.getServiceUniqueTag(
                                event.getConnectionFromUid(),
                                event.getConnectionToUid(),
                                event.getKey()
                        )
                );
            }
        };
        DialogUtils.onDialogRequest(
                mContext,
                mContext.getResources().getString(R.string.dialog_event_withdraw_title),
                view,
                dialogOnPositiveBtnClickListener,
                R.style.DialogUpDownTheme
        );
    }

    private void deleteFromDB(final Event event) {
        mConnectionsDatabaseRef.orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(event.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from event adapter delete from db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null event adapter delete from db");
                                continue;
                            }
                            if (toUidValue.equals(event.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationUtils.EVENTS_NODE).child(event.getKey());
                                databaseReference.setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteFromStorage(final Event event) {
        ApplicationUtils.deleteImageFromStorage(mContext, event.getExtraPhotoUrl(), null, null);
    }

    private void editMessage(final Event event) {
        final EditText input = new EditText(mContext);
        String inputText = event.getText();
        input.setText(inputText);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String newValue = input.getText().toString();
                if (newValue.length() == 0) {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(R.string.zero_character_name_message),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    newValue = ApplicationUtils.convertTextToEmojiIfNeeded(
                            mContext,
                            newValue
                    );
                    updateDB(event, newValue);
                }
            }
        };
        DialogUtils.onDialogRequest(
                mContext,
                mContext.getResources().getString(R.string.dialog_title),
                input,
                dialogOnPositiveBtnClickListener,
                R.style.DialogLeftRightTheme
        );
    }

    private void updateDB(final Event event, final String newValue) {
        mConnectionsDatabaseRef.orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(event.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from event adapter update db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null event adapter update db");
                                continue;
                            }
                            if (toUidValue.equals(event.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationUtils.EVENTS_NODE).child(event.getKey());
                                Map<String, Object> updateValueMap = new HashMap<>();
                                updateValueMap.put(
                                        "/" + ApplicationUtils.OBJECT_TEXT_IDENTIFIER,
                                        newValue
                                );
                                databaseReference.updateChildren(updateValueMap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    class WithExtraPhotoOnViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView placeTextView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOnViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_event_extra_photo);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            customizeTextView = itemView.findViewById(R.id.tv_event_customize);
            dismissTextView = itemView.findViewById(R.id.tv_event_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_event_header);
        }
    }

    class WithExtraPhotoOffViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView placeTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOffViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_event_extra_photo);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            headerLinearLayout = itemView.findViewById(R.id.ll_event_header);
        }
    }

    class BasicOnViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView placeTextView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public BasicOnViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            customizeTextView = itemView.findViewById(R.id.tv_event_customize);
            dismissTextView = itemView.findViewById(R.id.tv_event_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_event_header);
        }
    }

    class BasicOffViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView whenTextView;
        TextView messageTextView;
        TextView placeTextView;
        LinearLayout headerLinearLayout;

        public BasicOffViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_event_title);
            whenTextView = itemView.findViewById(R.id.tv_event_when);
            messageTextView = itemView.findViewById(R.id.tv_event_message);
            placeTextView = itemView.findViewById(R.id.tv_event_place);
            headerLinearLayout = itemView.findViewById(R.id.ll_event_header);
        }
    }
}
