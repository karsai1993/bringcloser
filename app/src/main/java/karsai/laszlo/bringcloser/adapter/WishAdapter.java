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
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Adapter to handle wish related information
 */
public class WishAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASIC_ON = 1;
    private static final int BASIC_OFF = 2;
    private static final int EXTRA_PHOTO_ON = 3;
    private static final int EXTRA_PHOTO_OFF = 4;

    private Context mContext;
    private List<Wish> mWishList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseRef;

    public WishAdapter(Context context, List<Wish> wishList) {
        this.mContext = context;
        this.mWishList = wishList;
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
                                .inflate(R.layout.list_item_wish_extra_photo_on, parent, false)
                );
            case EXTRA_PHOTO_OFF:
                return new WithExtraPhotoOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_wish_extra_photo_off, parent, false)
                );
            case BASIC_ON:
                return new BasicOnViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_wish_basic_on, parent, false)
                );
            default:
                return new BasicOffViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_wish_basic_off, parent, false)
                );
        }
    }

    @Override
    public int getItemViewType(int position) {
        Wish wish = mWishList.get(position);
        String photoUrl = wish.getExtraPhotoUrl();
        boolean isSent = ApplicationUtils.isSent(wish);
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
        final Wish wish = mWishList.get(position);
        switch (holder.getItemViewType()) {
            case BASIC_ON:
                BasicOnViewHolder basicOnViewHolder = (BasicOnViewHolder) holder;
                basicOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOnViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wish.getOccasion())
                );
                basicOnViewHolder.occasionTextView.setAlpha(1F);
                final String dateAndTimeBasicOn = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        wish.getWhenToArrive()
                );
                basicOnViewHolder.whenTextView.setText(dateAndTimeBasicOn);
                basicOnViewHolder.whenTextView.setAlpha(1F);
                basicOnViewHolder.contentTextView.setText(wish.getText());
                basicOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(wish, dateAndTimeBasicOn);
                    }
                });
                basicOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(wish);
                    }
                });
                break;
            case BASIC_OFF:
                BasicOffViewHolder basicOffViewHolder = (BasicOffViewHolder) holder;
                basicOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                basicOffViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wish.getOccasion())
                );
                basicOffViewHolder.occasionTextView.setAlpha(1F);
                basicOffViewHolder.whenTextView.setText(
                        ApplicationUtils.getLocalDateAndTimeToDisplay(
                                mContext,
                                wish.getWhenToArrive()
                        )
                );
                basicOffViewHolder.whenTextView.setAlpha(1F);
                basicOffViewHolder.contentTextView.setText(wish.getText());
                break;
            case EXTRA_PHOTO_ON:
                WithExtraPhotoOnViewHolder withExtraPhotoOnViewHolder
                        = (WithExtraPhotoOnViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        wish.getExtraPhotoUrl(),
                        withExtraPhotoOnViewHolder.imageView,
                        false
                );
                withExtraPhotoOnViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOnViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wish.getOccasion())
                );
                withExtraPhotoOnViewHolder.occasionTextView.setAlpha(1F);
                final String dateAndTimePhotoOn = ApplicationUtils.getLocalDateAndTimeToDisplay(
                        mContext,
                        wish.getWhenToArrive()
                );
                withExtraPhotoOnViewHolder.whenTextView.setText(dateAndTimePhotoOn);
                withExtraPhotoOnViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOnViewHolder.contentTextView.setText(wish.getText());
                withExtraPhotoOnViewHolder.dismissTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        withdraw(wish, dateAndTimePhotoOn);
                    }
                });
                withExtraPhotoOnViewHolder.customizeTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editMessage(wish);
                    }
                });
                break;
            case EXTRA_PHOTO_OFF:
                WithExtraPhotoOffViewHolder withExtraPhotoOffViewHolder
                        = (WithExtraPhotoOffViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        wish.getExtraPhotoUrl(),
                        withExtraPhotoOffViewHolder.imageView,
                        false
                );
                withExtraPhotoOffViewHolder.headerLinearLayout.setAlpha(0.75F);
                withExtraPhotoOffViewHolder.occasionTextView.setText(
                        ApplicationUtils.getTranslatedWishOccasion(mContext, wish.getOccasion())
                );
                withExtraPhotoOffViewHolder.occasionTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.whenTextView.setText(
                        ApplicationUtils.getLocalDateAndTimeToDisplay(
                                mContext,
                                wish.getWhenToArrive()
                        )
                );
                withExtraPhotoOffViewHolder.whenTextView.setAlpha(1F);
                withExtraPhotoOffViewHolder.contentTextView.setText(wish.getText());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mWishList.size();
    }

    class WithExtraPhotoOnViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView occasionTextView;
        TextView whenTextView;
        TextView contentTextView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOnViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_wish_extra_photo);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            contentTextView = itemView.findViewById(R.id.tv_wish_text);
            customizeTextView = itemView.findViewById(R.id.tv_wish_customize);
            dismissTextView = itemView.findViewById(R.id.tv_wish_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_wish_header);
        }
    }

    class WithExtraPhotoOffViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView occasionTextView;
        TextView whenTextView;
        TextView contentTextView;
        LinearLayout headerLinearLayout;

        public WithExtraPhotoOffViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_wish_extra_photo);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            contentTextView = itemView.findViewById(R.id.tv_wish_text);
            headerLinearLayout = itemView.findViewById(R.id.ll_wish_header);
        }
    }

    class BasicOnViewHolder extends RecyclerView.ViewHolder {

        TextView occasionTextView;
        TextView whenTextView;
        TextView contentTextView;
        TextView customizeTextView;
        TextView dismissTextView;
        LinearLayout headerLinearLayout;

        public BasicOnViewHolder(View itemView) {
            super(itemView);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            contentTextView = itemView.findViewById(R.id.tv_wish_text);
            customizeTextView = itemView.findViewById(R.id.tv_wish_customize);
            dismissTextView = itemView.findViewById(R.id.tv_wish_dismiss);
            headerLinearLayout = itemView.findViewById(R.id.ll_wish_header);
        }
    }

    class BasicOffViewHolder extends RecyclerView.ViewHolder {

        TextView occasionTextView;
        TextView whenTextView;
        TextView contentTextView;
        LinearLayout headerLinearLayout;

        public BasicOffViewHolder(View itemView) {
            super(itemView);
            occasionTextView = itemView.findViewById(R.id.tv_wish_occasion);
            whenTextView = itemView.findViewById(R.id.tv_wish_when);
            contentTextView = itemView.findViewById(R.id.tv_wish_text);
            headerLinearLayout = itemView.findViewById(R.id.ll_wish_header);
        }
    }

    private void editMessage(final Wish wish) {
        final EditText input = new EditText(mContext);
        String inputText = wish.getText();
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
                    updateDB(wish, newValue);
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

    private void withdraw(final Wish wish, String dateAndTime) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(mContext)
                .inflate(R.layout.withdraw_given_info, null, false);
        TextView textViewOne = view.findViewById(R.id.tv_info_one);
        TextView textViewTwo = view.findViewById(R.id.tv_info_two);
        TextView textViewThree = view.findViewById(R.id.tv_info_three);
        textViewOne.setText(
                ApplicationUtils.getTranslatedWishOccasion(mContext, wish.getOccasion())
        );
        textViewTwo.setText(dateAndTime);
        textViewThree.setText(wish.getText());
        DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deleteFromDB(wish);
                deleteFromStorage(wish);
                FirebaseJobDispatcher dispatcher
                        = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
                dispatcher.cancel(
                        ApplicationUtils.getServiceUniqueTag(
                                wish.getConnectionFromUid(),
                                wish.getConnectionToUid(),
                                wish.getKey()
                        )
                );
            }
        };
        DialogUtils.onDialogRequest(
                mContext,
                mContext.getResources().getString(R.string.dialog_wish_withdraw_title),
                view,
                dialogOnPositiveBtnClickListener,
                R.style.DialogUpDownTheme
        );
    }

    private void deleteFromDB(final Wish wish) {
        mConnectionsDatabaseRef.orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(wish.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from uid wish delete from db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null wish delete from db");
                                continue;
                            }
                            if (toUidValue.equals(wish.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationUtils.WISHES_NODE).child(wish.getKey());
                                databaseReference.setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteFromStorage(final Wish wish) {
        ApplicationUtils.deleteImageFromStorage(mContext, wish.getExtraPhotoUrl(), null, null);
    }

    private void updateDB(final Wish wish, final String newValue) {
        mConnectionsDatabaseRef.orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(wish.getConnectionFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null connection from wish update db");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null wish update db");
                                continue;
                            }
                            if (toUidValue.equals(wish.getConnectionToUid())) {
                                DatabaseReference databaseReference = mConnectionsDatabaseRef
                                        .child(key)
                                        .child(ApplicationUtils.WISHES_NODE).child(wish.getKey());
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
}
