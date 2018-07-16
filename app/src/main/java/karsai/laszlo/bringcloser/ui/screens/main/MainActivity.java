package karsai.laszlo.bringcloser.ui.screens.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.BringCloserApp;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.ui.screens.common.CommonActivity;
import karsai.laszlo.bringcloser.ui.screens.settings.SettingsActivity;
import karsai.laszlo.bringcloser.ui.screens.welcome.WelcomeActivity;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import karsai.laszlo.bringcloser.utils.NetworkUtils;

public class MainActivity extends CommonActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainView {

    @Inject
    MainActivityPresenter mPresenter;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.tab_layout_connection_types) TabLayout mTabLayout;
    @BindView(R.id.viewpager_connection_types) ViewPager mViewPager;
    @BindView(R.id.ll_connections) LinearLayout mContentLinearLayout;
    @BindView(R.id.pb_connections) ProgressBar mContentProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mCurrentUserDatabaseReference;
    private ValueEventListener mUserDatabaseReferenceEventListener;
    private User mCurrentUser;
    private TextView mUserNameInNavDrawer;
    private TextView mUserBirthdayInNavDrawer;
    private TextView mUserGenderInNavDrawer;
    private TextView mUserEmailVerificationInNavDrawer;
    private ImageView mUserPhotoInNavDrawer;
    private FirebaseUser mFirebaseUser;
    private TextView mToolbarTitleTextView;
    private ProgressBar mToolbarTitleProgressBar;
    private String mToken;
    private String mStoredToken;
    private String mNewStoredToken;
    private Map<String, Object> mTokenMap;
    private String mCurrentUserUid;
    private ConnectionFragmentPagerAdapter mConnectionFragmentPagerAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUserDatabaseReference.addValueEventListener(mUserDatabaseReferenceEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUserDatabaseReferenceEventListener != null) {
            mCurrentUserDatabaseReference.removeEventListener(mUserDatabaseReferenceEventListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attachView(this);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        onSignedInInitialize();
    }

    @Override
    public void injectDependencies() {
        ((BringCloserApp) getApplication())
                .getMyComponent()
                .inject(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setPageBasedOnIntent(intent);
    }

    private void onSignedInInitialize() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_no_internet);
        }
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mToken = FirebaseInstanceId.getInstance().getToken();
        mTokenMap = new HashMap<>();
        mTokenMap.put(mToken, true);
        ButterKnife.bind(this);
        mCurrentUserUid = mFirebaseUser.getUid();
        mCurrentUser = new User(
                mFirebaseUser.isEmailVerified(),
                mFirebaseUser.getDisplayName(),
                mFirebaseUser.getPhotoUrl() != null ? mFirebaseUser.getPhotoUrl().toString() : null,
                getResources().getString(R.string.settings_birthday_default),
                getResources().getString(R.string.gender_none),
                mTokenMap,
                mCurrentUserUid
        );
        initializeUsersData();
        mToolbar.setTitle("");
        mToolbarTitleTextView = mToolbar.findViewById(R.id.tv_main_toolbar);
        mToolbarTitleProgressBar = mToolbar.findViewById(R.id.pb_main_toolbar);
        mToolbarTitleProgressBar.setVisibility(View.VISIBLE);
        mContentLinearLayout.setVisibility(View.GONE);
        mContentProgressBar.setVisibility(View.VISIBLE);
        mToolbarTitleTextView.setText(getResources().getString(R.string.starting_toolbar_title));
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        View navigationHeaderView = mNavigationView.getHeaderView(0);
        mUserNameInNavDrawer = navigationHeaderView.findViewById(R.id.tv_user_name);
        mUserPhotoInNavDrawer = navigationHeaderView.findViewById(R.id.iv_user_photo);
        mUserBirthdayInNavDrawer = navigationHeaderView.findViewById(R.id.tv_user_birthday);
        mUserGenderInNavDrawer = navigationHeaderView.findViewById(R.id.tv_user_gender);
        mUserEmailVerificationInNavDrawer
                = navigationHeaderView.findViewById(R.id.tv_user_email_verification);

        mViewPager.setId(R.id.view_pager_id);
        mConnectionFragmentPagerAdapter = new ConnectionFragmentPagerAdapter(
                getSupportFragmentManager(),
                getApplicationContext()
        );
        mViewPager.setAdapter(mConnectionFragmentPagerAdapter);
        Intent receivedData = getIntent();
        setPageBasedOnIntent(receivedData);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setPageBasedOnIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ApplicationHelper.NOTIFICATION_INTENT_ACTION_PAGE_REQUEST))
                    mViewPager.setCurrentItem(2);
                else if (action.equals(ApplicationHelper.NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION))
                    mViewPager.setCurrentItem(0);
                else if (action
                        .equals(ApplicationHelper.NEW_SENT_REQUEST_INTENT_ACTION_PAGE_CONNECTION)) {
                    mViewPager.setCurrentItem(1);
                }
            }
        }
    }

    private void initializeUsersData() {
        mCurrentUserDatabaseReference
                = mUsersDatabaseReference.child(mCurrentUserUid);
        mUserDatabaseReferenceEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = dataSnapshot.getValue(User.class);
                    setToolbarTitle(mCurrentUser.getUsername());
                    mContentLinearLayout.setVisibility(View.VISIBLE);
                    mContentProgressBar.setVisibility(View.GONE);
                    setUserDataInNavigationDrawer();
                    ImageUtils.setPhoto(
                            getApplicationContext(),
                            mCurrentUser.getPhotoUrl(),
                            mUserPhotoInNavDrawer,
                            true
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mCurrentUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    DatabaseReference databaseReference
                            = mUsersDatabaseReference.child(mCurrentUserUid);
                    databaseReference.setValue(mCurrentUser);
                } else {
                    mCurrentUser = dataSnapshot.getValue(User.class);
                    if (mFirebaseUser.isEmailVerified()){
                        if (!mCurrentUser.getIsEmailVerified()) {
                            mCurrentUser.setIsEmailVerified(true);
                            Map<String, Object> updateVerificationValue = new HashMap<>();
                            updateVerificationValue.put(
                                    "/" + ApplicationHelper.USER_EMAIL_VERIFICATION_IDENTIFIER,
                                    true
                            );
                            mCurrentUserDatabaseReference.updateChildren(updateVerificationValue);
                        }
                    }
                    mStoredToken = ApplicationHelper.getTokenFromPrefs(
                            getApplicationContext(),
                            ApplicationHelper.TOKEN_STORAGE_KEY
                    );
                    mNewStoredToken = ApplicationHelper.getTokenFromPrefs(
                            getApplicationContext(),
                            ApplicationHelper.TOKEN_NEW_STORAGE_KEY
                    );
                    if (mStoredToken == null) {
                        mCurrentUserDatabaseReference
                                .child(ApplicationHelper.USER_TOKENS_IDENTIFIER)
                                .child(mToken)
                                .setValue(true);
                        ApplicationHelper.saveTokenToPrefs(
                                getApplicationContext(),
                                ApplicationHelper.TOKEN_STORAGE_KEY,
                                mToken
                        );
                    } else if (mNewStoredToken != null) {
                        mTokenMap.put(mStoredToken, null);
                        mCurrentUser.setTokensMap(mTokenMap);
                        mCurrentUserDatabaseReference
                                .child(ApplicationHelper.USER_TOKENS_IDENTIFIER)
                                .updateChildren(mTokenMap);
                        ApplicationHelper.saveTokenToPrefs(
                                getApplicationContext(),
                                ApplicationHelper.TOKEN_NEW_STORAGE_KEY,
                                null
                        );
                    } else if (mStoredToken.equals(mToken)) {
                        mCurrentUserDatabaseReference
                                .child(ApplicationHelper.USER_TOKENS_IDENTIFIER)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean isContained = false;
                                        for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                                            if (tokenSnapshot.getKey().equals(mToken)) {
                                                isContained = true;
                                                break;
                                            }
                                        }
                                        if (!isContained) {
                                            mCurrentUserDatabaseReference
                                                    .child(ApplicationHelper.USER_TOKENS_IDENTIFIER)
                                                    .child(mToken).setValue(true);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setUserDataInNavigationDrawer() {
        mUserNameInNavDrawer.setText(mCurrentUser.getUsername());
        String birthday = mCurrentUser.getBirthday();
        if (birthday != null
                && !birthday.equals(getResources().getString(R.string.settings_birthday_default))) {
            mUserBirthdayInNavDrawer.setVisibility(View.VISIBLE);
            mUserBirthdayInNavDrawer.setText(birthday);
        } else {
            mUserBirthdayInNavDrawer.setVisibility(View.GONE);
        }
        String gender = mCurrentUser.getGender();
        if (gender != null && !gender.equals(getResources().getString(R.string.gender_none))) {
            mUserGenderInNavDrawer.setVisibility(View.VISIBLE);
            mUserGenderInNavDrawer.setText(gender);
        } else {
            mUserGenderInNavDrawer.setVisibility(View.GONE);
        }
        if (mCurrentUser.getIsEmailVerified()) {
            mUserEmailVerificationInNavDrawer.setVisibility(View.GONE);
        } else {
            mUserEmailVerificationInNavDrawer.setVisibility(View.VISIBLE);
        }
    }

    private void setToolbarTitle(String username) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getResources().getString(R.string.welcome_start));
        stringBuilder.append(" ");
        stringBuilder.append(username.split(" ")[0]);
        stringBuilder.append("!");
        mToolbarTitleProgressBar.setVisibility(View.GONE);
        mToolbarTitleTextView.setText(stringBuilder.toString());
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_received_items) {
            Toast.makeText(this, "received items", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_profile_settings) {
            Intent settingsIntent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            settingsIntent.putExtra(ApplicationHelper.USER_KEY, mCurrentUser);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_about) {
            Toast.makeText(this, "about", Toast.LENGTH_LONG).show();
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        item.setChecked(false);
        item.setCheckable(false);
        return true;
    }
}
