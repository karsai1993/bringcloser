package karsai.laszlo.bringcloser.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
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
    TextView mToolbarExpandedTitleTextView;
    @BindView(R.id.tv_connection_toolbar_relationship)
    TextView mToolbarRelationshipTextView;
    @BindView(R.id.tv_connection_toolbar_since)
    TextView mToolbarSinceTextView;
    @BindView(R.id.iv_connection_toolbar_other_photo)
    ImageView mToolbarPhotoImageView;

    private String mCurrentUserUid;
    private String mCurrentType;
    private String mOtherType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseRef;
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

        Intent receivedData = getIntent();
        if (receivedData != null) {
            mConnectionDetail = receivedData.getParcelableExtra(ApplicationHelper.CONNECTION_KEY);
        }
        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
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

        mPageAdapter = new ConnectionDetailFragmentPagerAdapter(
                getSupportFragmentManager(),
                this
        );
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void populateToolbarData(ConnectionDetail connectionDetail) {
        String title;
        String photoUrl;
        if (connectionDetail.getFromUid().equals(mCurrentUserUid)) {
            title = new StringBuilder()
                    .append(
                            getResources()
                                    .getString(R.string.connection_activity_toolbar_title)
                    ).append(connectionDetail.getToName().split(" ")[0])
                    .toString();
            photoUrl = connectionDetail.getToPhotoUrl();
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
            title = new StringBuilder()
                    .append(
                            getResources()
                                    .getString(R.string.connection_activity_toolbar_title)
                    ).append(connectionDetail.getFromName().split(" ")[0])
                    .toString();
            photoUrl = connectionDetail.getFromPhotoUrl();
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
        mToolbarTitleTextView.setText(title);
        mToolbarExpandedTitleTextView.setText(title);
        mToolbarSinceTextView.setText(
                ApplicationHelper.convertDateAndTimeToLocal(connectionDetail.getTimestamp())
        );
        mToolbarRelationshipTextView.setText(
                new StringBuilder().append(mCurrentType).append(" - ").append(mOtherType).toString()
        );
        ImageUtils.setUserPhoto(this, photoUrl, mToolbarPhotoImageView);
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
