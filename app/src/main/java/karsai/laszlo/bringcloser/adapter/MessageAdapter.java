package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.utils.ImageUtils;

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
        if (messageDetail != null) {
            sentText = messageDetail.getText();
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
                    messageOtherViewHolder.mChatFromOtherTextView.setVisibility(View.VISIBLE);
                    messageOtherViewHolder.mChatFromOtherTextView.setText(sentText);
                    messageOtherViewHolder.mChatFromOtherSentImageView.setVisibility(View.GONE);
                } else {
                    messageOtherViewHolder.mChatFromOtherTextView.setVisibility(View.GONE);
                    messageOtherViewHolder.mChatFromOtherSentImageView.setVisibility(View.VISIBLE);
                    ImageUtils.displayMessagePhoto(
                            mContext,
                            sentPhotoUrl,
                            messageOtherViewHolder.mChatFromOtherSentImageView);
                }
                break;
            case MESSAGE_CURRENT_VIEW_TYPE:
                MessageCurrentViewHolder messageCurrentViewHolder = (MessageCurrentViewHolder) holder;
                if (sentText != null && !sentText.isEmpty()) {
                    messageCurrentViewHolder.mChatFromCurrentTextView.setVisibility(View.VISIBLE);
                    messageCurrentViewHolder.mChatFromCurrentTextView.setText(sentText);
                    messageCurrentViewHolder.mChatFromCurrentSentImageView.setVisibility(View.GONE);
                } else {
                    messageCurrentViewHolder.mChatFromCurrentTextView.setVisibility(View.GONE);
                    messageCurrentViewHolder.mChatFromCurrentSentImageView.setVisibility(View.VISIBLE);
                    ImageUtils.displayMessagePhoto(
                            mContext,
                            sentPhotoUrl,
                            messageCurrentViewHolder.mChatFromCurrentSentImageView);
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

    class MessageCurrentViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mChatFromCurrentLinearLayout;
        TextView mChatFromCurrentTextView;
        ImageView mChatFromCurrentSentImageView;

        public MessageCurrentViewHolder(View itemView) {
            super(itemView);
            mChatFromCurrentLinearLayout = itemView.findViewById(R.id.ll_chat_from_current);
            mChatFromCurrentTextView = itemView.findViewById(R.id.tv_chat_from_current_text);
            mChatFromCurrentSentImageView
                    = itemView.findViewById(R.id.iv_chat_from_current_sent_photo);
        }
    }

    class MessageOtherViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mChatFromOtherLinearLayout;
        ImageView mChatFromOtherImageView;
        TextView mChatFromOtherTextView;
        ImageView mChatFromOtherSentImageView;

        public MessageOtherViewHolder(View itemView) {
            super(itemView);
            mChatFromOtherLinearLayout = itemView.findViewById(R.id.ll_chat_from_other);
            mChatFromOtherImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            mChatFromOtherTextView = itemView.findViewById(R.id.tv_chat_from_other_text);
            mChatFromOtherSentImageView = itemView.findViewById(R.id.iv_chat_from_other_sent_photo);
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
