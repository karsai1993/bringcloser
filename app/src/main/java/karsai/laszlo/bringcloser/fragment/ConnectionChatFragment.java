package karsai.laszlo.bringcloser.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.adapter.MessageAdapter;
import karsai.laszlo.bringcloser.background.SearchChatMessageAsyncTask;
import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.model.User;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Fragment to handle connection chat messages related information
 */
public class ConnectionChatFragment extends Fragment {

    public ConnectionChatFragment() {}

    private RecyclerView mChatRecyclerView;
    private ProgressBar mProgressBar;
    private String mCurrentUserUid;
    private ConnectionDetail mConnectionDetail;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mMessagesDatabaseRef;
    private Query mConnectionsQuery;
    private ValueEventListener mConnectionMessagesValueEventListener;
    private DatabaseReference mConnectionTypingDatabaseRef;
    private ValueEventListener mConnectionTypingValueEventListener;
    private List<MessageDetail> mMessageDetailList;
    private List<Message> mMessageList;
    private MessageAdapter mMessageAdapter;
    private TextView mChatNoDataTextView;
    private List<ChatDetail> mChatDetailList;
    private FloatingActionButton mScrollUpFab;
    private FloatingActionButton mScrollDownFab;
    private ImageView mSearchImageView;
    private EditText mMessageFilterEditText;
    private LinearLayout mChatInnerControlLayout;
    private TextWatcher mTextWatcher;
    private Activity mActivity;
    private Bundle mSavedInstanceState;
    private String mOtherUid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
                .child(ApplicationUtils.CONNECTIONS_NODE);
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.USERS_NODE);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mConnectionDetail = bundle.getParcelable(ApplicationUtils.CONNECTION_DETAIL_KEY);
        }
        mMessageList = new ArrayList<>();
        mMessageDetailList = new ArrayList<>();
        mChatDetailList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(
                getContext(),
                mChatDetailList
        );
        mChatRecyclerView.setAdapter(mMessageAdapter);

        if (mCurrentUserUid.equals(mConnectionDetail.getFromUid())) {
            mOtherUid = mConnectionDetail.getToUid();
        } else {
            mOtherUid = mConnectionDetail.getFromUid();
        }
        mConnectionTypingDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CHAT_TYPING_NODE)
                .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                .child(mOtherUid);
        mConnectionsQuery = mConnectionsDatabaseReference
                .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid());
        mConnectionMessagesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null connection from chat");
                        continue;
                    }
                    String toUidValue = dataSnapshot
                            .child(key)
                            .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) {
                        Timber.wtf("to uid null chat");
                        continue;
                    }
                    if (toUidValue.equals(mConnectionDetail.getToUid())) {
                        mMessagesDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationUtils.MESSAGES_NODE);
                        mMessagesDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mMessageList.clear();
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    Message message = messageSnapshot.getValue(Message.class);
                                    if (message == null) {
                                        Timber.wtf("message null");
                                        continue;
                                    }
                                    mMessageList.add(message);
                                }
                                mUsersDatabaseReference
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(
                                                    @NonNull DataSnapshot dataSnapshot) {
                                                Context contextForUserChange = getContext();
                                                if (contextForUserChange == null) return;
                                                int prevCount = mChatDetailList.size();
                                                mChatDetailList.clear();
                                                mMessageDetailList.clear();
                                                mMessageFilterEditText
                                                        .removeTextChangedListener(mTextWatcher);
                                                for (Message message : mMessageList) {
                                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                        String uid = userSnapshot.getKey();
                                                        User user = userSnapshot.getValue(User.class);
                                                        if (user == null) {
                                                            Timber.wtf("user null collecting messagedetails");
                                                            continue;
                                                        }
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
                                                        ApplicationUtils.getDateInfoNextToMessageData(
                                                                contextForUserChange,
                                                                mMessageDetailList
                                                        )
                                                );
                                                mProgressBar.setVisibility(View.GONE);
                                                int messageNum = mChatDetailList.size();
                                                if (messageNum > 0) {
                                                    mMessageAdapter.notifyDataSetChanged();
                                                    mChatNoDataTextView.setVisibility(View.GONE);
                                                    mChatRecyclerView.setVisibility(View.VISIBLE);
                                                } else {
                                                    mChatRecyclerView.setVisibility(View.GONE);
                                                    mChatNoDataTextView.setVisibility(View.VISIBLE);
                                                }
                                                assignTypingListener();
                                                mMessageFilterEditText
                                                        .addTextChangedListener(mTextWatcher);
                                                String filter = mMessageFilterEditText.getText().toString();
                                                if (!filter.isEmpty()) {
                                                    mSearchImageView.setImageDrawable(
                                                            contextForUserChange.getResources()
                                                                    .getDrawable(R.drawable.baseline_clear_black_48)
                                                    );
                                                    mChatInnerControlLayout.setVisibility(View.GONE);
                                                    mMessageFilterEditText.setVisibility(View.VISIBLE);
                                                    new SearchChatMessageAsyncTask(
                                                            new WeakReference<RecyclerView>(mChatRecyclerView),
                                                            mMessageAdapter,
                                                            mChatDetailList,
                                                            new WeakReference<ProgressBar>(mProgressBar),
                                                            new WeakReference<Context>(getContext()),
                                                            mMessageDetailList
                                                    ).execute(filter);
                                                }
                                                if (mSavedInstanceState == null
                                                        || (prevCount > 0
                                                        && messageNum > prevCount)) {
                                                    mChatRecyclerView.scrollToPosition(
                                                            mMessageAdapter.getItemCount() - 1
                                                    );
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
            mSearchImageView = mActivity.findViewById(R.id.iv_chat_search);
            mMessageFilterEditText = mActivity.findViewById(R.id.et_chat_filter);

            mSearchImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Drawable.ConstantState currConstantState
                            = mSearchImageView.getDrawable().getConstantState();
                    Context context = getContext();
                    if (context == null) {
                        Timber.wtf("context null for chat search");
                        return;
                    }
                    Drawable.ConstantState baseConstantState
                            = context
                            .getResources()
                            .getDrawable(R.drawable.baseline_search_black_48)
                            .getConstantState();
                    if (currConstantState == baseConstantState
                            && mMessageAdapter.getItemCount() == 0) {
                        Toast.makeText(
                                context,
                                getString(R.string.chat_no_data_search_message),
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        if (currConstantState == baseConstantState) {
                            mSearchImageView.animate().rotation(90).start();
                            mSearchImageView.setImageDrawable(
                                    context.getResources()
                                            .getDrawable(R.drawable.baseline_clear_black_48)
                            );
                            mChatInnerControlLayout.setVisibility(View.GONE);
                            mMessageFilterEditText.setVisibility(View.VISIBLE);
                            mMessageFilterEditText.requestFocus();
                        } else {
                            mSearchImageView.animate().rotation(0).start();
                            mSearchImageView.setImageDrawable(
                                    context.getResources()
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
                            ApplicationUtils
                                    .getDateInfoNextToMessageData(getContext(), mMessageDetailList)
                    );
                    mMessageAdapter.notifyDataSetChanged();
                } else {
                    new SearchChatMessageAsyncTask(
                            new WeakReference<RecyclerView>(mChatRecyclerView),
                            mMessageAdapter,
                            mChatDetailList,
                            new WeakReference<ProgressBar>(mProgressBar),
                            new WeakReference<Context>(getContext()),
                            mMessageDetailList
                    ).execute(filter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        mConnectionTypingValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isTyping = dataSnapshot.getValue(Boolean.class);
                    if (isTyping != null) {
                        applyTypingHandling(isTyping);
                    } else {
                        applyTypingHandling(false);
                    }
                } else {
                    applyTypingHandling(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        return rootView;
    }

    private void applyTypingHandling(final boolean isTyping) {
        mUsersDatabaseReference
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot dataSnapshot) {
                        mChatDetailList.clear();
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String uid = userSnapshot.getKey();
                            User user = userSnapshot.getValue(User.class);
                            if (user == null) continue;
                            if (mOtherUid.equals(uid)) {
                                String fromPhotoUrl
                                        = user.getPhotoUrl();
                                if (isTyping) {
                                    mMessageDetailList.add(
                                            new MessageDetail(
                                                    null,
                                                    fromPhotoUrl,
                                                    null,
                                                    null,
                                                    ApplicationUtils.getCurrentUTCDateAndTime()
                                            )
                                    );
                                } else {
                                    int lastPos = mMessageDetailList.size() - 1;
                                    if (lastPos >= 0) {
                                        MessageDetail lastItem = mMessageDetailList.get(lastPos);
                                        if (lastItem.getText() == null && lastItem.getPhotoUrl() == null) {
                                            mMessageDetailList.remove(lastPos);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        mChatDetailList.addAll(
                                ApplicationUtils.getDateInfoNextToMessageData(
                                        getContext(),
                                        mMessageDetailList
                                )
                        );
                        int messageNum = mChatDetailList.size();
                        if (messageNum > 0) {
                            mMessageAdapter.notifyDataSetChanged();
                            mChatNoDataTextView.setVisibility(View.GONE);
                            mChatRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            mChatRecyclerView.setVisibility(View.GONE);
                            mChatNoDataTextView.setVisibility(View.VISIBLE);
                        }
                        if (isTyping) {
                            int lastPos = ((LinearLayoutManager)mChatRecyclerView.getLayoutManager())
                                    .findLastVisibleItemPosition();
                            if (lastPos == mMessageAdapter.getItemCount() - 2) {
                                mChatRecyclerView.scrollToPosition(
                                        mMessageAdapter.getItemCount() - 1);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionsQuery.addListenerForSingleValueEvent(mConnectionMessagesValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        unAssignTypingListener();
        if (mConnectionMessagesValueEventListener != null) {
            mConnectionsQuery.removeEventListener(mConnectionMessagesValueEventListener);
        }
    }

    private void unAssignTypingListener() {
        if (mConnectionTypingValueEventListener != null) {
            mConnectionTypingDatabaseRef.removeEventListener(mConnectionTypingValueEventListener);
        }
    }
    private void assignTypingListener() {
        unAssignTypingListener();
        mConnectionTypingDatabaseRef.addValueEventListener(mConnectionTypingValueEventListener);
    }
}
