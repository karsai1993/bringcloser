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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Adapter to handle thought related information
 */
public class ThoughtAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASIC_ON = 1;
    private static final int BASIC_OFF = 2;
    private static final int EXTRA_PHOTO_ON = 3;
    private static final int EXTRA_PHOTO_OFF = 4;

    private Context mContext;
    private List<Thought> mThoughtList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;

    public ThoughtAdapter(Context context, List<Thought> thoughtList) {
        this.mContext = context;
        this.mThoughtList = thoughtList;
        this.mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.mConnectionsDatabaseRef = this.mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case EXTRA_PHOTO_ON:
                return new WithExtraPhotoOnViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_thought_extra_photo_on, parent, false)
                );
            case EXTRA_PHOTO_OFF:
                return new WithExtraPhotoOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_thought_extra_photo_off, parent, false)
                );
            case BASIC_ON:
                return new BasicOnViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_thought_basic_on, parent, false)
                );
            default:
                return new BasicOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_thought_basic_off, parent, false)
                );
        }
    }

    @Override
    public int getItemViewType(int position) {
        Thought thought = mThoughtList.get(position);
        String photoUrl = thought.getExtraPhotoUrl();
        boolean hasArrived = thought.hasArrived();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (hasArrived) {
                return EXTRA_PHOTO_OFF;
            } else {
                return EXTRA_PHOTO_ON;
            }
        } else {
            if (hasArrived) {
                return BASIC_OFF;
            } else {
                return BASIC_ON;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Thought thought = mThoughtList.get(position);
        switch (holder.getItemViewType()) {
            case BASIC_ON:
                final BasicOnViewHolder basicOnViewHolder = (BasicOnViewHolder) holder;
                basicOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOnViewHolder.messageTextView.setText(thought.getText());
                final String dateAndTimeBasicOn = ApplicationHelper.getLocalDateAndTimeToDisplay(
                        mContext,
                        thought.getTimestamp()
                );
                basicOnViewHolder.switchView.setChecked(false);
                basicOnViewHolder.switchView.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked) {
                            sendThought(thought, basicOnViewHolder.switchView, dateAndTimeBasicOn);
                        }
                    }
                });
                basicOnViewHolder.timestampTextView.setText(dateAndTimeBasicOn);
                basicOnViewHolder.timestampTextView.setAlpha(1F);
                basicOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(thought);
                    }
                });
                basicOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(thought, dateAndTimeBasicOn);
                    }
                });
                break;
            case BASIC_OFF:
                BasicOffViewHolder basicOffViewHolder = (BasicOffViewHolder) holder;
                basicOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOffViewHolder.messageTextView.setText(thought.getText());
                basicOffViewHolder.timestampTextView.setText(
                        ApplicationHelper.getLocalDateAndTimeToDisplay(
                                mContext,
                                thought.getTimestamp()
                        )
                );
                basicOffViewHolder.timestampTextView.setAlpha(1F);
                break;
            case EXTRA_PHOTO_ON:
                final WithExtraPhotoOnViewHolder withExtraPhotoOnViewHolder
                        = (WithExtraPhotoOnViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        thought.getExtraPhotoUrl(),
                        withExtraPhotoOnViewHolder.imageView,
                        false
                );
                withExtraPhotoOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOnViewHolder.messageTextView.setText(thought.getText());
                final String dateAndTimePhotoOn = ApplicationHelper.getLocalDateAndTimeToDisplay(
                        mContext,
                        thought.getTimestamp()
                );
                withExtraPhotoOnViewHolder.switchView.setChecked(false);
                withExtraPhotoOnViewHolder.switchView.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked) {
                            sendThought(thought, withExtraPhotoOnViewHolder.switchView, dateAndTimePhotoOn);
                        }
                    }
                });
                withExtraPhotoOnViewHolder.timestampTextView.setText(dateAndTimePhotoOn);
                withExtraPhotoOnViewHolder.timestampTextView.setAlpha(1F);
                withExtraPhotoOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(thought, dateAndTimePhotoOn);
                    }
                });
                withExtraPhotoOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(thought);
                    }
                });
                break;
            case EXTRA_PHOTO_OFF:
                WithExtraPhotoOffViewHolder withExtraPhotoOffViewHolder
                        = (WithExtraPhotoOffViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        thought.getExtraPhotoUrl(),
                        withExtraPhotoOffViewHolder.imageView,
                        false
                );
                withExtraPhotoOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOffViewHolder.messageTextView.setText(thought.getText());
                withExtraPhotoOffViewHolder.timestampTextView.setText(
                        ApplicationHelper.getLocalDateAndTimeToDisplay(
                                mContext,
                                thought.getTimestamp()
                        )
                );
                withExtraPhotoOffViewHolder.timestampTextView.setAlpha(1F);
                break;
        }
    }

    private void sendThought(final Thought thought, Switch switchView, String dateAndTime) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(mContext)
                .inflate(R.layout.thought_info, null, false);
        TextView textViewOne = view.findViewById(R.id.tv_info_one);
        TextView textViewTwo = view.findViewById(R.id.tv_info_two);
        textViewOne.setText(thought.getText());
        textViewTwo.setText(dateAndTime);
        DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                updateDB(thought, null);
            }
        };
        DialogUtils.onDialogRequestForThoughtSending(
                mContext,
                mContext.getResources().getString(R.string.dialog_thought_send_title),
                view,
                dialogOnPositiveBtnClickListener,
                R.style.DialogUpDownTheme,
                switchView
        );
    }

    private void withdraw(final Thought thought, String dateAndTime) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(mContext)
                .inflate(R.layout.thought_info, null, false);
        TextView textViewOne = view.findViewById(R.id.tv_info_one);
        TextView textViewTwo = view.findViewById(R.id.tv_info_two);
        textViewOne.setText(thought.getText());
        textViewTwo.setText(dateAndTime);
        DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deleteFromDB(thought);
                deleteFromStorage(thought);
            }
        };
        DialogUtils.onDialogRequest(
                mContext,
                mContext.getResources().getString(R.string.dialog_thought_withdraw_title),
                view,
                dialogOnPositiveBtnClickListener,
                R.style.DialogUpDownTheme
        );
    }

    private void deleteFromStorage(Thought thought) {
        ApplicationHelper.deleteImageFromStorage(mContext, thought.getExtraPhotoUrl());
    }

    private void deleteFromDB(final Thought thought) {
        mConnectionsDatabaseRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(thought.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from thought delete from db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null thought delete from db");
                                continue;
                            }
                            if (toUidValue.equals(thought.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationHelper.THOUGHTS_NODE).child(thought.getKey());
                                databaseReference.setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void editMessage(final Thought thought) {
        final EditText input = new EditText(mContext);
        String inputText = thought.getText();
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
                    updateDB(thought, newValue);
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

    private void updateDB(final Thought thought, final String newValue) {
        mConnectionsDatabaseRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(thought.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from thought update db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null thought update db");
                                continue;
                            }
                            if (toUidValue.equals(thought.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationHelper.THOUGHTS_NODE).child(thought.getKey());
                                Map<String, Object> updateValueMap = new HashMap<>();
                                if (newValue != null) {
                                    updateValueMap.put(
                                            "/" + ApplicationHelper.OBJECT_TEXT_IDENTIFIER,
                                            newValue
                                    );
                                } else {
                                    updateValueMap.put(
                                            "/" + ApplicationHelper.OBJECT_HAS_ARRIVED_IDENTIFIER,
                                            true
                                    );
                                }
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
        return mThoughtList.size();
    }

    class WithExtraPhotoOnViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView messageTextView;
        TextView timestampTextView;
        Switch switchView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOnViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_thought_extra_photo);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            switchView = itemView.findViewById(R.id.switch_thought_status);
            customizeTextView = itemView.findViewById(R.id.tv_thought_customize);
            dismissTextView = itemView.findViewById(R.id.tv_thought_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_thought_header);
        }
    }

    class WithExtraPhotoOffViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView messageTextView;
        TextView timestampTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOffViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_thought_extra_photo);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            headerLinearLayout = itemView.findViewById(R.id.ll_thought_header);
        }
    }

    class BasicOnViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView timestampTextView;
        Switch switchView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public BasicOnViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            switchView = itemView.findViewById(R.id.switch_thought_status);
            customizeTextView = itemView.findViewById(R.id.tv_thought_customize);
            dismissTextView = itemView.findViewById(R.id.tv_thought_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_thought_header);
        }
    }

    class BasicOffViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView timestampTextView;
        LinearLayout headerLinearLayout;

        public BasicOffViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_thought_message);
            timestampTextView = itemView.findViewById(R.id.tv_thought_timestamp);
            headerLinearLayout = itemView.findViewById(R.id.ll_thought_header);
        }
    }
}
