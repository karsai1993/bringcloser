package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.utils.ImageUtils;

/**
 * Created by Laci on 03/07/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageDetail> mMessageDetailList;
    private Context mContext;
    private String mCurrentUserUid;

    public MessageAdapter(Context context, List<MessageDetail> messageDetailList) {
        this.mContext = context;
        this.mMessageDetailList = messageDetailList;
        this.mCurrentUserUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(
                LayoutInflater.from(mContext)
                        .inflate(R.layout.list_item_message, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageDetail messageDetail = mMessageDetailList.get(position);
        if (mCurrentUserUid.equals(messageDetail.getFrom())) {
            holder.mChatFromOtherLinearLayout.setVisibility(View.GONE);
            holder.mChatFromCurrentLinearLayout.setVisibility(View.VISIBLE);
            holder.mChatFromCurrentTextView.setText(messageDetail.getText());
        } else {
            holder.mChatFromOtherLinearLayout.setVisibility(View.VISIBLE);
            holder.mChatFromCurrentLinearLayout.setVisibility(View.GONE);
            holder.mChatFromOtherTextView.setText(messageDetail.getText());
            ImageUtils.setUserPhoto(
                    mContext, messageDetail.getFromPhotoUrl(), holder.mChatFromOtherImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (mMessageDetailList == null) return 0;
        return mMessageDetailList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mChatFromOtherLinearLayout;
        ImageView mChatFromOtherImageView;
        TextView mChatFromOtherTextView;
        LinearLayout mChatFromCurrentLinearLayout;
        TextView mChatFromCurrentTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mChatFromCurrentLinearLayout = itemView.findViewById(R.id.ll_chat_from_current);
            mChatFromCurrentTextView = itemView.findViewById(R.id.tv_chat_from_current_text);
            mChatFromOtherLinearLayout = itemView.findViewById(R.id.ll_chat_from_other);
            mChatFromOtherImageView = itemView.findViewById(R.id.iv_chat_from_other_photo);
            mChatFromOtherTextView = itemView.findViewById(R.id.tv_chat_from_other_text);
        }
    }
}
