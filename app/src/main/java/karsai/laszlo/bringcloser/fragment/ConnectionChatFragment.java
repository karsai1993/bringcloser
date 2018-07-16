package karsai.laszlo.bringcloser.fragment;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import karsai.laszlo.bringcloser.background.SearchChatMessageAsyncTask;
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
    private MessageAdapter mMessageAdapter;
    private TextView mChatNoDataTextView;
    private List<ChatDetail> mChatDetailList;
    private FloatingActionButton mScrollUpFab;
    private FloatingActionButton mScrollDownFab;
    private ImageView mSearchImageView;
    private ImageView mCameraImageView;
    private ImageView mGalleryImageView;
    private ImageView mSendImageView;
    private EditText mMessageEditText;
    private EditText mMessageFilterEditText;
    private LinearLayout mChatControlLayout;
    private LinearLayout mChatInnerControlLayout;
    private TextWatcher mTextWatcher;
    private Activity mActivity;
    private Bundle mSavedInstanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        View rootView
                = inflater.inflate(R.layout.fragment_connection_chat, container, false);
        mChatRecyclerView = rootView.findViewById(R.id.rv_chat);
        mProgressBar = rootView.findViewById(R.id.pb_chat);
        mChatNoDataTextView = rootView.findViewById(R.id.tv_chat_no_data);
        mScrollUpFab = rootView.findViewById(R.id.fab_chat_scroll_up);
        mScrollDownFab = rootView.findViewById(R.id.fab_chat_scroll_down);
        mChatRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), OrientationHelper.VERTICAL, false));
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstPos = ((LinearLayoutManager)recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                int lastPos = ((LinearLayoutManager)recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition();
                if (firstPos > 1) {
                    mScrollUpFab.setVisibility(View.VISIBLE);
                } else {
                    mScrollUpFab.setVisibility(View.GONE);
                }
                if (lastPos < mMessageAdapter.getItemCount() - 2) {
                    mScrollDownFab.setVisibility(View.VISIBLE);
                } else {
                    mScrollDownFab.setVisibility(View.GONE);
                }
            }
        });
        mScrollDownFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
            }
        });
        mScrollUpFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatRecyclerView.smoothScrollToPosition(0);
            }
        });

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
        mMessageList = new ArrayList<>();
        mMessageDetailList = new ArrayList<>();
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
                                int prevMessageNum = mMessageList.size();
                                if (prevMessageNum > 0 && !dataSnapshot.exists()) {
                                    String otherName;
                                    String otherGender;
                                    if (mConnectionDetail.getFromUid().equals(mCurrentUserUid)) {
                                        otherName = mConnectionDetail.getToName();
                                        otherGender = mConnectionDetail.getToGender();
                                    } else {
                                        otherGender = mConnectionDetail.getFromGender();
                                        otherName = mConnectionDetail.getFromName();
                                    }
                                    String genderRepresentative;
                                    if (otherGender.equals(getContext().getString(R.string.gender_female))) {
                                        genderRepresentative
                                                = getContext().getString(R.string.gender_representative_female);
                                    } else if (otherGender.equals(getContext().getString(R.string.gender_male))) {
                                        genderRepresentative
                                                = getContext().getString(R.string.gender_representative_male);
                                    } else {
                                        genderRepresentative
                                                = getContext().getString(R.string.gender_representative_none);
                                    }
                                    Toast.makeText(
                                            getContext(),
                                            new StringBuilder()
                                            .append(getContext().getString(R.string.other_people_delete_common_1))
                                            .append(otherName)
                                            .append(getContext().getString(R.string.other_people_delete_common_2))
                                            .append(genderRepresentative)
                                            .append(getContext().getString(R.string.other_people_delete_common_3))
                                            .toString(),
                                            Toast.LENGTH_LONG).show();
                                    if (mActivity != null) {
                                        mActivity.finish();
                                    }
                                }
                                mMessageList.clear();
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
                                                mMessageDetailList.clear();
                                                mMessageFilterEditText
                                                        .removeTextChangedListener(mTextWatcher);
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
                                                        ApplicationHelper.getDateInfoNextToMessageData(
                                                                getContext(),
                                                                mMessageDetailList
                                                        )
                                                );
                                                mProgressBar.setVisibility(View.GONE);
                                                int messageNum = mChatDetailList.size();
                                                if (messageNum > 0) {
                                                    mMessageAdapter.notifyDataSetChanged();
                                                    mChatNoDataTextView.setVisibility(View.GONE);
                                                    mChatRecyclerView.setVisibility(View.VISIBLE);
                                                    /*if (mSavedInstanceState == null
                                                            || (prevCount > 0
                                                            && messageNum > prevCount)) {
                                                        mChatRecyclerView
                                                                .smoothScrollToPosition(
                                                                        messageNum - 1);
                                                        mScrollDownFab.setVisibility(View.VISIBLE);
                                                    } else {
                                                        mSavedInstanceState = null;
                                                    }*/
                                                } else {
                                                    mChatRecyclerView.setVisibility(View.GONE);
                                                    mChatNoDataTextView.setVisibility(View.VISIBLE);
                                                }
                                                mMessageFilterEditText
                                                        .addTextChangedListener(mTextWatcher);
                                                String filter = mMessageFilterEditText.getText().toString();
                                                if (!filter.isEmpty()) {
                                                    mSearchImageView.setImageDrawable(
                                                            getContext().getResources()
                                                                    .getDrawable(R.drawable.baseline_clear_black_48)
                                                    );
                                                    mChatInnerControlLayout.setVisibility(View.GONE);
                                                    mMessageFilterEditText.setVisibility(View.VISIBLE);
                                                    new SearchChatMessageAsyncTask(
                                                            mChatRecyclerView,
                                                            mMessageAdapter,
                                                            mChatDetailList,
                                                            mProgressBar,
                                                            getContext(),
                                                            mMessageDetailList
                                                    ).execute(filter);
                                                }
                                                /*int pos = ((LinearLayoutManager)
                                                        mChatRecyclerView.getLayoutManager())
                                                        .findLastCompletelyVisibleItemPosition();
                                                while (pos != mMessageAdapter.getItemCount() - 1) {
                                                    mChatRecyclerView.scrollToPosition(++pos);
                                                }*/
                                                if (mSavedInstanceState == null
                                                        || (prevCount > 0
                                                        && messageNum > prevCount)) {
                                                    /*mChatRecyclerView
                                                            .scrollToPosition(
                                                                    messageNum - 1);
                                                    mScrollDownFab.setVisibility(View.VISIBLE);*/
                                                    int pos = ((LinearLayoutManager)
                                                            mChatRecyclerView.getLayoutManager())
                                                            .findLastCompletelyVisibleItemPosition();
                                                    while (pos != mMessageAdapter.getItemCount() - 1) {
                                                        mChatRecyclerView.scrollToPosition(++pos);
                                                    }
                                                } else {
                                                    mSavedInstanceState = null;
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
        mProgressBar.setVisibility(View.VISIBLE);
        mChatRecyclerView.setVisibility(View.GONE);
        mActivity = getActivity();
        if (mActivity != null) {
            mChatInnerControlLayout = mActivity.findViewById(R.id.ll_chat_action_panel_inner);
            mChatControlLayout = mActivity.findViewById(R.id.ll_chat_action_panel);
            mSearchImageView = mActivity.findViewById(R.id.iv_chat_search);
            mCameraImageView = mActivity.findViewById(R.id.iv_chat_add_photo_from_camera);
            mGalleryImageView = mActivity.findViewById(R.id.iv_chat_add_photo_from_gallery);
            mSendImageView = mActivity.findViewById(R.id.iv_chat_send);
            mMessageEditText = mActivity.findViewById(R.id.et_chat);
            mMessageFilterEditText = mActivity.findViewById(R.id.et_chat_filter);
            mSearchImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Drawable.ConstantState currConstantState
                        = mSearchImageView.getDrawable().getConstantState();
                    Drawable.ConstantState baseConstantState
                            = getContext()
                            .getResources()
                            .getDrawable(R.drawable.baseline_search_black_48)
                            .getConstantState();
                    if (currConstantState == baseConstantState
                            && mMessageAdapter.getItemCount() == 0) {
                        Toast.makeText(
                                getContext(),
                                getString(R.string.chat_no_data_search_message),
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        if (currConstantState == baseConstantState) {
                            mSearchImageView.animate().rotation(90).start();
                            mSearchImageView.setImageDrawable(
                                    getContext().getResources()
                                            .getDrawable(R.drawable.baseline_clear_black_48)
                            );
                            mChatInnerControlLayout.setVisibility(View.GONE);
                            mMessageFilterEditText.setVisibility(View.VISIBLE);
                            mMessageFilterEditText.requestFocus();
                        } else {
                            mSearchImageView.animate().rotation(0).start();
                            mSearchImageView.setImageDrawable(
                                    getContext().getResources()
                                            .getDrawable(R.drawable.baseline_search_black_48)
                            );
                            mMessageFilterEditText.setText("");
                            mMessageFilterEditText.setVisibility(View.GONE);
                            mChatInnerControlLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String filter = charSequence.toString();
                if (filter.isEmpty()) {
                    mChatDetailList.clear();
                    mChatDetailList.addAll(
                            ApplicationHelper
                                    .getDateInfoNextToMessageData(getContext(), mMessageDetailList)
                    );
                    mMessageAdapter.notifyDataSetChanged();
                } else {
                    new SearchChatMessageAsyncTask(
                            mChatRecyclerView,
                            mMessageAdapter,
                            mChatDetailList,
                            mProgressBar,
                            getContext(),
                            mMessageDetailList
                    ).execute(filter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
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
