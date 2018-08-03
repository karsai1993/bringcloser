package karsai.laszlo.bringcloser.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Created by Laci on 03/07/2018.
 * Adapter to handle message related information
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MESSAGE_OTHER_BASIC_VIEW_TYPE = 0;
    private static final int MESSAGE_OTHER_EXTRA_PHOTO_VIEW_TYPE = 1;
    private static final int MESSAGE_CURRENT_BASIC_VIEW_TYPE = 2;
    private static final int MESSAGE_CURRENT_EXTRA_PHOTO_VIEW_TYPE = 3;
    private static final int MESSAGE_DATE_VIEW_TYPE = 4;
    private static final int MESSAGE_OTHER_TYPING_TYPE = 5;

    private List<ChatDetail> mChatDetailList;
    private Context mContext;
    private String mCurrentUserUid;

    public MessageAdapter(Context context, List<ChatDetail> chatDetailList) {
        this.mContext = context;
        this.mChatDetailList = chatDetailList;
        this.mCurrentUserUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_OTHER_BASIC_VIEW_TYPE) {
            return new MessageOtherBasicViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_other_basic, parent, false)
            );
        } else if (viewType == MESSAGE_OTHER_EXTRA_PHOTO_VIEW_TYPE) {
            return new MessageOtherExtraPhotoViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_other_extra_photo, parent, false)
            );
        } else if (viewType == MESSAGE_CURRENT_BASIC_VIEW_TYPE) {
            return new MessageCurrentBasicViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_current_basic, parent, false)
            );
        } else if (viewType == MESSAGE_CURRENT_EXTRA_PHOTO_VIEW_TYPE) {
            return new MessageCurrentExtraPhotoViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_current_extra_photo, parent, false)
            );
        } else if (viewType == MESSAGE_DATE_VIEW_TYPE) {
            return new MessageDateViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_date, parent, false)
            );
        } else {
            return new MessageOtherTypingViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_typing, parent, false)
            );
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatDetail chatDetail = mChatDetailList.get(position);
        if (chatDetail.getMessageDetail() == null) {
            return MESSAGE_DATE_VIEW_TYPE;
        } else {
            MessageDetail messageDetail = chatDetail.getMessageDetail();
            if (messageDetail.getFrom() == null) {
                return MESSAGE_OTHER_TYPING_TYPE;
            } else if (mCurrentUserUid.equals(messageDetail.getFrom())) {
                if (messageDetail.getPhotoUrl() == null) {
                    return MESSAGE_CURRENT_BASIC_VIEW_TYPE;
                } else {
                    return MESSAGE_CURRENT_EXTRA_PHOTO_VIEW_TYPE;
                }
            } else {
                if (messageDetail.getPhotoUrl() == null) {
                    return MESSAGE_OTHER_BASIC_VIEW_TYPE;
                } else {
                    return MESSAGE_OTHER_EXTRA_PHOTO_VIEW_TYPE;
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatDetail chatDetail = mChatDetailList.get(position);
        MessageDetail messageDetail = chatDetail.getMessageDetail();
        String sentText = "";
        String sentPhotoUrl = "";
        String fromPhotoUrl = "";
        String date = "";
        String dateAndTime = "";
        String time = "";
        if (messageDetail != null) {
            sentText = messageDetail.getText();
            dateAndTime = chatDetail.getTime();
            String [] parts = dateAndTime.split(" ");
            try {
                time = parts[3];
            } catch (ArrayIndexOutOfBoundsException e) {
                Timber.wtf("array index out of bounds - time");
            }
            sentPhotoUrl = messageDetail.getPhotoUrl();
            fromPhotoUrl = messageDetail.getFromPhotoUrl();
        } else {
            date = chatDetail.getDate();
        }
        final String finalSentText = sentText;
        final String finalSentPhotoUrl = sentPhotoUrl;
        final String finalDateAndTime = dateAndTime;
        switch (holder.getItemViewType()) {
            case MESSAGE_OTHER_BASIC_VIEW_TYPE:
                MessageOtherBasicViewHolder messageOtherBasicViewHolder
                        = (MessageOtherBasicViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        fromPhotoUrl,
                        messageOtherBasicViewHolder.fromImageView,
                        true);
                messageOtherBasicViewHolder.messageTextView.setText(sentText);
                messageOtherBasicViewHolder.timeTextView.setText(time);
                messageOtherBasicViewHolder.textFieldLinearLayout.setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                saveToClipBoard(finalSentText, false);
                                return true;
                            }
                        });
                break;
            case MESSAGE_OTHER_EXTRA_PHOTO_VIEW_TYPE:
                MessageOtherExtraPhotoViewHolder messageOtherExtraPhotoViewHolder
                        = (MessageOtherExtraPhotoViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        fromPhotoUrl,
                        messageOtherExtraPhotoViewHolder.fromImageView,
                        true);
                ImageUtils.setPhoto(
                        mContext,
                        sentPhotoUrl,
                        messageOtherExtraPhotoViewHolder.imageView,
                        false);
                messageOtherExtraPhotoViewHolder.timeTextView.setText(time);
                messageOtherExtraPhotoViewHolder.timeTextView.setAlpha(0.75F);
                messageOtherExtraPhotoViewHolder.shareImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareImage(finalSentPhotoUrl);
                            }
                        }
                );
                messageOtherExtraPhotoViewHolder.imageView.setOnLongClickListener(
                        new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        saveToClipBoard(finalSentPhotoUrl, true);
                        return true;
                    }
                });
                messageOtherExtraPhotoViewHolder.imageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onImageClick(
                                        finalSentPhotoUrl,
                                        (ViewGroup)view.getParent(),
                                        finalDateAndTime
                                );
                            }
                        });
                break;
            case MESSAGE_CURRENT_BASIC_VIEW_TYPE:
                MessageCurrentBasicViewHolder messageCurrentBasicViewHolder
                        = (MessageCurrentBasicViewHolder) holder;
                messageCurrentBasicViewHolder.messageTextView.setText(sentText);
                messageCurrentBasicViewHolder.timeTextView.setText(time);
                messageCurrentBasicViewHolder.textFieldLinearLayout.setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                saveToClipBoard(finalSentText, false);
                                return true;
                            }
                        }
                );
                break;
            case MESSAGE_CURRENT_EXTRA_PHOTO_VIEW_TYPE:
                MessageCurrentExtraPhotoViewHolder messageCurrentExtraPhotoViewHolder
                        = (MessageCurrentExtraPhotoViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        sentPhotoUrl,
                        messageCurrentExtraPhotoViewHolder.imageView,
                        false);
                messageCurrentExtraPhotoViewHolder.timeTextView.setText(time);
                messageCurrentExtraPhotoViewHolder.timeTextView.setAlpha(0.75F);
                messageCurrentExtraPhotoViewHolder.shareImageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareImage(finalSentPhotoUrl);
                            }
                        }
                );
                messageCurrentExtraPhotoViewHolder.imageView.setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                saveToClipBoard(finalSentPhotoUrl, true);
                                return true;
                            }
                        });
                messageCurrentExtraPhotoViewHolder.imageView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onImageClick(
                                        finalSentPhotoUrl,
                                        (ViewGroup)view.getParent(),
                                        finalDateAndTime
                                );
                            }
                        });
                break;
            case MESSAGE_DATE_VIEW_TYPE:
                MessageDateViewHolder messageDateViewHolder = (MessageDateViewHolder) holder;
                messageDateViewHolder.mChatDateTextView.setText(
                        getDisplayDateWithRespectToCurrentDate(date)
                );
                break;
            case MESSAGE_OTHER_TYPING_TYPE:
                final MessageOtherTypingViewHolder messageOtherTypingViewHolder
                        = (MessageOtherTypingViewHolder) holder;
                ImageUtils.setPhoto(
                        mContext,
                        fromPhotoUrl,
                        messageOtherTypingViewHolder.fromImageView,
                        true);
                messageOtherTypingViewHolder.typingLayout.setTranslationX(-10f);
                messageOtherTypingViewHolder.typingLayout.setAlpha(0f);
                messageOtherTypingViewHolder.typingLayout.animate().translationX(0.0F)
                        .alpha(1f)
                        .setDuration(300)
                        .setStartDelay(50)
                        .start();
                Animation animationOne = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, -15f,
                        TranslateAnimation.ABSOLUTE, 0f);
                animationOne.setDuration(2500);
                animationOne.setStartOffset(200);
                animationOne.setRepeatCount(-1);
                animationOne.setRepeatMode(Animation.INFINITE);
                animationOne.setInterpolator(new CycleInterpolator(1f));
                messageOtherTypingViewHolder.textViewOne.setAnimation(animationOne);
                Animation animationTwo = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, -15f,
                        TranslateAnimation.ABSOLUTE, 0f);
                animationTwo.setDuration(2300);
                animationTwo.setStartOffset(400);
                animationTwo.setRepeatCount(-1);
                animationTwo.setRepeatMode(Animation.INFINITE);
                animationTwo.setInterpolator(new CycleInterpolator(1f));
                messageOtherTypingViewHolder.textViewTwo.setAnimation(animationTwo);
                Animation animationThree = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, 0f,
                        TranslateAnimation.ABSOLUTE, -15f,
                        TranslateAnimation.ABSOLUTE, 0f);
                animationThree.setDuration(2100);
                animationThree.setStartOffset(600);
                animationThree.setRepeatCount(-1);
                animationThree.setRepeatMode(Animation.INFINITE);
                animationThree.setInterpolator(new CycleInterpolator(1f));
                messageOtherTypingViewHolder.textViewThree.setAnimation(animationThree);
                break;
        }
    }

    private String getDisplayDateWithRespectToCurrentDate(String dateToCompare) {
        String currentLocalDateAndTime = ApplicationHelper.getLocalDateAndTimeToDisplay(
                mContext,
                ApplicationHelper.getCurrentUTCDateAndTime()
        );
        if (currentLocalDateAndTime.equals(mContext.getResources().getString(R.string.data_not_available))) {
            Timber.wtf( "currentLocalDateAndTime problem occurred");
            return "";
        }
        String currentLocalDateParts [] = currentLocalDateAndTime.split(" ");
        String currentLocalDate
                = currentLocalDateParts[0]
                + " " + currentLocalDateParts[1]
                + " " + currentLocalDateParts[2];
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.DATE, -1);
        Date yesterdayDateAndTime = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(
                ApplicationHelper.DISPLAY_DATE_PATTERN,
                Locale.getDefault()
        );
        sdf.setTimeZone(TimeZone.getDefault());
        String yesterdayDateAndTimeAsText = sdf.format(yesterdayDateAndTime);
        String yesterdayDateAndTimeParts [] = yesterdayDateAndTimeAsText.split(" ");
        String yesterdayLocalDate
                = yesterdayDateAndTimeParts[0]
                + " " + yesterdayDateAndTimeParts[1]
                + " " + yesterdayDateAndTimeParts[2];
        if (dateToCompare.equals(currentLocalDate)) {
            return mContext.getString(R.string.today);
        } else if (dateToCompare.equals(yesterdayLocalDate)) {
            return mContext.getString(R.string.yesterday);
        } else {
            return dateToCompare;
        }
    }

    @Override
    public int getItemCount() {
        if (mChatDetailList == null) return 0;
        return mChatDetailList.size();
    }

    private void onImageClick(final String photoUrl, ViewGroup root, final String dateAndTime) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.image, root, false);

        final ImageView imageView = view.findViewById(R.id.iv_chat_photo_big);
        ImageView shareImageView = view.findViewById(R.id.iv_chat_photo_share);
        TextView dateAndTimeTextView = view.findViewById(R.id.tv_chat_photo_date_and_time);

        ImageUtils.setPhoto(mContext, photoUrl, imageView, false);
        dateAndTimeTextView.setText(dateAndTime);
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage(photoUrl);
            }
        });

        DialogUtils.onDialogRequestForImage(mContext, view, R.style.DialogLeftRightTheme);
    }

    private void shareImage(String photoUrl) {
        String mimeType = "text/plain";
        String title = mContext.getResources().getString(R.string.share_title);
        Activity activity = (Activity) mContext;
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(activity)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(photoUrl)
                .createChooserIntent();
        if (shareIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(shareIntent);
        }
    }

    private void saveToClipBoard(String text, boolean isImage) {
        String messagePart = "";
        boolean isPart = false;
        if (!isImage) {
            if (text.length() > 5) {
                messagePart = text.substring(0, 5).trim();
                isPart = true;
            } else {
                messagePart = text.trim();
            }
        }
        ClipboardManager clipboard
                = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip
                = ClipData.newPlainText("Copied Text", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            String textToShow;
            if (isImage) {
                textToShow = mContext.getResources().getString(R.string.image_link_copied);
            } else if (isPart) {
                textToShow = new StringBuilder()
                        .append(mContext.getResources()
                                .getString(R.string.saved_to_clipboard_1))
                        .append(messagePart)
                        .append(mContext.getResources()
                                .getString(R.string.saved_to_clipboard_12))
                        .append(mContext.getResources()
                                .getString(R.string.saved_to_clipboard_2))
                        .toString();
            } else {
                textToShow = new StringBuilder()
                        .append(mContext.getResources()
                                .getString(R.string.saved_to_clipboard_1))
                        .append(messagePart)
                        .append(mContext.getResources()
                                .getString(R.string.saved_to_clipboard_2))
                        .toString();
            }
            Toast.makeText(
                    mContext,
                    textToShow,
                    Toast.LENGTH_LONG).show();
        }
    }

    class MessageCurrentBasicViewHolder extends RecyclerView.ViewHolder{

        TextView messageTextView;
        TextView timeTextView;
        LinearLayout textFieldLinearLayout;

        MessageCurrentBasicViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_chat_from_current_text);
            timeTextView = itemView.findViewById(R.id.tv_chat_from_current_time);
            textFieldLinearLayout = itemView.findViewById(R.id.ll_chat_text_field);
        }
    }

    class MessageCurrentExtraPhotoViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        ImageView shareImageView;
        TextView timeTextView;

        MessageCurrentExtraPhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_chat_from_current_sent_photo);
            shareImageView = itemView.findViewById(R.id.iv_chat_from_current_photo_share);
            timeTextView = itemView.findViewById(R.id.tv_chat_from_current_time);
        }
    }

    class MessageOtherBasicViewHolder extends RecyclerView.ViewHolder{

        ImageView fromImageView;
        TextView messageTextView;
        TextView timeTextView;
        LinearLayout textFieldLinearLayout;

        MessageOtherBasicViewHolder(View itemView) {
            super(itemView);
            fromImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            messageTextView = itemView.findViewById(R.id.tv_chat_from_other_text);
            timeTextView = itemView.findViewById(R.id.tv_chat_from_other_time);
            textFieldLinearLayout = itemView.findViewById(R.id.ll_chat_text_field);
        }
    }

    class MessageOtherExtraPhotoViewHolder extends RecyclerView.ViewHolder{

        ImageView fromImageView;
        ImageView imageView;
        ImageView shareImageView;
        TextView timeTextView;

        MessageOtherExtraPhotoViewHolder(View itemView) {
            super(itemView);
            fromImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            imageView = itemView.findViewById(R.id.iv_chat_from_other_sent_photo);
            shareImageView = itemView.findViewById(R.id.iv_chat_from_other_photo_share);
            timeTextView = itemView.findViewById(R.id.tv_chat_from_other_time);
        }
    }

    class MessageDateViewHolder extends RecyclerView.ViewHolder{

        TextView mChatDateTextView;

        MessageDateViewHolder(View itemView) {
            super(itemView);
            mChatDateTextView = itemView.findViewById(R.id.tv_chat_date);
        }
    }

    class MessageOtherTypingViewHolder extends RecyclerView.ViewHolder{

        ImageView fromImageView;
        TextView textViewOne;
        TextView textViewTwo;
        TextView textViewThree;
        LinearLayout typingLayout;

        MessageOtherTypingViewHolder(View itemView) {
            super(itemView);
            typingLayout = itemView.findViewById(R.id.ll_typing);
            fromImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            textViewOne = itemView.findViewById(R.id.tv_chat_from_other_typing_one);
            textViewTwo = itemView.findViewById(R.id.tv_chat_from_other_typing_two);
            textViewThree = itemView.findViewById(R.id.tv_chat_from_other_typing_three);
        }
    }
}
