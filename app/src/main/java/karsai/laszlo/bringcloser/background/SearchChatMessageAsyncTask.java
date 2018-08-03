package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.adapter.MessageAdapter;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.MessageDetail;

/**
 * Background task to handle filtering in chat messages
 */
public class SearchChatMessageAsyncTask extends AsyncTask<String, Void, List<ChatDetail>> {

    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;
    private List<ChatDetail> mChatDetailList;
    private ProgressBar mProgressBar;
    private Context mContext;
    private List<MessageDetail> mMessageDetailList;

    public SearchChatMessageAsyncTask(
            RecyclerView mRecyclerView,
            MessageAdapter mMessageAdapter,
            List<ChatDetail> mChatDetailList,
            ProgressBar mProgressBar,
            Context mContext,
            List<MessageDetail> mMessageDetailList) {
        this.mRecyclerView = mRecyclerView;
        this.mMessageAdapter = mMessageAdapter;
        this.mChatDetailList = mChatDetailList;
        this.mProgressBar = mProgressBar;
        this.mContext = mContext;
        this.mMessageDetailList = mMessageDetailList;
    }

    @Override
    protected void onPreExecute() {
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<ChatDetail> doInBackground(String... inputs) {
        String filter = inputs[0];
        List<MessageDetail> messageDetailList = new ArrayList<>();
        for (MessageDetail messageDetail : mMessageDetailList) {
            String text = messageDetail.getText();
            if (text != null
                    && text.toLowerCase(Locale.getDefault())
                    .contains(filter.toLowerCase(Locale.getDefault()))) {
                messageDetailList.add(messageDetail);
            }
        }
        return ApplicationHelper.getDateInfoNextToMessageData(mContext, messageDetailList);
    }

    @Override
    protected void onPostExecute(List<ChatDetail> chatDetails) {
        mChatDetailList.clear();
        mChatDetailList.addAll(chatDetails);
        mMessageAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}
