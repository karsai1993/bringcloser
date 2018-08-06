package karsai.laszlo.bringcloser.activity;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.AddTokenUtils;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity to show the connection status of a user to the other users
 */
public class MainActivity extends CommonActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.tab_layout_connection_types) TabLayout mTabLayout;
    @BindView(R.id.viewpager_connection_types) ViewPager mViewPager;
    @BindView(R.id.ll_connections) LinearLayout mContentLinearLayout;
    @BindView(R.id.tv_no_internet) TextView mNoInternetAlertTextView;
    @BindView(R.id.pb_connections) ProgressBar mContentProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mCurrentUserDatabaseReference;
    private ValueEventListener mUserDatabaseReferenceEventListener;
    private User mCurrentUser;
    private TextView mUserNameInNavDrawer;
    private TextView mUserBirthdayInNavDrawer;
    private RelativeLayout mUserBirthdayLayoutInNavDrawer;
    private TextView mUserGenderInNavDrawer;
    private RelativeLayout mUserGenderLayoutInNavDrawer;
    private TextView mUserEmailVerificationInNavDrawer;
    private ImageView mUserPhotoInNavDrawer;
    private FirebaseUser mFirebaseUser;
    private TextView mToolbarTitleTextView;
    private ProgressBar mToolbarTitleProgressBar;
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        onSignedInInitialize();
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setPageBasedOnIntent(intent);
    }

    private void onSignedInInitialize() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.USERS_NODE);
        mCurrentUserUid = mFirebaseUser.getUid();
        mCurrentUser = new User(
                mFirebaseUser.isEmailVerified(),
                mFirebaseUser.getDisplayName(),
                mFirebaseUser.getPhotoUrl() != null ? mFirebaseUser.getPhotoUrl().toString() : null,
                getResources().getString(R.string.settings_birthday_default_id),
                getResources().getString(R.string.gender_none_id),
                null,
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
        mUserBirthdayLayoutInNavDrawer = navigationHeaderView.findViewById(R.id.rl_user_birthday);
        mUserGenderInNavDrawer = navigationHeaderView.findViewById(R.id.tv_user_gender);
        mUserGenderLayoutInNavDrawer = navigationHeaderView.findViewById(R.id.rl_user_gender);
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
                switch (action) {
                    case ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_REQUEST:
                        mViewPager.setCurrentItem(2);
                        break;
                    case ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION:
                        mViewPager.setCurrentItem(0);
                        break;
                    case ApplicationUtils.NEW_SENT_REQUEST_INTENT_ACTION_PAGE_CONNECTION:
                        mViewPager.setCurrentItem(1);
                        break;
                    default:
                        break;
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
                    if (mCurrentUser == null) {
                        Timber.wtf("user node, datasnapshot exists but current user returned null");
                        return;
                    }
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
                    if (mCurrentUser == null) {
                        Timber.wtf("user node, datasnapshot exists but current user returned null");
                        return;
                    }
                    if (mFirebaseUser.isEmailVerified()){
                        if (!mCurrentUser.getIsEmailVerified()) {
                            mCurrentUser.setIsEmailVerified(true);
                            Map<String, Object> updateVerificationValue = new HashMap<>();
                            updateVerificationValue.put(
                                    "/" + ApplicationUtils.USER_EMAIL_VERIFICATION_IDENTIFIER,
                                    true
                            );
                            mCurrentUserDatabaseReference.updateChildren(updateVerificationValue);
                        }
                    }
                }
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(
                        MainActivity.this,  new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        AddTokenUtils.addTokenIfNeeded(newToken, mCurrentUserUid);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setUserDataInNavigationDrawer() {
        mUserNameInNavDrawer.setText(mCurrentUser.getUsername());
        String birthday = mCurrentUser.getBirthday();
        if (birthday != null
                && !birthday.equals(getResources().getString(R.string.settings_birthday_default_id))) {
            mUserBirthdayLayoutInNavDrawer.setVisibility(View.VISIBLE);
            String [] birthdayParts = birthday.split("-");
            birthday = birthdayParts[0] + birthdayParts[1] + birthdayParts[2] + "000000";
            birthday = ApplicationUtils.getLocalDateAndTimeToDisplay(this, birthday);
            birthdayParts = birthday.split(" ");
            birthday = birthdayParts[0] + " " + birthdayParts[1] + " " + birthdayParts[2];
            mUserBirthdayInNavDrawer.setText(birthday);
        } else {
            mUserBirthdayLayoutInNavDrawer.setVisibility(View.GONE);
        }
        String gender = mCurrentUser.getGender();
        if (gender != null && !gender.equals(getResources().getString(R.string.gender_none_id))) {
            if (gender.equals(getResources().getString(R.string.gender_male_id))) {
                gender = getResources().getString(R.string.gender_male);
            } else if (gender.equals(getResources().getString(R.string.gender_female_id))) {
                gender = getResources().getString(R.string.gender_female);
            }
            mUserGenderLayoutInNavDrawer.setVisibility(View.VISIBLE);
            mUserGenderInNavDrawer.setText(gender);
        } else {
            mUserGenderLayoutInNavDrawer.setVisibility(View.GONE);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_received_items) {
            startActivity(new Intent(MainActivity.this, ReceivedDetailsActivity.class));
        } else if (id == R.id.nav_profile_settings) {
            Intent settingsIntent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            settingsIntent.putExtra(ApplicationUtils.USER_KEY, mCurrentUser);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_terms_of_use) {
            ApplicationUtils.openWebPage(
                    this,
                    getResources().getString(R.string.terms_of_use)
            );
        } else if (id == R.id.nav_privacy_policy) {
            ApplicationUtils.openWebPage(
                    this,
                    getResources().getString(R.string.privacy_policy)
            );
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        item.setChecked(false);
        item.setCheckable(false);
        return true;
    }
}
