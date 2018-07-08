package karsai.laszlo.bringcloser.ui.screens.addchosenconnection;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.ui.screens.main.MainActivity;

/**
 * Created by Laci on 07/06/2018.
 */

public class AddChosenConnectionActivity extends AppCompatActivity {

    @BindView(R.id.iv_chosen_photo)
    ImageView mChosenPhotoImageView;
    @BindView(R.id.tv_chosen_name)
    TextView mChosenNameTextView;
    @BindView(R.id.tv_chosen_question)
    TextView mChosenQuestionTextView;
    @BindView(R.id.spinner_chosen_relationship)
    Spinner mChosenRelationshipSpinner;
    @BindView(R.id.fab_approve_add_new_connection)
    FloatingActionButton mApproveAddNewConnectionFab;

    private String mCurrentUserUid;
    private String mChosenUserUid;
    private User mChosenUser;
    private User mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsDatabaseReference;
    private Query mConnectionsDatabaseQuery;
    private ValueEventListener mConnectionsDatabaseValueEventListener;
    private DatabaseReference mCurrentUserDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chosen_connection);
        ButterKnife.bind(this);

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        Intent receivedIntent = getIntent();
        mChosenUser = receivedIntent.getParcelableExtra(ApplicationHelper.INTENT_CHOSEN_USER_KEY);
        mChosenUserUid = mChosenUser.getUid();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_icons8_load_96_1);
        requestOptions.error(R.drawable.baseline_error_outline_black_48);
        requestOptions.fitCenter();
        requestOptions.circleCrop();

        String chosenUserName = mChosenUser.getUsername();
        String photoUrl = mChosenUser.getPhotoUrl();
        RequestBuilder<Drawable> requestBuilder;
        if (photoUrl != null && !photoUrl.isEmpty())
            requestBuilder = Glide.with(this).load(photoUrl);
        else requestBuilder = Glide.with(this).load(R.drawable.baseline_face_black_48);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) postponeEnterTransition();
        else supportPostponeEnterTransition();
        requestBuilder
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startPostponedEnterTransition();
                        else supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(mChosenPhotoImageView);

        mChosenNameTextView.setText(chosenUserName);
        mChosenQuestionTextView.setText(
                new StringBuilder()
                        .append(getResources().getString(
                                R.string.add_connection_chosen_question_part1)
                        ).append(" ")
                        .append(chosenUserName.split(" ")[0])
                        .append(" ")
                        .append(getResources().getString(
                                R.string.add_connection_chosen_question_part2)
                        ).toString()
        );
        ArrayAdapter<String> relationshipListAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                android.R.layout.simple_spinner_item,
                ApplicationHelper.getRelationshipOptionsList(getApplicationContext())
        );
        mChosenRelationshipSpinner.setAdapter(relationshipListAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentUserDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
        mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.CONNECTIONS_NODE);

        mConnectionsDatabaseQuery = mConnectionsDatabaseReference.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(mChosenUserUid);

        mConnectionsDatabaseValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class).equals(mCurrentUserUid)) {
                                mApproveAddNewConnectionFab.setVisibility(View.GONE);
                                Snackbar.make(
                                        findViewById(R.id.fab_approve_add_new_connection),
                                        new StringBuilder()
                                                .append(getResources().getString(R.string.already_got_request))
                                                .append(" ")
                                                .append(mChosenUser.getUsername())
                                                .append("!").toString(),
                                        Snackbar.LENGTH_INDEFINITE).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

        mApproveAddNewConnectionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String selectedRelationshipType
                        = (String) mChosenRelationshipSpinner.getSelectedItem();
                if (selectedRelationshipType
                        .equals(getResources().getString(R.string.relationship_type_none))) {
                    Snackbar.make(
                            view,
                            getResources().getString(R.string.relationship_type_not_selected),
                            Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    mCurrentUserDatabaseReference.child(mCurrentUserUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mCurrentUser = dataSnapshot.getValue(User.class);
                                    Connection connection = new Connection(
                                            mCurrentUser.getUid(),
                                            mChosenUser.getUid(),
                                            ApplicationHelper.CONNECTION_BIT_NEG,
                                            selectedRelationshipType,
                                            ApplicationHelper.getCurrentUTCDateAndTime(
                                                    ApplicationHelper.DATE_PATTERN_FULL)
                                    );
                                    mConnectionsDatabaseReference.push().setValue(connection);
                                    Intent mainIntent = new Intent(
                                            AddChosenConnectionActivity.this,
                                            MainActivity.class
                                    );
                                    mainIntent.setAction(
                                            ApplicationHelper
                                                    .NEW_SENT_REQUEST_INTENT_ACTION_PAGE_CONNECTION
                                    );
                                    startActivity(mainIntent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectionsDatabaseQuery.addValueEventListener(mConnectionsDatabaseValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnectionsDatabaseValueEventListener != null) {
            mConnectionsDatabaseQuery.removeEventListener(mConnectionsDatabaseValueEventListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition();
        else supportFinishAfterTransition();
        return true;
    }
}
