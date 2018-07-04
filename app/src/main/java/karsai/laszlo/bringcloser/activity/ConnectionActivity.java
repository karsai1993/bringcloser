package karsai.laszlo.bringcloser.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ConnectionDetailFragmentPagerAdapter;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class ConnectionActivity extends AppCompatActivity {

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
    /*mSearchFab = rootView.findViewById(R.id.fab_chat_search);
        mCameraImageView = rootView.findViewById(R.id.iv_chat_add_photo_from_camera);
        mGalleryImageView = rootView.findViewById(R.id.iv_chat_add_photo_from_gallery);
        mSendImageView = rootView.findViewById(R.id.iv_chat_send);
        mMessageEditText = rootView.findViewById(R.id.et_chat);*/
    @BindView(R.id.fab_chat_search)
    FloatingActionButton mSearchFab;
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

    private String mCurrentUserUid;
    private String mCurrentType;
    private String mOtherType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mConnectionsDatabaseReference;
    private ValueEventListener mConnectionUsersValueEventListener;
    private ConnectionDetailFragmentPagerAdapter mPageAdapter;
    private ConnectionDetail mConnectionDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onSupportNavigateUp();
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

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        Intent receivedData = getIntent();
        if (receivedData != null) {
            mConnectionDetail = receivedData.getParcelableExtra(ApplicationHelper.CONNECTION_KEY);
            mPageAdapter = new ConnectionDetailFragmentPagerAdapter(
                    getSupportFragmentManager(),
                    this,
                    mConnectionDetail
            );
            mViewPager.setAdapter(mPageAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mChatControlPanelLinearLayout.measure(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                mSearchFab.measure(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int heightFab = mSearchFab.getMeasuredHeight();
                int heightPanel = mChatControlPanelLinearLayout.getMeasuredHeight();
                float compOffset = 1F - positionOffset;
                if (position == 0) {
                    mChatControlPanelLinearLayout.setVisibility(View.VISIBLE);
                    mSearchFab.setVisibility(View.VISIBLE);
                    mChatControlPanelLinearLayout.setAlpha(compOffset);
                    mSearchFab.setAlpha(compOffset);
                    mChatControlPanelLinearLayout.setTranslationY(heightPanel * positionOffset);
                    mSearchFab.setTranslationY(heightFab * positionOffset);
                } else {
                    mChatControlPanelLinearLayout.setVisibility(View.GONE);
                    mSearchFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String message = editable.toString();
                if (message.isEmpty()) mSendImageView.setVisibility(View.GONE);
                else mSendImageView.setVisibility(View.VISIBLE);
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);
        mSendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectionsDatabaseReference
                        .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                        .equalTo(mConnectionDetail.getFromUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                                    String key = connectionSnapshot.getKey();
                                    if (dataSnapshot
                                            .child(key)
                                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                            .getValue(String.class)
                                            .equals(mConnectionDetail.getToUid())) {
                                        mConnectionsDatabaseReference
                                                .child(key)
                                                .child(ApplicationHelper.MESSAGES_NODE)
                                                .push()
                                                .setValue(
                                                        new Message(
                                                                mCurrentUserUid,
                                                                mMessageEditText.getText().toString(),
                                                                null,
                                                                ApplicationHelper
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
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseRef = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
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
    }

    private void populateToolbarData(ConnectionDetail connectionDetail) {
        String currentPhotoUrl;
        String otherPhotoUrl;
        String otherName;
        if (connectionDetail.getFromUid().equals(mCurrentUserUid)) {
            otherName = connectionDetail.getToName().split(" ")[0];
            currentPhotoUrl = connectionDetail.getFromPhotoUrl();
            otherPhotoUrl = connectionDetail.getToPhotoUrl();
            mOtherType = ApplicationHelper.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    false
            ).toUpperCase(Locale.getDefault());
            mCurrentType = ApplicationHelper.getPersonalizedRelationshipType(
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
            mCurrentType = ApplicationHelper.getPersonalizedRelationshipType(
                    this,
                    connectionDetail.getType(),
                    connectionDetail.getToGender(),
                    connectionDetail.getFromGender(),
                    false
            ).toUpperCase(Locale.getDefault());
            mOtherType = ApplicationHelper.getPersonalizedRelationshipType(
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
        ImageUtils.setUserPhoto(this, otherPhotoUrl, mToolbarOtherPhotoImageView);
        mToolbarCurrentRelationshipTextView.setText(mCurrentType);
        ImageUtils.setUserPhoto(this, currentPhotoUrl, mToolbarCurrentPhotoImageView);
        mToolbarSinceTextView.setText(
                ApplicationHelper.convertDateAndTimeToLocal(connectionDetail.getTimestamp())
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsersDatabaseRef.addValueEventListener(mConnectionUsersValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnectionUsersValueEventListener != null) {
            mUsersDatabaseRef.removeEventListener(mConnectionUsersValueEventListener);
        }
    }

    private void applyConnectionDeletionHandler() {
        final String otherName;
        final String fromUid = mConnectionDetail.getFromUid();
        if (fromUid.equals(mCurrentUserUid)) otherName = mConnectionDetail.getToName();
        else otherName = mConnectionDetail.getFromName();
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ApplicationHelper.deletePairConnection(
                        fromUid,
                        mConnectionDetail.getToUid(),
                        ConnectionActivity.this,
                        otherName
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
}
