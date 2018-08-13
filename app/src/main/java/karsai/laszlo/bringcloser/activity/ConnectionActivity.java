package karsai.laszlo.bringcloser.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.adapter.ConnectionDetailFragmentPagerAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity to show information about each connections
 */
public class ConnectionActivity extends CommonActivity {

    @BindView(R.id.connection_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.connection_collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.connection_app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.connection_root_view)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.viewpager_connection_details)
    ViewPager mViewPager;
    @BindView(R.id.tab_layout_connection_details)
    TabLayout mTabLayout;
    @BindView(R.id.tv_connection_toolbar_title)
    TextView mToolbarTitleTextView;
    @BindView(R.id.tv_connection_toolbar_other_name)
    TextView mToolbarOtherNameTextView;
    @BindView(R.id.tv_connection_toolbar_other_relationship)
    TextView mToolbarOtherRelationshipTextView;
    @BindView(R.id.iv_connection_toolbar_other_photo)
    ImageView mToolbarOtherPhotoImageView;
    @BindView(R.id.tv_connection_toolbar_current_relationship)
    TextView mToolbarCurrentRelationshipTextView;
    @BindView(R.id.iv_connection_toolbar_current_photo)
    ImageView mToolbarCurrentPhotoImageView;
    @BindView(R.id.tv_connection_toolbar_since)
    TextView mToolbarSinceTextView;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;

    @BindView(R.id.iv_chat_add_photo_from_camera)
    ImageView mCameraImageView;
    @BindView(R.id.iv_chat_add_photo_from_gallery)
    ImageView mGalleryImageView;
    @BindView(R.id.iv_chat_send)
    ImageView mSendImageView;
    @BindView(R.id.et_chat)
    EditText mMessageEditText;
    @BindView(R.id.ll_chat_action_panel)
    LinearLayout mChatControlPanelLinearLayout;
    @BindView(R.id.cl_ctrl_panel)
    CoordinatorLayout mChatControlPanelCoordinatorLayout;

    @BindView(R.id.fab_plus_one_wish)
    FloatingActionButton mPlusOneWishFab;
    @BindView(R.id.fab_plus_one_event)
    FloatingActionButton mPlusOneEventFab;
    @BindView(R.id.fab_plus_one_thought)
    FloatingActionButton mPlusOneThoughtFab;

    private String mCurrentUserUid;
    private FirebaseUser mFirebaseUser;
    private String mCurrentType;
    private String mOtherType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mConnectionsDatabaseReference;
    private ValueEventListener mConnectionUsersValueEventListener;
    private ChildEventListener mConnectionChildValueEventListener;
    private ConnectionDetailFragmentPagerAdapter mPageAdapter;
    private ConnectionDetail mConnectionDetail;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mMessageImagesRootRef;
    private StorageReference mMessageImagesRef;
    private TextWatcher mTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.connection_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete_connection:
                        applyConnectionDeletionHandler();
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        mToolbarTitleTextView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mToolbarTitleTextView.getVisibility() == View.VISIBLE) {
                            mToolbarTitleTextView
                                    .animate()
                                    .translationX(0F)
                                    .alpha(1F)
                                    .setDuration(500)
                                    .start();
                        } else {
                            mToolbarTitleTextView.setTranslationX(mAppBarLayout.getWidth());
                            mToolbarTitleTextView.setAlpha(0F);
                        }
                    }
                });
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset < getResources().getInteger(R.integer.collapsing_threshold)) {
                    mToolbarTitleTextView.setVisibility(View.VISIBLE);
                } else {
                    mToolbarTitleTextView.setVisibility(View.GONE);
                }
            }
        });

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        Intent receivedData = getIntent();
        if (receivedData != null) {
            mConnectionDetail = receivedData.getParcelableExtra(ApplicationUtils.CONNECTION_KEY);
            mPageAdapter = new ConnectionDetailFragmentPagerAdapter(
                    getSupportFragmentManager(),
                    this,
                    mConnectionDetail
            );
            mViewPager.setAdapter(mPageAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
            if (mViewPager.getCurrentItem() == 0) {
                mAppBarLayout.setExpanded(false);
            }
            mMessageImagesRootRef = mFirebaseStorage.getReference()
                    .child(mCurrentUserUid)
                    .child(ApplicationUtils.STORAGE_MESSAGE_IMAGES_FOLDER);
        } else {
            Timber.wtf("received data returned null connection activity");
        }
        mGalleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUtils.onClickFromFile(ConnectionActivity.this);
            }
        });
        mCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUtils.onClickFromCamera(ConnectionActivity.this);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mChatControlPanelLinearLayout.measure(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int heightPanel = mChatControlPanelLinearLayout.getMeasuredHeight();
                float compOffset = 1F - positionOffset;
                if (position == 0) {
                    mPlusOneWishFab.setVisibility(View.GONE);
                    mPlusOneEventFab.setVisibility(View.GONE);
                    mPlusOneThoughtFab.setVisibility(View.GONE);
                    mChatControlPanelLinearLayout.setVisibility(View.VISIBLE);
                    mChatControlPanelLinearLayout.setAlpha(compOffset);
                    mChatControlPanelLinearLayout.setTranslationY(heightPanel * positionOffset);
                } else {
                    mChatControlPanelLinearLayout.setVisibility(View.GONE);
                    if (position == 1) {
                        mPlusOneEventFab.setVisibility(View.GONE);
                        mPlusOneThoughtFab.setVisibility(View.GONE);
                        mPlusOneWishFab.setVisibility(View.VISIBLE);
                    } else if (position == 2) {
                        mPlusOneWishFab.setVisibility(View.GONE);
                        mPlusOneThoughtFab.setVisibility(View.GONE);
                        mPlusOneEventFab.setVisibility(View.VISIBLE);
                    } else {
                        mPlusOneWishFab.setVisibility(View.GONE);
                        mPlusOneEventFab.setVisibility(View.GONE);
                        mPlusOneThoughtFab.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                float height;
                if (position == 0) {
                    registerChatVisibility();
                    height = getResources().getDimension(R.dimen.chat_ctrl_panel_height);
                } else {
                    unregisterChatVisibility();
                    height = getResources().getDimension(R.dimen.not_chat_panel_height);
                }
                mChatControlPanelCoordinatorLayout.getLayoutParams().height = (int) height;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mChatControlPanelLinearLayout.setBackgroundColor(Color.WHITE);
                } else {
                    mChatControlPanelLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
        mMessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (!isFocused) {
                    unregisterChatTyping();
                } else if (!mMessageEditText.getText().toString().isEmpty()) {
                    registerChatTyping();
                }
            }
        });
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString();
                if (input.isEmpty()) {
                    unregisterChatTyping();
                } else {
                    registerChatTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String message = editable.toString();
                if (message.isEmpty()) {
                    mSendImageView.setVisibility(View.GONE);
                }
                else {
                    mSendImageView.setVisibility(View.VISIBLE);
                }
            }
        };
        mMessageEditText.addTextChangedListener(mTextWatcher);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CONNECTIONS_NODE);
        mConnectionChildValueEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
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
                if (otherGender.equals(getResources().getString(R.string.gender_female))) {
                    genderRepresentative
                            = getResources().getString(R.string.gender_representative_female);
                } else if (otherGender.equals(getResources().getString(R.string.gender_male))) {
                    genderRepresentative
                            = getResources().getString(R.string.gender_representative_male);
                } else {
                    genderRepresentative
                            = getResources().getString(R.string.gender_representative_none);
                }
                Toast.makeText(
                        ConnectionActivity.this,
                        new StringBuilder()
                                .append(getResources().getString(R.string.other_people_delete_common_1))
                                .append(otherName)
                                .append(getResources().getString(R.string.other_people_delete_common_2))
                                .append(genderRepresentative)
                                .append(getResources().getString(R.string.other_people_delete_common_3))
                                .toString(),
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mSendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = ApplicationUtils.convertTextToEmojiIfNeeded(
                        getApplicationContext(),
                        mMessageEditText.getText().toString()
                );
                createNewMessageObject(message, null);
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.USERS_NODE);
        mConnectionUsersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fromUid = mConnectionDetail.getFromUid();
                String toUid = mConnectionDetail.getToUid();
                String type = mConnectionDetail.getType();
                String timestamp = mConnectionDetail.getTimestamp();
                ConnectionDetail connectionDetail = new ConnectionDetail();
                boolean isFromDataRead = false;
                boolean isToDataRead = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    User user = userSnapshot.getValue(User.class);
                    if (user == null) {
                        Timber.wtf("user null getting connected data " + uid);
                        continue;
                    }
                    if (fromUid.equals(uid)) {
                        connectionDetail.setFromUid(uid);
                        connectionDetail.setFromGender(user.getGender());
                        connectionDetail.setFromName(user.getUsername());
                        connectionDetail.setFromPhotoUrl(user.getPhotoUrl());
                        connectionDetail.setFromBirthday(user.getBirthday());
                        isFromDataRead = true;
                    } else if (toUid.equals(uid)) {
                        connectionDetail.setToUid(uid);
                        connectionDetail.setToGender(user.getGender());
                        connectionDetail.setToName(user.getUsername());
                        connectionDetail.setToPhotoUrl(user.getPhotoUrl());
                        connectionDetail.setToBirthday(user.getBirthday());
                        isToDataRead = true;
                    }
                    if (isFromDataRead && isToDataRead) {
                        connectionDetail.setType(type);
                        connectionDetail.setTimestamp(timestamp);
                        populateToolbarData(connectionDetail);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mMessageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    mViewPager.requestFocus();
                } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    mGalleryImageView.requestFocus();
                } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                        && !mMessageEditText.getText().toString().isEmpty()) {
                    mSendImageView.requestFocus();
                }
                return false;
            }
        });
        mViewPager.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    onSupportNavigateUp();
                }
                return false;
            }
        });
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    private void unregisterChatVisibility() {
        mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CHAT_VISIBILITY_NODE)
                .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                .child(mCurrentUserUid)
                .setValue(false);
    }

    private void registerChatVisibility() {
        mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CHAT_VISIBILITY_NODE)
                .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                .child(mCurrentUserUid)
                .setValue(true);
    }

    private void unregisterChatTyping() {
        mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CHAT_TYPING_NODE)
                .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                .child(mCurrentUserUid)
                .setValue(false);
    }

    private void registerChatTyping() {
        mFirebaseDatabase.getReference()
                .child(ApplicationUtils.CHAT_TYPING_NODE)
                .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                .child(mCurrentUserUid)
                .setValue(true);
    }

    private void createNewMessageObject(final String text, final String photoUrl) {
        mConnectionsDatabaseReference
                .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mConnectionDetail.getFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("connections key null create new message");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid not found");
                                continue;
                            }
                            if (toUidValue.equals(mConnectionDetail.getToUid())) {
                                mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationUtils.MESSAGES_NODE)
                                        .push()
                                        .setValue(
                                                new Message(
                                                        mConnectionDetail.getFromUid(),
                                                        mConnectionDetail.getToUid(),
                                                        mCurrentUserUid,
                                                        mCurrentUserUid
                                                                .equals(mConnectionDetail.getToUid()) ?
                                                                mConnectionDetail.getFromUid() :
                                                                mConnectionDetail.getToUid(),
                                                        text,
                                                        photoUrl,
                                                        ApplicationUtils
                                                                .getCurrentUTCDateAndTime()
                                                )
                                        );
                                mMessageEditText.setText("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        mAppBarLayout.setExpanded(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ImageUtils.RC_PHOTO_PICKER || requestCode == ImageUtils.REQUEST_IMAGE_CAPTURE)
                && resultCode == RESULT_OK) {
            mMessageImagesRef = mMessageImagesRootRef.child(
                    ApplicationUtils.getCurrentUTCDateAndTime()
            );
        }
        OnSuccessListener<UploadTask.TaskSnapshot> uploadOnSuccessListener
                = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                if (storageMetadata == null) {
                    return;
                }
                mMessageImagesRef = storageMetadata.getReference();
                if (mMessageImagesRef == null) {
                    return;
                }
                mMessageImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String photoUrl = uri.toString();
                        createNewMessageObject(null, photoUrl);
                    }
                });
            }
        };
        ImageUtils.onActivityResult(
                ConnectionActivity.this,
                requestCode,
                resultCode,
                data,
                mMessageImagesRef,
                uploadOnSuccessListener
        );
    }

    private void populateToolbarData(ConnectionDetail connectionDetail) {
        String currentPhotoUrl;
        String otherPhotoUrl;
        String otherName;
        if (connectionDetail.getFromUid().equals(mCurrentUserUid)) {
            otherName = connectionDetail.getToName().split(" ")[0];
            currentPhotoUrl = connectionDetail.getFromPhotoUrl();
            otherPhotoUrl = connectionDetail.getToPhotoUrl();
            mOtherType = ApplicationUtils.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    false
            ).toUpperCase(Locale.getDefault());
            mCurrentType = ApplicationUtils.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    true
            ).toUpperCase(Locale.getDefault());
        } else {
            otherName = connectionDetail.getFromName().split(" ")[0];
            currentPhotoUrl = connectionDetail.getToPhotoUrl();
            otherPhotoUrl = connectionDetail.getFromPhotoUrl();
            mCurrentType = ApplicationUtils.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    false
            ).toUpperCase(Locale.getDefault());
            mOtherType = ApplicationUtils.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    true
            ).toUpperCase(Locale.getDefault());
        }
        String title = new StringBuilder()
                .append(
                        getResources()
                                .getString(R.string.connection_activity_toolbar_title)
                ).append(otherName)
                .toString();

        mToolbarTitleTextView.setText(title);
        mToolbarOtherNameTextView.setText(otherName);
        mToolbarOtherRelationshipTextView.setText(mOtherType);
        ImageUtils.setPhoto(this, otherPhotoUrl, mToolbarOtherPhotoImageView, true);
        mToolbarCurrentRelationshipTextView.setText(mCurrentType);
        ImageUtils.setPhoto(this, currentPhotoUrl, mToolbarCurrentPhotoImageView, true);
        String dateAndTimeLocal = ApplicationUtils.getLocalDateAndTimeToDisplay(
                this,
                connectionDetail.getTimestamp()
        );
        mToolbarSinceTextView.setText(dateAndTimeLocal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewPager.getCurrentItem() == 0) {
            registerChatVisibility();
        }
        mUsersDatabaseRef.addValueEventListener(mConnectionUsersValueEventListener);
        mConnectionsDatabaseReference.addChildEventListener(mConnectionChildValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mViewPager.getCurrentItem() == 0) {
            mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.CONNECTIONS_NODE)
                    .child(mConnectionDetail.getFromUid() + "_" + mConnectionDetail.getToUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                unregisterChatVisibility();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        if (mConnectionUsersValueEventListener != null) {
            mUsersDatabaseRef.removeEventListener(mConnectionUsersValueEventListener);
        }
        if (mConnectionChildValueEventListener != null) {
            mConnectionsDatabaseReference.removeEventListener(mConnectionChildValueEventListener);
        }
    }

    private void deletePairMemoryElements(
            final Context context,
            final String fromUid,
            final String toUid,
            final String name,
            final String otherUid) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef
                = firebaseDatabase.getReference().child(ApplicationUtils.CONNECTIONS_NODE);
        Query connectionsQuery = connectionsRef
                .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(fromUid);
        connectionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (key == null) continue;
                    String toUidValue = dataSnapshot
                            .child(key)
                            .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) continue;
                    if (toUidValue.equals(toUid)) {
                        DatabaseReference messagesDatabaseRef = connectionsRef
                                .child(key)
                                .child(ApplicationUtils.MESSAGES_NODE);
                        final DatabaseReference wishesDatabaseRef = connectionsRef
                                .child(key)
                                .child(ApplicationUtils.WISHES_NODE);
                        final DatabaseReference eventsDatabaseRef = connectionsRef
                                .child(key)
                                .child(ApplicationUtils.EVENTS_NODE);
                        final DatabaseReference thoughtsDatabaseRef = connectionsRef
                                .child(key)
                                .child(ApplicationUtils.THOUGHTS_NODE);
                        deleteMemoryElementsAndConnection(
                                messagesDatabaseRef,
                                wishesDatabaseRef,
                                eventsDatabaseRef,
                                thoughtsDatabaseRef,
                                context,
                                fromUid,
                                toUid,
                                name,
                                otherUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteMemoryElementsAndConnection(
            DatabaseReference messagesDatabaseRef,
            final DatabaseReference wishesDatabaseRef,
            final DatabaseReference eventsDatabaseRef,
            final DatabaseReference thoughtsDatabaseRef,
            final Context context,
            final String fromUid,
            final String toUid,
            final String name,
            final String otherUid) {
        final String currUid;
        if (otherUid.equals(fromUid)) {
            currUid = toUid;
        } else {
            currUid = fromUid;
        }
        messagesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message == null) continue;
                    ApplicationUtils.deleteImageFromStorage(
                            context,
                            message.getPhotoUrl(),
                            currUid,
                            otherUid);
                }
                wishesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Wish wish = snapshot.getValue(Wish.class);
                            if (wish == null) continue;
                            ApplicationUtils.deleteImageFromStorage(
                                    context,
                                    wish.getExtraPhotoUrl(),
                                    currUid,
                                    otherUid);
                        }
                        eventsDatabaseRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Event event = snapshot.getValue(Event.class);
                                            if (event == null) continue;
                                            ApplicationUtils.deleteImageFromStorage(
                                                    context,
                                                    event.getExtraPhotoUrl(),
                                                    currUid,
                                                    otherUid);
                                        }
                                        thoughtsDatabaseRef.addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            Thought thought = snapshot.getValue(Thought.class);
                                                            if (thought == null) continue;
                                                            ApplicationUtils.deleteImageFromStorage(
                                                                    context,
                                                                    thought.getExtraPhotoUrl(),
                                                                    currUid,
                                                                    otherUid);
                                                        }
                                                        if (fromUid != null
                                                                && toUid != null
                                                                && name != null) {
                                                            ApplicationUtils.deletePairConnection(
                                                                    fromUid,
                                                                    toUid,
                                                                    context,
                                                                    name);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void applyConnectionDeletionHandler() {
        final String otherName;
        final String otherUid;
        final String fromUid = mConnectionDetail.getFromUid();
        if (fromUid.equals(mCurrentUserUid)) {
            otherName = mConnectionDetail.getToName();
            otherUid = mConnectionDetail.getToUid();
        }
        else {
            otherName = mConnectionDetail.getFromName();
            otherUid = mConnectionDetail.getFromUid();
        }
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mConnectionChildValueEventListener != null) {
                    mConnectionsDatabaseReference.removeEventListener(mConnectionChildValueEventListener);
                }
                deletePairMemoryElements(
                        ConnectionActivity.this,
                        fromUid,
                        mConnectionDetail.getToUid(),
                        otherName,
                        otherUid
                );
                finish();
            }
        };
        TextView deleteConnection = new TextView(this);
        deleteConnection.setText(
                new StringBuilder()
                        .append("\n")
                        .append(otherName)
                        .append("\n")
                        .append(mOtherType)
                        .toString()
        );
        deleteConnection.setGravity(Gravity.CENTER);
        DialogUtils.onDialogRequest(
                this,
                getResources().getString(R.string.dialog_connection_delete_title),
                deleteConnection,
                onClickListener,
                R.style.DialogUpDownTheme
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageUtils.onRequestPermissionResult(
                ConnectionActivity.this,
                requestCode,
                grantResults
        );
    }
}
