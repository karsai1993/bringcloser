package karsai.laszlo.bringcloser.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.MessageAdapter;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionChatFragment extends Fragment {

    public ConnectionChatFragment() {}

    private RecyclerView mChatRecyclerView;
    private ProgressBar mProgressBar;
    /*private FloatingActionButton mSearchFab;
    private ImageView mCameraImageView;
    private ImageView mGalleryImageView;
    private ImageView mSendImageView;
    private EditText mMessageEditText;
    */private String mCurrentUserUid;
    private ConnectionDetail mConnectionDetail;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mMessagesDatabaseRef;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionMessagesValueEventListener;
    private List<MessageDetail> mMessageDetailList;
    private List<Message> mMessageList;
    private FloatingActionButton mActivitySearchFab;
    private MessageAdapter mMessageAdapter;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private TextView mChatNoDataTextView;
    private List<ChatDetail> mChatDetailList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("hopp_chat", "itt");
        View rootView
                = inflater.inflate(R.layout.fragment_connection_chat, container, false);
        mChatRecyclerView = rootView.findViewById(R.id.rv_chat);
        mProgressBar = rootView.findViewById(R.id.pb_chat);
        mChatNoDataTextView = rootView.findViewById(R.id.tv_chat_no_data);
        mChatRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false));
        mChatRecyclerView.setHasFixedSize(true);
        Activity activity = getActivity();
        if (activity != null) {
            mActivitySearchFab = activity.findViewById(R.id.fab_chat_search);
        }
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mActivitySearchFab == null) return;
                if (dy > 0 && mActivitySearchFab.getVisibility() == View.VISIBLE) {
                    mActivitySearchFab.setVisibility(View.INVISIBLE);
                } else if (dy < 0 && mActivitySearchFab.getVisibility() != View.VISIBLE) {
                    mActivitySearchFab.setVisibility(View.VISIBLE);
                }
            }
        };

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mConnectionDetail = bundle.getParcelable(ApplicationHelper.CONNECTION_DETAIL_KEY);
        }
        mChatDetailList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(
                getContext(),
                mChatDetailList
        );
        mChatRecyclerView.setAdapter(mMessageAdapter);

        mConnectionsQuery = mConnectionsDatabaseReference
                .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid());
        mConnectionMessagesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class)
                            .equals(mConnectionDetail.getToUid())) {
                        mMessagesDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.MESSAGES_NODE);
                        mMessagesDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mMessageList = new ArrayList<>();
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    Message message = messageSnapshot.getValue(Message.class);
                                    mMessageList.add(message);
                                }
                                mUsersDatabaseReference
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(
                                                    @NonNull DataSnapshot dataSnapshot) {
                                                int prevCount = mChatDetailList.size();
                                                mChatDetailList.clear();
                                                mMessageDetailList = new ArrayList<>();
                                                for (Message message : mMessageList) {
                                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                        String uid = userSnapshot.getKey();
                                                        User user = userSnapshot.getValue(User.class);
                                                        if (message.getFrom().equals(uid)) {
                                                            String fromPhotoUrl
                                                                    = user.getPhotoUrl();
                                                            mMessageDetailList.add(
                                                                    new MessageDetail(
                                                                            message.getFrom(),
                                                                            fromPhotoUrl,
                                                                            message.getText(),
                                                                            message.getPhotoUrl(),
                                                                            message.getTimestamp()
                                                                    )
                                                            );
                                                            break;
                                                        }
                                                    }
                                                }
                                                mChatDetailList.addAll(
                                                        getDateInfoNextToMessageData(
                                                                mMessageDetailList
                                                        )
                                                );
                                                mProgressBar.setVisibility(View.GONE);
                                                if (mChatDetailList.size() > 0) {
                                                    mChatNoDataTextView.setVisibility(View.GONE);
                                                    mChatRecyclerView.setVisibility(View.VISIBLE);
                                                    mMessageAdapter.notifyDataSetChanged();
                                                    //mMessageAdapter.swap(mMessageDetailList);
                                                    /*mMessageAdapter = new MessageAdapter(
                                                            getContext(),
                                                            mMessageDetailList
                                                    );
                                                    mChatRecyclerView.setAdapter(mMessageAdapter);
                                                    */int currCount = mChatDetailList.size();
                                                    if (prevCount > 0 && currCount > prevCount) {
                                                        mChatRecyclerView
                                                                .smoothScrollToPosition(currCount);
                                                    }
                                                } else {
                                                    mChatRecyclerView.setVisibility(View.GONE);
                                                    mChatNoDataTextView.setVisibility(View.VISIBLE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(
                                                    @NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        if (mActivitySearchFab != null) {
            mActivitySearchFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   int count =  mMessageAdapter.getItemCount();
                   if (count == 0) {
                       Toast.makeText(
                               getContext(),
                               getResources().getString(R.string.chat_no_data_message),
                               Toast.LENGTH_LONG
                       ).show();
                   } else {

                   }
                }
            });
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mChatRecyclerView.setVisibility(View.GONE);
        return rootView;
    }

    private List<ChatDetail> getDateInfoNextToMessageData(List<MessageDetail> messageDetailList) {
        List<ChatDetail> chatDetailList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        for (MessageDetail messageDetail : messageDetailList) {
            String currDateAndTime = messageDetail.getTimestamp();
            String currDateAndTimeLocale = ApplicationHelper.getLocalDateAndTime(
                    getContext(),
                    currDateAndTime
            );
            String [] parts = currDateAndTimeLocale.split(ApplicationHelper.DATE_SPLITTER);
            String currDateAsText = parts[0];
            String currTimeAsText = parts[1];
            if (!dateList.contains(currDateAsText)) {
                dateList.add(currDateAsText);
                ChatDetail currDate = new ChatDetail(currDateAsText);
                chatDetailList.add(currDate);
            }
            ChatDetail currMessage = new ChatDetail(messageDetail, currTimeAsText);
            chatDetailList.add(currMessage);
        }
        return chatDetailList;
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatRecyclerView.addOnScrollListener(mOnScrollListener);
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionMessagesValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOnScrollListener != null) {
            mChatRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
        if (mConnectionMessagesValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionMessagesValueEventListener);
        }
    }
}
