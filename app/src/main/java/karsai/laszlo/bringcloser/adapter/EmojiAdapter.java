package karsai.laszlo.bringcloser.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import karsai.laszlo.bringcloser.R;

public class EmojiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int EMOJI_HEADER_VIEW_TYPE = 1;
    private static final int EMOJI_ITEM_VIEW_TYPE = 2;

    private Context mContext;
    private List<String> mEmojiIconList;
    private List<String> mEmojiKeyList;
    private String[] mEmojiDescriptionArray;

    public EmojiAdapter(
            Context context,
            List<String> emojiIconList,
            List<String> emojiKeyList,
            String[] emojiDescriptionArray) {
        this.mContext = context;
        this.mEmojiIconList = emojiIconList;
        this.mEmojiKeyList = emojiKeyList;
        this.mEmojiDescriptionArray = emojiDescriptionArray;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case EMOJI_HEADER_VIEW_TYPE:
                return new EmojiHeaderViewHolder(
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_emoji_header, null)
                );
                default:
                    return new EmojiItemViewHolder(
                            LayoutInflater.from(mContext)
                                    .inflate(R.layout.list_item_emoji_item, null)
                    );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case EMOJI_ITEM_VIEW_TYPE:
                EmojiItemViewHolder emojiItemViewHolder = (EmojiItemViewHolder) holder;
                emojiItemViewHolder.icon.setText(mEmojiIconList.get(position - 1));
                emojiItemViewHolder.key.setText(
                        mEmojiKeyList.get(position - 1).replaceAll("\\\\", "")
                );
                emojiItemViewHolder.description.setText(mEmojiDescriptionArray[position - 1]);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return EMOJI_HEADER_VIEW_TYPE;
        } else {
            return EMOJI_ITEM_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return mEmojiIconList.size() + 1;
    }

    class EmojiHeaderViewHolder extends RecyclerView.ViewHolder {

        EmojiHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    class EmojiItemViewHolder extends RecyclerView.ViewHolder {

        TextView icon;
        TextView key;
        TextView description;

        EmojiItemViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tv_emoji_icon);
            key = itemView.findViewById(R.id.tv_emoji_key);
            description = itemView.findViewById(R.id.tv_emoji_description);
        }
    }
}
