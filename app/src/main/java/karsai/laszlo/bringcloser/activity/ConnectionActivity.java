package karsai.laszlo.bringcloser.activity;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;

public class ConnectionActivity extends AppCompatActivity {

    private static final int COLLAPSING_THRESHOLD = -360;

    @BindView(R.id.connection_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.connection_collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.connection_app_bar_layout)
    AppBarLayout mAppBarLayout;

    private TextView mToolbarTitleTextView;
    private String mCurrentUserUid;
    private String mOtherUserUid;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDetailsDatabaseReference;
    private boolean mIsCurrentTheFirst;

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

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
        return false;
    }
}
