package karsai.laszlo.bringcloser.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("hopp_chat", "itt");
        View rootView
                = inflater.inflate(R.layout.fragment_connection_chat, container, false);
        mChatRecyclerView = rootView.findViewById(R.id.rv_chat);
        mProgressBar = rootView.findViewById(R.id.pb_chat);
        /*mSearchFab = rootView.findViewById(R.id.fab_chat_search);
        mCameraImageView = rootView.findViewById(R.id.iv_chat_add_photo_from_camera);
        mGalleryImageView = rootView.findViewById(R.id.iv_chat_add_photo_from_gallery);
        mSendImageView = rootView.findViewById(R.id.iv_chat_send);
        mMessageEditText = rootView.findViewById(R.id.et_chat);*/
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mChatRecyclerView.setLayoutManager(layoutManager);
        mChatRecyclerView.setHasFixedSize(true);
        mActivitySearchFab = getActivity().findViewById(R.id.fab_chat_search);
        if (mActivitySearchFab != null) {
            mChatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && mActivitySearchFab.getVisibility() == View.VISIBLE) {
                        mActivitySearchFab.setVisibility(View.INVISIBLE);
                    } else if (dy < 0 && mActivitySearchFab.getVisibility() != View.VISIBLE) {
                        mActivitySearchFab.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

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
        mMessageDetailList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(
                getContext(),
                mMessageDetailList
        );
        mChatRecyclerView.setAdapter(mMessageAdapter);

        /*int itemCount = messageAdapter.getItemCount();
        mChatRecyclerView
                .smoothScrollToPosition(
                        itemCount > 0 ? itemCount - 1 : 0
                );*/

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
                                                mMessageDetailList.clear();
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
                                                mProgressBar.setVisibility(View.GONE);
                                                mChatRecyclerView.setVisibility(View.VISIBLE);
                                                mMessageAdapter.notifyDataSetChanged();
                                                int itemCount = mMessageAdapter.getItemCount();
                                                mChatRecyclerView
                                                        .smoothScrollToPosition(
                                                                itemCount > 0 ? itemCount - 1 : 0
                                                        );
                                                /*MessageAdapter messageAdapter
                                                        = new MessageAdapter(
                                                                getContext(),
                                                        mMessageDetailList
                                                );
                                                mProgressBar.setVisibility(View.GONE);
                                                mChatRecyclerView.setVisibility(View.VISIBLE);
                                                mChatRecyclerView.setAdapter(messageAdapter);
                                                int itemCount = messageAdapter.getItemCount();
                                                mChatRecyclerView
                                                        .smoothScrollToPosition(
                                                                itemCount > 0 ? itemCount - 1 : 0
                                                        );*/
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
        mProgressBar.setVisibility(View.VISIBLE);
        mChatRecyclerView.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionMessagesValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mConnectionMessagesValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionMessagesValueEventListener);
        }
    }
}
