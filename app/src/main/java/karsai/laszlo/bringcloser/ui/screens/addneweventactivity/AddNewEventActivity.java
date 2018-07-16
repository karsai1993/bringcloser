package karsai.laszlo.bringcloser.ui.screens.addneweventactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.fragment.DatePickerFragment;
import karsai.laszlo.bringcloser.fragment.TimePickerFragment;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.service.UpdateStateService;
import karsai.laszlo.bringcloser.ui.screens.addnewwishactivity.AddNewWishActivity;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class AddNewEventActivity extends AppCompatActivity {

    @BindView(R.id.et_event_title)
    EditText mTitleEditText;
    @BindView(R.id.et_event_place)
    EditText mPlaceEditText;
    @BindView(R.id.et_event_message)
    EditText mMessageEditText;
    @BindView(R.id.tv_event_question)
    TextView mQuestionTextView;
    @BindView(R.id.tv_event_selected_date)
    TextView mSelectedDateTextView;
    @BindView(R.id.tv_event_selected_time)
    TextView mSelectedTimeTextView;
    @BindView(R.id.tv_event_date_selector)
    TextView mDateSelectorTextView;
    @BindView(R.id.tv_event_time_selector)
    TextView mTimeSelectorTextView;
    @BindView(R.id.iv_event_add_photo_from_camera)
    ImageView mCameraPhotoImageView;
    @BindView(R.id.iv_event_add_photo_from_gallery)
    ImageView mGalleryPhotoImageView;
    @BindView(R.id.tv_event_photo_alert)
    TextView mNoPhotoAlertTextView;
    @BindView(R.id.iv_event_added_extra_photo)
    ImageView mAddedExtraPhotoImageView;
    @BindView(R.id.iv_event_added_extra_photo_delete)
    ImageView mDeleteExtraPhotoImageView;
    @BindView(R.id.ll_event_add_extra_photo)
    LinearLayout mAddPhotoLinearLayout;
    @BindView(R.id.fab_approve_add_new_event)
    FloatingActionButton mApproveFab;

    private ConnectionDetail mConnectionDetail;
    private String mOtherUid;
    private String mType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseRef;
    private ValueEventListener mUserValueEventListener;
    private View mSnackbarView;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mImagesRootRef;
    private StorageReference mImagesRef;
    private DatabaseReference mConnectionsDatabaseReference;
    private Query mConnectionsQuery;
    private DatabaseReference mEventsDatabaseRef;
    private String mCurrentUserUid;
    private Bundle mPausedData;
    private Bundle mSavedInstanceState;
    private String mChosenPhotoUrl;

    private final static String SAVE_DATE = "date";
    private final static String SAVE_TIME = "time";
    private final static String SAVE_PHOTO_URL = "photo_url";
    private final static String SAVE_EXTRA_VISIBILITY = "extra_visibility";
    private final static String SAVE_TITLE_EDIT_TEXT_FOCUSED = "title_edit_text_focused";
    private final static String SAVE_TITLE_EDIT_TEXT_CONTENT = "title_edit_text_content";
    private final static String SAVE_PLACE_EDIT_TEXT_FOCUSED = "place_edit_text_focused";
    private final static String SAVE_PLACE_EDIT_TEXT_CONTENT = "place_edit_text_content";
    private final static String SAVE_MESSAGE_EDIT_TEXT_FOCUSED = "message_edit_text_focused";
    private final static String SAVE_MESSAGE_EDIT_TEXT_CONTENT = "message_edit_text_content";
    private final static String SAVE_IMAGE_REF = "image_ref";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);
        ButterKnife.bind(this);

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        Intent receivedData = getIntent();
        if (receivedData != null) {
            mConnectionDetail = receivedData.getParcelableExtra(ApplicationHelper.EXTRA_DATA);
            if (mConnectionDetail.getFromUid().equals(mCurrentUserUid)) {
                mOtherUid = mConnectionDetail.getToUid();
            } else {
                mOtherUid = mConnectionDetail.getFromUid();
            }

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mUserDatabaseRef = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.USERS_NODE)
                    .child(mOtherUid);
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    loadData(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mDateSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationHelper.EXTRA_ID, R.id.tv_event_selected_date);
                    datePickerFragment.setArguments(bundle);
                    datePickerFragment.show(getSupportFragmentManager(), ApplicationHelper.TAG_DATA_PICKER);
                }
            });
            mTimeSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment timePickerFragment = new TimePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationHelper.EXTRA_ID, R.id.tv_event_selected_time);
                    timePickerFragment.setArguments(bundle);
                    timePickerFragment.show(getSupportFragmentManager(), ApplicationHelper.TAG_TIME_PICKER);
                }
            });

            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            mFirebaseStorage = FirebaseStorage.getInstance();
            mImagesRootRef = mFirebaseStorage.getReference()
                    .child(mFirebaseUser.getUid())
                    .child(ApplicationHelper.STORAGE_EVENT_IMAGES_FOLDER);
            mImagesRef = mImagesRootRef;
            mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.CONNECTIONS_NODE);

            mGalleryPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromFile(AddNewEventActivity.this);
                }
            });
            mCameraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromCamera(AddNewEventActivity.this);
                }
            });
            mDeleteExtraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String photoUrl = uri.toString();
                            ApplicationHelper.deleteImageFromStorage(
                                    AddNewEventActivity.this,
                                    photoUrl
                            );
                            mAddPhotoLinearLayout.setVisibility(View.GONE);
                            mNoPhotoAlertTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            mApproveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTitleEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.event_alert_no_title),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mPlaceEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.event_alert_no_place),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mMessageEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.event_alert_no_message),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mSelectedDateTextView.getText().toString()
                            .equals(getString(R.string.selected_date_default))) {
                        Snackbar.make(
                                view,
                                getString(R.string.alert_no_date),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mSelectedTimeTextView.getText().toString()
                            .equals(getString(R.string.selected_time_default))) {
                        Snackbar.make(
                                view,
                                getString(R.string.alert_no_time),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else {
                        if (mAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
                            mImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String photoUrl = uri.toString();
                                    createEventItem(photoUrl);
                                }
                            });
                        } else {
                            createEventItem(null);
                        }
                    }
                }
            });
        }
    }

    private void deletePhotoIfExists() {
        if (mAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
            mImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String photoUrl = uri.toString();
                    ApplicationHelper.deleteImageFromStorage(
                            getApplicationContext(),
                            photoUrl
                    );
                    Toast.makeText(
                            getApplicationContext(),
                            getApplicationContext()
                                    .getResources().getString(R.string.up_nav_deletion_alert),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        deletePhotoIfExists();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        deletePhotoIfExists();
        super.onBackPressed();
    }

    private void createEventItem(final String photoUrl) {
        String dateComposition = new StringBuilder()
                .append(mSelectedDateTextView.getText().toString())
                .append(ApplicationHelper.DATE_SPLITTER)
                .append(mSelectedTimeTextView.getText().toString())
                .toString();
        SimpleDateFormat sdf = new SimpleDateFormat(
                ApplicationHelper.DATE_PATTERN_COMPOSITION,
                Locale.getDefault()
        );
        SimpleDateFormat displaySimpleDateFormat = new SimpleDateFormat(
                ApplicationHelper.DATE_PATTERN_FULL,
                Locale.getDefault()
        );
        String whenToArrive = "";
        try {
            Date date = sdf.parse(dateComposition);
            whenToArrive = displaySimpleDateFormat.format(date);
        } catch (ParseException e) {
            whenToArrive = "";
        } finally {
            if (!whenToArrive.isEmpty()) {
                mConnectionsQuery = mConnectionsDatabaseReference
                        .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                        .equalTo(mConnectionDetail.getFromUid());
                final String finalWhenToArrive = whenToArrive;
                mConnectionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (dataSnapshot
                                    .child(key)
                                    .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class)
                                    .equals(mConnectionDetail.getToUid())) {
                                mEventsDatabaseRef = mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationHelper.EVENTS_NODE);
                                DatabaseReference databaseReference
                                        = mEventsDatabaseRef.push();
                                final Event event = new Event(
                                        mCurrentUserUid,
                                        mConnectionDetail.getFromUid(),
                                        mConnectionDetail.getToUid(),
                                        photoUrl,
                                        finalWhenToArrive,
                                        mTitleEditText.getText().toString(),
                                        mPlaceEditText.getText().toString(),
                                        mMessageEditText.getText().toString(),
                                        false,
                                        databaseReference.getKey()
                                );
                                databaseReference.setValue(event).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startUpdateService(event);
                                                finish();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void startUpdateService(Event event) {
        FirebaseJobDispatcher dispatcher
                = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        Bundle extraBundle = new Bundle();
        extraBundle.putString(
                ApplicationHelper.SERVICE_TYPE_IDENTIFIER,
                ApplicationHelper.SERVICE_TYPE_EVENT
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_FROM_IDENTIFIER,
                event.getConnectionFromUid()
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_TO_IDENTIFIER,
                event.getConnectionToUid()
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_KEY_IDENTIFIER,
                event.getKey()
        );
        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(UpdateStateService.class)
                // uniquely identifies the job
                .setTag(ApplicationHelper.getServiceUniqueTag(
                        event.getConnectionFromUid(),
                        event.getConnectionToUid(),
                        event.getKey())
                )
                // one-off job
                //.setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                .setTrigger(getTriggerTime(event))
                // don't overwrite an existing job with the same tag
                //.setReplaceCurrent(false)
                // retry with exponential backoff
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extraBundle)
                .build();
        dispatcher.mustSchedule(myJob);
    }

    private JobTrigger getTriggerTime(Event event) {
        String targetTime = event.getWhenToArrive() + ":00";
        SimpleDateFormat sdf = new SimpleDateFormat(
                ApplicationHelper.DATE_PATTERN_FOR_SERVICE,
                Locale.getDefault()
        );
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            Date targetDate = sdf.parse(targetTime);
            Date currentDate = new Date();
            long diffInSecondsAsLong = (targetDate.getTime() - currentDate.getTime()) / 1000;
            int diffInSecondsAsInt = (int) diffInSecondsAsLong;
            return Trigger.executionWindow(diffInSecondsAsInt, diffInSecondsAsInt);
        } catch (ParseException e) {
            return Trigger.NOW;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final StorageReference tempRef = mImagesRef;
        mImagesRef = mImagesRootRef.child(
                ApplicationHelper.getCurrentUTCDateAndTime(
                        ApplicationHelper.DATE_PATTERN_FULL_STORAGE
                )
        );
        OnSuccessListener<UploadTask.TaskSnapshot> uploadOnSuccessListener
                = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        final String photoUrl = uri.toString();
                        if (mAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
                            tempRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri prevUri) {
                                    String prevPhotoUrl = prevUri.toString();
                                    ApplicationHelper.deleteImageFromStorage(
                                            AddNewEventActivity.this,
                                            prevPhotoUrl
                                    );
                                    mNoPhotoAlertTextView.setVisibility(View.GONE);
                                    mAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                                    ImageUtils.setPhoto(
                                            AddNewEventActivity.this,
                                            photoUrl,
                                            mAddedExtraPhotoImageView,
                                            false
                                    );
                                    mChosenPhotoUrl = photoUrl;
                                }
                            });
                        } else {
                            mNoPhotoAlertTextView.setVisibility(View.GONE);
                            mAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                            ImageUtils.setPhoto(
                                    AddNewEventActivity.this,
                                    photoUrl,
                                    mAddedExtraPhotoImageView,
                                    false
                            );
                            mChosenPhotoUrl = photoUrl;
                        }
                    }
                });
            }
        };
        ImageUtils.onActivityResult(
                AddNewEventActivity.this,
                mSnackbarView,
                requestCode,
                resultCode,
                data,
                mImagesRef,
                uploadOnSuccessListener
        );
    }

    private void loadData(User user) {
        final String name = user.getUsername().split(" ")[0];
        mQuestionTextView.setText(
                new StringBuilder()
                        .append(getString(R.string.event_question_1))
                        .append(name)
                        .append(getString(R.string.event_question_2))
                        .toString()
        );
        applyPauseOrRotationHandling(mPausedData);
        applyPauseOrRotationHandling(mSavedInstanceState);
        mPausedData = null;
        mSavedInstanceState = null;
    }

    private void applyPauseOrRotationHandling(Bundle bundle) {
        if (bundle != null) {
            String title = bundle.getString(SAVE_TITLE_EDIT_TEXT_CONTENT);
            mTitleEditText.setText(title);
            if (bundle.getBoolean(SAVE_TITLE_EDIT_TEXT_FOCUSED)) {
                mTitleEditText.requestFocus();
                mTitleEditText.setSelection(title.length());
            }
            String place = bundle.getString(SAVE_PLACE_EDIT_TEXT_CONTENT);
            mPlaceEditText.setText(place);
            if (bundle.getBoolean(SAVE_PLACE_EDIT_TEXT_FOCUSED)) {
                mPlaceEditText.requestFocus();
                mPlaceEditText.setSelection(place.length());
            }
            String message = bundle.getString(SAVE_MESSAGE_EDIT_TEXT_CONTENT);
            mMessageEditText.setText(message);
            if (bundle.getBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED)) {
                mMessageEditText.requestFocus();
                mMessageEditText.setSelection(message.length());
            }
            String date = bundle.getString(SAVE_DATE);
            mSelectedDateTextView.setText(date);
            String time = bundle.getString(SAVE_TIME);
            mSelectedTimeTextView.setText(time);
            boolean isExtraPhotoVisible = bundle.getBoolean(SAVE_EXTRA_VISIBILITY);
            if (isExtraPhotoVisible) {
                mNoPhotoAlertTextView.setVisibility(View.GONE);
                mAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                mChosenPhotoUrl = bundle.getString(SAVE_PHOTO_URL);
                String imageRef = bundle.getString(SAVE_IMAGE_REF);
                mImagesRef = mFirebaseStorage.getReference(imageRef);
                ImageUtils.setPhoto(
                        AddNewEventActivity.this,
                        mChosenPhotoUrl,
                        mAddedExtraPhotoImageView,
                        false
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserDatabaseRef.addValueEventListener(mUserValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPausedData = new Bundle();
        mPausedData.putString(SAVE_TITLE_EDIT_TEXT_CONTENT, mTitleEditText.getText().toString());
        mPausedData.putBoolean(SAVE_TITLE_EDIT_TEXT_FOCUSED, mTitleEditText.isFocused());
        mPausedData.putString(SAVE_PLACE_EDIT_TEXT_CONTENT, mPlaceEditText.getText().toString());
        mPausedData.putBoolean(SAVE_PLACE_EDIT_TEXT_FOCUSED, mPlaceEditText.isFocused());
        mPausedData.putString(SAVE_MESSAGE_EDIT_TEXT_CONTENT, mMessageEditText.getText().toString());
        mPausedData.putBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED, mMessageEditText.isFocused());
        mPausedData.putString(SAVE_DATE, mSelectedDateTextView.getText().toString());
        mPausedData.putString(SAVE_TIME, mSelectedTimeTextView.getText().toString());
        boolean isExtraPhotoVisible = mAddPhotoLinearLayout.getVisibility() == View.VISIBLE;
        mPausedData.putBoolean(SAVE_EXTRA_VISIBILITY, isExtraPhotoVisible);
        if (isExtraPhotoVisible) {
            mPausedData.putString(SAVE_PHOTO_URL, mChosenPhotoUrl);
        }
        mPausedData.putString(SAVE_IMAGE_REF, mImagesRef.getPath());
        if (mUserValueEventListener != null) {
            mUserDatabaseRef.removeEventListener(mUserValueEventListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_TITLE_EDIT_TEXT_CONTENT, mTitleEditText.getText().toString());
        outState.putBoolean(SAVE_TITLE_EDIT_TEXT_FOCUSED, mTitleEditText.isFocused());
        outState.putString(SAVE_PLACE_EDIT_TEXT_CONTENT, mPlaceEditText.getText().toString());
        outState.putBoolean(SAVE_PLACE_EDIT_TEXT_FOCUSED, mPlaceEditText.isFocused());
        outState.putString(SAVE_MESSAGE_EDIT_TEXT_CONTENT, mMessageEditText.getText().toString());
        outState.putBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED, mMessageEditText.isFocused());
        outState.putString(SAVE_DATE, mSelectedDateTextView.getText().toString());
        outState.putString(SAVE_TIME, mSelectedTimeTextView.getText().toString());
        boolean isExtraPhotoVisible = mAddPhotoLinearLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(SAVE_EXTRA_VISIBILITY, isExtraPhotoVisible);
        if (isExtraPhotoVisible) {
            outState.putString(SAVE_PHOTO_URL, mChosenPhotoUrl);
        }
        outState.putString(SAVE_IMAGE_REF, mImagesRef.getPath());
        super.onSaveInstanceState(outState);
    }
}
