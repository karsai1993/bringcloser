package karsai.laszlo.bringcloser.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ConnectionDetailFragmentPagerAdapter;

/*
Circular animation is from:
https://android.jlelse.eu/a-little-thing-that-matter-how-to-reveal-an-activity-with-circular-revelation-d94f9bfcae28
 */
public class ConnectionActivity extends AppCompatActivity {

    private static final int COLLAPSING_THRESHOLD = -360;

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

    private TextView mToolbarTitleTextView;
    private String mCurrentUserUid;
    private String mOtherUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDetailsDatabaseReference;
    private boolean mIsCurrentTheFirst;
    private int mPosX;
    private int mPosY;
    private ConnectionDetailFragmentPagerAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });
        mToolbarTitleTextView = mToolbar.findViewById(R.id.tv_connection_toolbar_title);
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
                if (verticalOffset < COLLAPSING_THRESHOLD) {
                    mToolbarTitleTextView.setVisibility(View.VISIBLE);
                } else {
                    mToolbarTitleTextView.setVisibility(View.GONE);
                }
            }
        });

        /*Intent receivedIntent = getIntent();
        mDetail = receivedIntent.getParcelableExtra(ApplicationHelper.CONNECTION_DETAIL_KEY);
        mCurrentUserUid = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        mIsCurrentTheFirst = mDetail.getFirstUid().equals(mCurrentUserUid);
        if (mIsCurrentTheFirst) mOtherUserUid = mDetail.getSecondUid();
        else mOtherUserUid = mDetail.getFirstUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDetailsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.DETAILS_NODE);
        mDetailsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        /*Intent receivedData = getIntent();
        if (savedInstanceState == null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && receivedData != null) {
            mCoordinatorLayout.setVisibility(View.INVISIBLE);
            mPosX = receivedData.getIntExtra(ApplicationHelper.EXTRA_X_COORD, 0);
            mPosY = receivedData.getIntExtra(ApplicationHelper.EXTRA_Y_COORD, 0);
            ViewTreeObserver viewTreeObserver = mCoordinatorLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(mPosX, mPosY);
                        mCoordinatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            mCoordinatorLayout.setVisibility(View.VISIBLE);
        }*/

        mPageAdapter = new ConnectionDetailFragmentPagerAdapter(
                getSupportFragmentManager(),
                this
        );
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mToolbarTitleTextView.setText(
                new StringBuilder()
                        .append(
                                getResources()
                                        .getString(R.string.connection_activity_toolbar_title)
                        ).append(" ")
                        .append("Laci")
                        .toString()
        );
    }

    /*protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(mCoordinatorLayout.getWidth(), mCoordinatorLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mCoordinatorLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            mCoordinatorLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(mCoordinatorLayout.getWidth(), mCoordinatorLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    mCoordinatorLayout, mPosX, mPosY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCoordinatorLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });

            circularReveal.start();
        }
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        /*unRevealActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            startPostponedEnterTransition();
        else supportStartPostponedEnterTransition();
        */startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
        return true;
    }
}
