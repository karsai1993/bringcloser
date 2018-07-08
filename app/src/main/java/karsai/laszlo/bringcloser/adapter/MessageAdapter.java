package karsai.laszlo.bringcloser.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import karsai.laszlo.bringcloser.utils.NotificationUtils;

/**
 * Created by Laci on 03/07/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MESSAGE_OTHER_VIEW_TYPE = 0;
    private static final int MESSAGE_CURRENT_VIEW_TYPE = 1;
    private static final int MESSAGE_DATE_VIEW_TYPE = 2;

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
        if (viewType == MESSAGE_OTHER_VIEW_TYPE) {
            return new MessageOtherViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_other, parent, false)
            );
        } else if (viewType == MESSAGE_CURRENT_VIEW_TYPE) {
            return new MessageCurrentViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_current, parent, false)
            );
        } else {
            return new MessageDateViewHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_message_date, parent, false)
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
            if (mCurrentUserUid.equals(messageDetail.getFrom())) {
                return MESSAGE_CURRENT_VIEW_TYPE;
            } else {
                return MESSAGE_OTHER_VIEW_TYPE;
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
            String [] parts = dateAndTime.split(ApplicationHelper.DATE_SPLITTER);
            time = parts[1];
            dateAndTime = dateAndTime.replace(ApplicationHelper.DATE_SPLITTER, " ");
            sentPhotoUrl = messageDetail.getPhotoUrl();
            fromPhotoUrl = messageDetail.getFromPhotoUrl();
        } else {
            date = chatDetail.getDate();
        }
        switch (holder.getItemViewType()) {
            case MESSAGE_OTHER_VIEW_TYPE:
                MessageOtherViewHolder messageOtherViewHolder = (MessageOtherViewHolder) holder;
                ImageUtils.setUserPhoto(
                        mContext,
                        fromPhotoUrl,
                        messageOtherViewHolder.mChatFromOtherImageView);
                if (sentText != null && !sentText.isEmpty()) {
                    messageOtherViewHolder.mChatFromOtherTextLinearLayout.setVisibility(View.VISIBLE);
                    messageOtherViewHolder.mChatFromOtherTextView.setText(sentText);
                    messageOtherViewHolder.mChatFromOtherTimeTextView.setText(time);
                    messageOtherViewHolder.mChatFromOtherSentImageView.setVisibility(View.GONE);
                    messageOtherViewHolder.mView.setVisibility(View.GONE);
                    final String finalSentText = sentText;
                    messageOtherViewHolder.mChatFromOtherTextLinearLayout.setOnLongClickListener(
                            new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            saveToClipBoard(finalSentText);
                            return true;
                        }
                    });
                } else {
                    messageOtherViewHolder.mChatFromOtherTextLinearLayout.setVisibility(View.GONE);
                    messageOtherViewHolder.mView.setVisibility(View.VISIBLE);
                    messageOtherViewHolder.mChatFromOtherSentImageView.setVisibility(View.VISIBLE);
                    ImageUtils.displayMessagePhoto(
                            mContext,
                            sentPhotoUrl,
                            messageOtherViewHolder.mChatFromOtherSentImageView);
                    final String finalSentPhotoUrlOther = sentPhotoUrl;
                    final String finalDateAndTimeOther = dateAndTime;
                    messageOtherViewHolder.mChatFromOtherSentImageView.setOnClickListener(
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onImageClick(finalSentPhotoUrlOther, finalDateAndTimeOther);
                        }
                    });
                }
                break;
            case MESSAGE_CURRENT_VIEW_TYPE:
                MessageCurrentViewHolder messageCurrentViewHolder = (MessageCurrentViewHolder) holder;
                if (sentText != null && !sentText.isEmpty()) {
                    messageCurrentViewHolder.mChatFromCurrentTextLinearLayout.setVisibility(View.VISIBLE);
                    messageCurrentViewHolder.mChatFromCurrentTextView.setText(sentText);
                    messageCurrentViewHolder.mChatFromCurrentTimeTextView.setText(time);
                    messageCurrentViewHolder.mChatFromCurrentSentImageView.setVisibility(View.GONE);
                    messageCurrentViewHolder.mView.setVisibility(View.GONE);
                    final String finalSentText = sentText;
                    messageCurrentViewHolder.mChatFromCurrentTextLinearLayout.setOnLongClickListener(
                            new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            saveToClipBoard(finalSentText);
                            return true;
                        }
                    });
                } else {
                    messageCurrentViewHolder.mChatFromCurrentTextLinearLayout.setVisibility(View.GONE);
                    messageCurrentViewHolder.mView.setVisibility(View.VISIBLE);
                    messageCurrentViewHolder.mChatFromCurrentSentImageView.setVisibility(View.VISIBLE);
                    ImageUtils.displayMessagePhoto(
                            mContext,
                            sentPhotoUrl,
                            messageCurrentViewHolder.mChatFromCurrentSentImageView);
                    final String finalDateAndTimeCurrent = dateAndTime;
                    final String finalSentPhotoUrlCurrent = sentPhotoUrl;
                    messageCurrentViewHolder.mChatFromCurrentSentImageView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onImageClick(finalSentPhotoUrlCurrent, finalDateAndTimeCurrent);
                                }
                            }
                    );
                }
                break;
            case MESSAGE_DATE_VIEW_TYPE:
                MessageDateViewHolder messageDateViewHolder = (MessageDateViewHolder) holder;
                messageDateViewHolder.mChatDateTextView.setText(date);
        }
    }

    @Override
    public int getItemCount() {
        if (mChatDetailList == null) return 0;
        return mChatDetailList.size();
    }

    private void onImageClick(final String photoUrl, final String dateAndTime) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.image, null, false);

        final ImageView imageView = view.findViewById(R.id.iv_chat_photo_big);
        ImageView shareImageView = view.findViewById(R.id.iv_chat_photo_share);
        ImageView downloadImageView = view.findViewById(R.id.iv_chat_photo_download);
        TextView dateAndTimeTextView = view.findViewById(R.id.tv_chat_photo_date_and_time);

        ImageUtils.displayMessagePhoto(mContext, photoUrl, imageView);
        dateAndTimeTextView.setText(dateAndTime);
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage(photoUrl);
            }
        });
        downloadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage(photoUrl, dateAndTime);
            }
        });

        DialogUtils.onDialogRequestForImage(mContext, view, R.style.DialogLeftRightTheme);
    }

    private void downloadImage(String photoUrl, String dateAndTime) {

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

    private void saveToClipBoard(String message) {
        String messagePart;
        boolean isPart = false;
        if (message.length() > 5) {
            messagePart = message.substring(0, 5).trim();
            isPart = true;
        } else {
            messagePart = message.trim();
        }
        ClipboardManager clipboard
                = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip
                = ClipData.newPlainText("Copied Text", message);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            if (isPart) {
                Toast.makeText(
                        mContext,
                        new StringBuilder()
                                .append(mContext.getResources()
                                        .getString(R.string.saved_to_clipboard_1))
                                .append(messagePart)
                                .append(mContext.getResources()
                                        .getString(R.string.saved_to_clipboard_12))
                                .append(mContext.getResources()
                                        .getString(R.string.saved_to_clipboard_2))
                                .toString(),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(
                        mContext,
                        new StringBuilder()
                                .append(mContext.getResources()
                                        .getString(R.string.saved_to_clipboard_1))
                                .append(messagePart)
                                .append(mContext.getResources()
                                        .getString(R.string.saved_to_clipboard_2))
                                .toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    class MessageCurrentViewHolder extends RecyclerView.ViewHolder{

        TextView mChatFromCurrentTextView;
        TextView mChatFromCurrentTimeTextView;
        ImageView mChatFromCurrentSentImageView;
        View mView;
        LinearLayout mChatFromCurrentTextLinearLayout;

        public MessageCurrentViewHolder(View itemView) {
            super(itemView);
            mChatFromCurrentTextView = itemView.findViewById(R.id.tv_chat_from_current_text);
            mChatFromCurrentSentImageView
                    = itemView.findViewById(R.id.iv_chat_from_current_sent_photo);
            mView = itemView.findViewById(R.id.v_chat_from_current_placeholder);
            mChatFromCurrentTextLinearLayout = itemView.findViewById(R.id.ll_chat_from_current_text);
            mChatFromCurrentTimeTextView = itemView.findViewById(R.id.tv_chat_from_current__time);
        }
    }

    class MessageOtherViewHolder extends RecyclerView.ViewHolder{

        ImageView mChatFromOtherImageView;
        TextView mChatFromOtherTextView;
        TextView mChatFromOtherTimeTextView;
        ImageView mChatFromOtherSentImageView;
        View mView;
        LinearLayout mChatFromOtherTextLinearLayout;

        public MessageOtherViewHolder(View itemView) {
            super(itemView);
            mChatFromOtherImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            mChatFromOtherTextView = itemView.findViewById(R.id.tv_chat_from_other_text);
            mChatFromOtherSentImageView = itemView.findViewById(R.id.iv_chat_from_other_sent_photo);
            mView = itemView.findViewById(R.id.v_chat_from_other_placeholder);
            mChatFromOtherTimeTextView = itemView.findViewById(R.id.tv_chat_from_other_time);
            mChatFromOtherTextLinearLayout = itemView.findViewById(R.id.ll_chat_from_other_text);
        }
    }

    class MessageDateViewHolder extends RecyclerView.ViewHolder{

        TextView mChatDateTextView;

        public MessageDateViewHolder(View itemView) {
            super(itemView);
            mChatDateTextView = itemView.findViewById(R.id.tv_chat_date);
        }
    }
}
