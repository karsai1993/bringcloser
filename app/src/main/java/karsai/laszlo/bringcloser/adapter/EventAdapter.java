package karsai.laszlo.bringcloser.adapter;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASIC_ON = 1;
    private static final int BASIC_OFF = 2;
    private static final int EXTRA_PHOTO_ON = 3;
    private static final int EXTRA_PHOTO_OFF = 4;

    private Context mContext;
    private List<Event> mEventList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;
    private SimpleDateFormat mSimpleDateFormat;

    public EventAdapter(Context context, List<Event> eventList) {
        this.mContext = context;
        this.mEventList = eventList;
        this.mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.mConnectionsDatabaseRef = this.mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mSimpleDateFormat
                = new SimpleDateFormat(ApplicationHelper.DATE_PATTERN_FULL, Locale.getDefault());
        mSimpleDateFormat.setTimeZone(TimeZone.getDefault());
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
        boolean hasArrived = event.hasArrived();
        String targetTime = event.getWhenToArrive();
        boolean isExpired;
        try {
            Date targetDate = mSimpleDateFormat.parse(targetTime);
            Date currentDate = new Date();
            if (currentDate.after(targetDate)) {
                isExpired = true;
            } else {
                isExpired = false;
            }
        } catch (ParseException e) {
            isExpired = false;
        }
        boolean isSent = hasArrived || isExpired;
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
                basicOnViewHolder.whenTextView.setText(
                        event.getWhenToArrive().replaceAll(ApplicationHelper.DATE_SPLITTER, " ")
                );
                basicOnViewHolder.whenTextView.setAlpha(1F);
                basicOnViewHolder.messageTextView.setText(event.getText());
                basicOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(event);
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
                        event.getWhenToArrive().replaceAll(ApplicationHelper.DATE_SPLITTER, " ")
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
                withExtraPhotoOnViewHolder.whenTextView.setText(
                        event.getWhenToArrive().replaceAll(ApplicationHelper.DATE_SPLITTER, " ")
                );
                withExtraPhotoOnViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOnViewHolder.messageTextView.setText(event.getText());
                withExtraPhotoOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(event);
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
                        event.getWhenToArrive().replaceAll(ApplicationHelper.DATE_SPLITTER, " ")
                );
                withExtraPhotoOffViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.messageTextView.setText(event.getText());
                break;
        }
    }

    private void withdraw(final Event event) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.withdraw_given_info, null, false);
        TextView textViewOne = view.findViewById(R.id.tv_info_one);
        TextView textViewTwo = view.findViewById(R.id.tv_info_two);
        TextView textViewThree = view.findViewById(R.id.tv_info_three);
        textViewOne.setText(event.getTitle());
        textViewTwo.setText(
                event.getWhenToArrive().replaceAll(ApplicationHelper.DATE_SPLITTER, " "));
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
                        ApplicationHelper.getServiceUniqueTag(
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
        mConnectionsDatabaseRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(event.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class)
                                    .equals(event.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationHelper.EVENTS_NODE).child(event.getKey());
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
        ApplicationHelper.deleteImageFromStorage(mContext, event.getExtraPhotoUrl());
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
        mConnectionsDatabaseRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(event.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class)
                                    .equals(event.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationHelper.EVENTS_NODE).child(event.getKey());
                                Map<String, Object> updateValueMap = new HashMap<>();
                                updateValueMap.put(
                                        "/" + ApplicationHelper.OBJECT_TEXT_IDENTIFIER,
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
