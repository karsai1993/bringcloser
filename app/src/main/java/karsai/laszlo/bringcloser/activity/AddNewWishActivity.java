package karsai.laszlo.bringcloser.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.fragment.DatePickerFragment;
import karsai.laszlo.bringcloser.fragment.TimePickerFragment;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.service.UpdateStateService;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity to perform adding a new wish
 */
public class AddNewWishActivity extends CommonActivity {

    @BindView(R.id.spinner_wish)
    Spinner mSpinner;
    @BindView(R.id.et_wish)
    TextInputEditText mWishEditText;
    @BindView(R.id.et_wish_other)
    TextInputEditText mWishOtherEditText;
    @BindView(R.id.til_wish_other)
    TextInputLayout mWishOtherTextInputLayout;
    @BindView(R.id.tv_wish_question)
    TextView mWishQuestionTextView;
    @BindView(R.id.tv_wish_selected_date)
    TextView mWishSelectedDateTextView;
    @BindView(R.id.tv_wish_selected_time)
    TextView mWishSelectedTimeTextView;
    @BindView(R.id.tv_wish_date_selector)
    TextView mWishDateSelectorTextView;
    @BindView(R.id.tv_wish_time_selector)
    TextView mWishTimeSelectorTextView;
    @BindView(R.id.iv_wish_add_photo_from_camera)
    ImageView mWishCameraPhotoImageView;
    @BindView(R.id.iv_wish_add_photo_from_gallery)
    ImageView mWishGalleryPhotoImageView;
    @BindView(R.id.tv_wish_photo_alert)
    TextView mWishNoPhotoAlertTextView;
    @BindView(R.id.iv_wish_added_extra_photo)
    ImageView mWishAddedExtraPhotoImageView;
    @BindView(R.id.iv_wish_added_extra_photo_delete)
    ImageView mWishDeleteExtraPhotoImageView;
    @BindView(R.id.ll_wish_add_extra_photo)
    LinearLayout mWishAddPhotoLinearLayout;
    @BindView(R.id.fab_approve_add_new_wish)
    FloatingActionButton mApproveFab;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;

    private ConnectionDetail mConnectionDetail;
    private String mOtherUid;
    private String mType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseRef;
    private ValueEventListener mUserValueEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mWishImagesRootRef;
    private StorageReference mWishImagesRef;
    private DatabaseReference mConnectionsDatabaseReference;
    private Query mConnectionsQuery;
    private DatabaseReference mWishesDatabaseRef;
    private String mCurrentUserUid;
    private Bundle mPausedData;
    private Bundle mSavedInstanceState;
    private String mChosenPhotoUrl;
    private boolean mGenderShouldBeReversed;
    private ProgressDialog mDialog;

    private final static String SAVE_CHOSEN_VALUE = "chosen_val";
    private final static String SAVE_DATE = "date";
    private final static String SAVE_TIME = "time";
    private final static String SAVE_CONTENT = "content";
    private final static String SAVE_PHOTO_URL = "photo_url";
    private final static String SAVE_EXTRA_VISIBILITY = "extra_visibility";
    private final static String SAVE_OTHER_EDIT_TEXT_VISIBILITY = "other_edit_text_visibility";
    private final static String SAVE_OTHER_EDIT_TEXT_CONTENT = "other_edit_text_content";
    private final static String SAVE_EDIT_TEXT_FOCUSED = "edit_text_focused";
    private final static String SAVE_OTHER_EDIT_TEXT_FOCUSED = "other_edit_text_focused";
    private final static String SAVE_IMAGE_REF = "image_ref";
    private final static String TAG = AddNewWishActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_add_new_wish);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mCurrentUserUid = FirebaseAuth.getInstance().getUid();
        Intent receivedData = getIntent();
        if (receivedData != null) {
            mConnectionDetail = receivedData.getParcelableExtra(ApplicationUtils.EXTRA_DATA);
            if (mConnectionDetail.getFromUid().equals(mCurrentUserUid)) {
                mOtherUid = mConnectionDetail.getToUid();
            } else {
                mOtherUid = mConnectionDetail.getFromUid();
                mGenderShouldBeReversed = true;
            }
            mType = receivedData.getStringExtra(ApplicationUtils.EXTRA_TYPE);

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mUserDatabaseRef = mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.USERS_NODE)
                    .child(mOtherUid);
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        loadData(user);
                    } else {
                        Timber.wtf("user null cannot load data for adding new wish");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mWishDateSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationUtils.EXTRA_ID, R.id.tv_wish_selected_date);
                    datePickerFragment.setArguments(bundle);
                    datePickerFragment.show(getSupportFragmentManager(), ApplicationUtils.TAG_DATA_PICKER);
                }
            });
            mWishTimeSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment timePickerFragment = new TimePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationUtils.EXTRA_ID, R.id.tv_wish_selected_time);
                    timePickerFragment.setArguments(bundle);
                    timePickerFragment.show(getSupportFragmentManager(), ApplicationUtils.TAG_TIME_PICKER);
                }
            });
            mFirebaseStorage = FirebaseStorage.getInstance();
            mWishImagesRootRef = mFirebaseStorage.getReference()
                    .child(mCurrentUserUid)
                    .child(ApplicationUtils.STORAGE_WISH_IMAGES_FOLDER);
            mWishImagesRef = mWishImagesRootRef;
            mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.CONNECTIONS_NODE);
            mWishGalleryPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageUtils.onClickFromFile(AddNewWishActivity.this);
                }
            });
            mWishCameraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageUtils.onClickFromCamera(AddNewWishActivity.this);
                }
            });
            mWishDeleteExtraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String photoUrl = uri.toString();
                            ApplicationUtils.deleteImageFromStorage(
                                    AddNewWishActivity.this,
                                    photoUrl,
                                    null,
                                    null
                            );
                            mWishAddPhotoLinearLayout.setVisibility(View.GONE);
                            mWishNoPhotoAlertTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            mDialog = new ProgressDialog(this);
            mApproveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.setMessage(getResources().getString(R.string.memory_creating));
                    mDialog.show();
                    if (mSpinner.getSelectedItem().equals(getString(R.string.wish_default_occasion))) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_occasion),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mSpinner.getSelectedItem()
                            .equals(getString(R.string.wish_other_occasion))
                            && mWishOtherEditText.getText().toString().isEmpty()) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_occasion_specified),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishEditText.getText().toString().isEmpty()) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_text),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishSelectedDateTextView.getText().toString()
                            .equals(getString(R.string.selected_date_default))) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Snackbar.make(
                                view,
                                getString(R.string.alert_no_date),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishSelectedTimeTextView.getText().toString()
                            .equals(getString(R.string.selected_time_default))) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Snackbar.make(
                                view,
                                getString(R.string.alert_no_time),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else {
                        if (mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
                            mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String photoUrl = uri.toString();
                                    createWishItem(photoUrl);
                                }
                            });
                        } else {
                            createWishItem(null);
                        }
                    }
                }
            });
            mSpinner.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        onSupportNavigateUp();
                    } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mWishEditText.requestFocus();
                    }
                    return false;
                }
            });
            mWishEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        mSpinner.requestFocus();
                    } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mWishDateSelectorTextView.requestFocus();
                    }
                    return false;
                }
            });
        } else {
            Timber.wtf("new wish received data null");
        }
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    private void deletePhotoIfExists() {
        if (mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
            mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String photoUrl = uri.toString();
                    ApplicationUtils.deleteImageFromStorage(
                            getApplicationContext(),
                            photoUrl,
                            null,
                            null
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

    private void createWishItem(final String photoUrl) {
        String [] dateParts = mWishSelectedDateTextView.getText().toString().split("-");
        String [] timeParts = mWishSelectedTimeTextView.getText().toString().split(":");
        String dateAndTimeComposition
                = dateParts[0]
                + dateParts[1]
                + dateParts[2]
                + timeParts[0]
                + timeParts[1]
                + "00";
        final String whenToArrive = ApplicationUtils.getUTCDateAndTime(this, dateAndTimeComposition);
        if (!whenToArrive.equals(getResources().getString(R.string.data_not_available))) {
            mConnectionsQuery = mConnectionsDatabaseReference
                    .orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                    .equalTo(mConnectionDetail.getFromUid());
            mConnectionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                        String key = connectionSnapshot.getKey();
                        if (key == null) {
                            Timber.wtf("key null connection from uid not found");
                            continue;
                        }
                        String toUidValue = dataSnapshot
                                .child(key)
                                .child(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                .getValue(String.class);
                        if (toUidValue == null) {
                            Timber.wtf("to uid not found connection to");
                            continue;
                        }
                        if (toUidValue.equals(mConnectionDetail.getToUid())) {
                            mWishesDatabaseRef = mConnectionsDatabaseReference
                                    .child(key)
                                    .child(ApplicationUtils.WISHES_NODE);
                            DatabaseReference databaseReference
                                    = mWishesDatabaseRef.push();
                            String selectedOccasion = (String) mSpinner.getSelectedItem();
                            Resources res = getResources();
                            if (selectedOccasion.equals(res.getString(R.string.wish_birthday_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_birthday_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_name_day_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_name_day_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_christmas_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_christmas_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_new_year_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_new_year_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_easter_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_easter_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_valentine_day_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_valentine_day_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_anniversary_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_anniversary_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_women_day_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_women_day_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_mother_day_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_mother_day_occasion_id);
                            } else if (selectedOccasion.equals(res.getString(R.string.wish_father_day_occasion))) {
                                selectedOccasion = res.getString(R.string.wish_father_day_occasion_id);
                            } else if (selectedOccasion.equals(getString(R.string.wish_other_occasion))) {
                                selectedOccasion = ApplicationUtils.convertTextToEmojiIfNeeded(
                                        getApplicationContext(),
                                        mWishOtherEditText.getText().toString()
                                );
                            }
                            String message = ApplicationUtils.convertTextToEmojiIfNeeded(
                                    getApplicationContext(),
                                    mWishEditText.getText().toString()
                            );
                            final Wish wish = new Wish(
                                    mCurrentUserUid,
                                    mConnectionDetail.getFromUid(),
                                    mConnectionDetail.getToUid(),
                                    photoUrl,
                                    whenToArrive,
                                    selectedOccasion,
                                    message,
                                    false,
                                    databaseReference.getKey()
                            );
                            databaseReference.setValue(wish).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startUpdateService(wish);
                                            if (mDialog != null && mDialog.isShowing()) {
                                                mDialog.dismiss();
                                            }
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
        } else {
            Timber.wtf("date conversion problem occurred");
        }
    }

    private void startUpdateService(Wish wish) {
        FirebaseJobDispatcher dispatcher
                = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        Bundle extraBundle = new Bundle();
        extraBundle.putString(
                ApplicationUtils.SERVICE_TYPE_IDENTIFIER,
                ApplicationUtils.SERVICE_TYPE_WISH
        );
        extraBundle.putString(
                ApplicationUtils.SERVICE_CONTENT_FROM_IDENTIFIER,
                wish.getConnectionFromUid()
        );
        extraBundle.putString(
                ApplicationUtils.SERVICE_CONTENT_TO_IDENTIFIER,
                wish.getConnectionToUid()
        );
        extraBundle.putString(
                ApplicationUtils.SERVICE_CONTENT_KEY_IDENTIFIER,
                wish.getKey()
        );
        Job myJob = dispatcher.newJobBuilder()
                .setService(UpdateStateService.class)
                .setTag(ApplicationUtils.getServiceUniqueTag(
                            wish.getConnectionFromUid(),
                            wish.getConnectionToUid(),
                            wish.getKey())
                )
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(getTriggerTime(wish))
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extraBundle)
                .build();
        dispatcher.mustSchedule(myJob);
    }

    private JobTrigger getTriggerTime(Wish wish) {
        String targetDateAndTimeAsText = wish.getWhenToArrive();
        String currentDateAndTimeAsText = ApplicationUtils.getCurrentUTCDateAndTime();
        Date targetDateAndTime = ApplicationUtils.getDateAndTime(targetDateAndTimeAsText);
        Date currentDateAndTime = ApplicationUtils.getDateAndTime(currentDateAndTimeAsText);
        if (targetDateAndTime == null || currentDateAndTime == null) {
            Timber.wtf("get trigger time problem occurred");
            return Trigger.NOW;
        }
        long diffInSecondsAsLong
                = Math.abs(targetDateAndTime.getTime() - currentDateAndTime.getTime()) / 1000;
        int diffInSecondsAsInt = (int) diffInSecondsAsLong;
        return Trigger.executionWindow(diffInSecondsAsInt, diffInSecondsAsInt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageUtils.onRequestPermissionResult(
                AddNewWishActivity.this,
                requestCode,
                grantResults
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ImageUtils.RC_PHOTO_PICKER
                || requestCode == ImageUtils.REQUEST_IMAGE_CAPTURE)
                && resultCode == RESULT_OK) {
            if (!mWishImagesRef.equals(mWishImagesRootRef)) {
                Task<Uri> task = mWishImagesRef.getDownloadUrl();
                task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Timber.d("completed");
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.wtf("failure - wish old image delete " + mCurrentUserUid);
                    }
                });
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String prevPhotoUrl = uri.toString();
                        ApplicationUtils.deleteImageFromStorage(
                                AddNewWishActivity.this,
                                prevPhotoUrl,
                                null,
                                null
                        );
                    }
                });
                task.addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Timber.d("canceled");
                    }
                });
            }
            mWishImagesRef = mWishImagesRootRef.child(
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
                mWishImagesRef = storageMetadata.getReference();
                if (mWishImagesRef == null) {
                    return;
                }
                mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String photoUrl = uri.toString();
                        mWishNoPhotoAlertTextView.setVisibility(View.GONE);
                        mWishAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                        ImageUtils.setPhoto(
                                AddNewWishActivity.this,
                                photoUrl,
                                mWishAddedExtraPhotoImageView,
                                false
                        );
                        mChosenPhotoUrl = photoUrl;
                    }
                });
            }
        };
        ImageUtils.onActivityResult(
                AddNewWishActivity.this,
                requestCode,
                resultCode,
                data,
                mWishImagesRef,
                uploadOnSuccessListener
        );
    }

    private void loadData(User user) {
        String gender = user.getGender();
        final String birthday = user.getBirthday();
        final String name = user.getUsername().split(" ")[0];
        List<String> occasionOptionsList = getPossibleOccasions(gender);
        ArrayAdapter<String> occasionListAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                android.R.layout.simple_spinner_item,
                occasionOptionsList
        );
        mSpinner.setAdapter(occasionListAdapter);
        final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getSelectedItem();
                applySpinnerSelectionHandler(selectedItem, name);
                addKnownDataToViewBasedOnSelection(selectedItem, birthday);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        mSpinner.setOnItemSelectedListener(onItemSelectedListener);
        mWishQuestionTextView.setText(
                new StringBuilder()
                        .append(getString(R.string.wish_question_1))
                        .append(name)
                        .append(getString(R.string.wish_question_2))
                        .toString()
        );

        applyPauseOrRotationHandling(mPausedData);
        applyPauseOrRotationHandling(mSavedInstanceState);
        mPausedData = null;
        mSavedInstanceState = null;

        mSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mSpinner.setOnItemSelectedListener(onItemSelectedListener);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mSpinner.setOnItemSelectedListener(null);
                }
                return false;
            }
        });
    }

    private void applyPauseOrRotationHandling(Bundle bundle) {
        if (bundle != null) {
            int chosenValue = bundle.getInt(SAVE_CHOSEN_VALUE);
            mSpinner.setOnItemSelectedListener(null);
            mSpinner.setSelection(chosenValue);
            String content = bundle.getString(SAVE_CONTENT);
            mWishEditText.setText(content);
            boolean isOtherEditTextVisible = bundle.getBoolean(SAVE_OTHER_EDIT_TEXT_VISIBILITY);
            if (isOtherEditTextVisible) {
                mWishOtherTextInputLayout.setVisibility(View.VISIBLE);
                String otherEditTextContent = bundle.getString(SAVE_OTHER_EDIT_TEXT_CONTENT);
                mWishOtherEditText.setText(otherEditTextContent);
                if (bundle.getBoolean(SAVE_OTHER_EDIT_TEXT_FOCUSED)) {
                    mWishOtherEditText.requestFocus();
                    mWishOtherEditText.setSelection(otherEditTextContent != null ? otherEditTextContent.length() : 0);
                }
            }
            if (bundle.getBoolean(SAVE_EDIT_TEXT_FOCUSED)) {
                mWishEditText.requestFocus();
                mWishEditText.setSelection(content != null ? content.length() : 0);
            }
            String date = bundle.getString(SAVE_DATE);
            mWishSelectedDateTextView.setText(date);
            String time = bundle.getString(SAVE_TIME);
            mWishSelectedTimeTextView.setText(time);
            boolean isExtraPhotoVisible = bundle.getBoolean(SAVE_EXTRA_VISIBILITY);
            if (isExtraPhotoVisible) {
                mWishNoPhotoAlertTextView.setVisibility(View.GONE);
                mWishAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                mChosenPhotoUrl = bundle.getString(SAVE_PHOTO_URL);
                String imageRef = bundle.getString(SAVE_IMAGE_REF);
                if (imageRef == null) {
                    Timber.wtf("imageref returned null after pause or rotation");
                    return;
                }
                mWishImagesRef = mFirebaseStorage.getReference(imageRef);
                ImageUtils.setPhoto(
                        AddNewWishActivity.this,
                        mChosenPhotoUrl,
                        mWishAddedExtraPhotoImageView,
                        false
                );
            }
        }
    }

    private void addKnownDataToViewBasedOnSelection(String selectedItem, String birthday) {
        String currDateAsText = ApplicationUtils.getCurrentUTCDateAndTime();
        Date currDate = ApplicationUtils.getDateAndTime(currDateAsText);
        if (currDate == null) {
            Timber.wtf("get date and time problem occurred");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.YEAR, 1);
        Date plusOneYearDate = calendar.getTime();
        SimpleDateFormat sdf
                = new SimpleDateFormat(ApplicationUtils.FULL_DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String plusOneYearDateAsText = sdf.format(plusOneYearDate);
        String currYear = currDateAsText.substring(0, 4);
        String nextYear = plusOneYearDateAsText.substring(0, 4);
        String dateToSet = getResources().getString(R.string.selected_date_default);
        if (selectedItem.equals(getString(R.string.wish_birthday_occasion))) {
            if (birthday != null && !birthday.equals(getString(R.string.settings_birthday_default_id))) {
                String [] birthdayParts = birthday.split("-");
                dateToSet = getDateToSet(
                        currDate, currYear, nextYear, birthdayParts[1], birthdayParts[2]);
            }
        } else if (selectedItem.equals(getString(R.string.wish_christmas_occasion))) {
            dateToSet = getDateToSet(
                    currDate, currYear, nextYear, "12", "24");
        } else if (selectedItem.equals(getString(R.string.wish_new_year_occasion))) {
            dateToSet = getDateToSet(
                    currDate, currYear, nextYear, "01", "01");
        } else if (selectedItem.equals(getString(R.string.wish_valentine_day_occasion))) {
            dateToSet = getDateToSet(
                    currDate, currYear, nextYear, "02", "14");
        } else if (selectedItem.equals(getString(R.string.wish_women_day_occasion))) {
            dateToSet = getDateToSet(
                    currDate, currYear, nextYear, "03", "08");
        }
        if (dateToSet == null) {
            dateToSet = getResources().getString(R.string.selected_date_default);
            Timber.wtf( "date to set problem occurred");
        }
        mWishSelectedDateTextView.setText(dateToSet);
    }

    private String getDateToSet(
            Date currDate,
            String currYear,
            String nextYear,
            String month,
            String day) {
        String dateAndTimeToSetAsText = currYear + month + day;
        String dateAndTimeToSetAsTextTemp = dateAndTimeToSetAsText + "000000";
        Date dateAndTimeToSet = ApplicationUtils.getDateAndTime(dateAndTimeToSetAsTextTemp);
        if (dateAndTimeToSet == null) {
            Timber.wtf("get date and time problem occurred");
            return null;
        }
        if (dateAndTimeToSet.before(currDate)) {
            dateAndTimeToSetAsText = nextYear + "-" + month + "-" + day;
        } else {
            dateAndTimeToSetAsText = currYear + "-" + month + "-" + day;
        }
        return dateAndTimeToSetAsText;
    }

    private void applySpinnerSelectionHandler(String selectedItem, String name) {
        mWishOtherTextInputLayout.setVisibility(View.GONE);
        if (selectedItem.equals(getString(R.string.wish_default_occasion))) {
            mWishEditText.setText("");
        } else if (selectedItem.equals(getString(R.string.wish_other_occasion))) {
            mWishOtherTextInputLayout.setVisibility(View.VISIBLE);
            mWishOtherEditText.setText("");
            mWishOtherEditText.requestFocus();
            mWishEditText.setText("");
        } else {
            String message = "";
            if (selectedItem.equals(getString(R.string.wish_birthday_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_person_spec_day_1))
                        .append(name)
                        .append(getString(R.string.wish_default_message_person_spec_day_2))
                        .append(getString(R.string.wish_birthday_occasion))
                        .append(getString(R.string.wish_default_message_person_spec_day_3))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_name_day_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_person_spec_day_1))
                        .append(name)
                        .append(getString(R.string.wish_default_message_person_spec_day_2))
                        .append(getString(R.string.wish_name_day_occasion))
                        .append(getString(R.string.wish_default_message_person_spec_day_3))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_christmas_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_year_spec_day_christmas))
                        .append(name)
                        .append(getString(R.string.wish_default_message_year_spec_day_end))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_new_year_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_year_spec_day_new_year))
                        .append(name)
                        .append(getString(R.string.wish_default_message_year_spec_day_end))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_easter_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_year_spec_day_easter))
                        .append(name)
                        .append(getString(R.string.wish_default_message_year_spec_day_end))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_valentine_day_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_valentine_day_1))
                        .append(name)
                        .append(getString(R.string.wish_default_message_valentine_day_2))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_anniversary_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_anniversary_day_1))
                        .append(name)
                        .append(getString(R.string.wish_default_message_anniversary_day_2))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_women_day_occasion))) {
                message = new StringBuilder()
                        .append(getString(R.string.wish_default_message_women_day_1))
                        .append(name)
                        .append(getString(R.string.wish_default_message_women_day_2))
                        .toString();
            } else if (selectedItem.equals(getString(R.string.wish_mother_day_occasion))) {
                message = getString(R.string.wish_default_message_mother_day);
            } else if (selectedItem.equals(getString(R.string.wish_father_day_occasion))){
                message = getString(R.string.wish_default_message_father_day);
            }
            mWishEditText.setText(message);
            if (mWishOtherTextInputLayout.getVisibility() != View.VISIBLE) {
                mWishEditText.setSelection(message.length());
            }
        }
    }

    private List<String> getPossibleOccasions(String gender) {
        List<String> occasionsList = new ArrayList<>();
        occasionsList.add(getString(R.string.wish_default_occasion));
        String[] basicOccasions = getResources().getStringArray(R.array.wish_basic_occasions);
        occasionsList.addAll(Arrays.asList(basicOccasions));
        if (mType.equals(getString(R.string.relationship_type_lover_id))) {
            String[] loveOccasions = getResources().getStringArray(R.array.wish_love_occasions);
            occasionsList.addAll(Arrays.asList(loveOccasions));
        }
        if (gender.equals(getString(R.string.gender_female_id))) {
            String[] femaleOccasions = getResources().getStringArray(R.array.wish_female_occasions);
            occasionsList.addAll(Arrays.asList(femaleOccasions));
        }
        String displayedType = ApplicationUtils.getPersonalizedRelationshipType(
                this,
                mType,
                !mGenderShouldBeReversed ? gender : null,
                !mGenderShouldBeReversed ? null : gender,
                mGenderShouldBeReversed
        );
        if (displayedType.equals(getString(R.string.relationship_type_godparent_male))
                || displayedType.equals(getString(R.string.relationship_type_parent_male))
                || displayedType.equals(getString(R.string.relationship_type_grandparent_male))) {
            String[] fatherOccasions = getResources().getStringArray(R.array.wish_father_occasions);
            occasionsList.addAll(Arrays.asList(fatherOccasions));
        }

        if (displayedType.equals(getString(R.string.relationship_type_godparent_female))
                || displayedType.equals(getString(R.string.relationship_type_parent_female))
                || displayedType.equals(getString(R.string.relationship_type_grandparent_female))) {
            String[] motherOccasions = getResources().getStringArray(R.array.wish_mother_occasions);
            occasionsList.addAll(Arrays.asList(motherOccasions));
        }
        occasionsList.add(getString(R.string.wish_other_occasion));
        return occasionsList;
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
        mPausedData.putInt(SAVE_CHOSEN_VALUE, mSpinner.getSelectedItemPosition());
        mPausedData.putString(SAVE_CONTENT, mWishEditText.getText().toString());
        mPausedData.putString(SAVE_DATE, mWishSelectedDateTextView.getText().toString());
        mPausedData.putString(SAVE_TIME, mWishSelectedTimeTextView.getText().toString());
        boolean isExtraPhotoVisible = mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE;
        mPausedData.putBoolean(SAVE_EXTRA_VISIBILITY, isExtraPhotoVisible);
        if (isExtraPhotoVisible) {
            mPausedData.putString(SAVE_PHOTO_URL, mChosenPhotoUrl);
        }
        boolean isOtherEditTextVisible = mWishOtherTextInputLayout.getVisibility() == View.VISIBLE;
        mPausedData.putBoolean(SAVE_OTHER_EDIT_TEXT_VISIBILITY, isOtherEditTextVisible);
        if (isOtherEditTextVisible) {
            mPausedData.putString(SAVE_OTHER_EDIT_TEXT_CONTENT, mWishOtherEditText.getText().toString());
        }
        mPausedData.putBoolean(SAVE_EDIT_TEXT_FOCUSED, mWishEditText.isFocused());
        mPausedData.putBoolean(SAVE_OTHER_EDIT_TEXT_FOCUSED, mWishOtherEditText.isFocused());
        mPausedData.putString(SAVE_IMAGE_REF, mWishImagesRef.getPath());
        if (mUserValueEventListener != null) {
            mUserDatabaseRef.removeEventListener(mUserValueEventListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_CHOSEN_VALUE, mSpinner.getSelectedItemPosition());
        outState.putString(SAVE_CONTENT, mWishEditText.getText().toString());
        outState.putString(SAVE_DATE, mWishSelectedDateTextView.getText().toString());
        outState.putString(SAVE_TIME, mWishSelectedTimeTextView.getText().toString());
        boolean isExtraPhotoVisible = mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(SAVE_EXTRA_VISIBILITY, isExtraPhotoVisible);
        if (isExtraPhotoVisible) {
            outState.putString(SAVE_PHOTO_URL, mChosenPhotoUrl);
        }
        boolean isOtherEditTextVisible = mWishOtherTextInputLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(SAVE_OTHER_EDIT_TEXT_VISIBILITY, isOtherEditTextVisible);
        if (isOtherEditTextVisible) {
            outState.putString(SAVE_OTHER_EDIT_TEXT_CONTENT, mWishOtherEditText.getText().toString());
        }
        outState.putBoolean(SAVE_EDIT_TEXT_FOCUSED, mWishEditText.isFocused());
        outState.putBoolean(SAVE_OTHER_EDIT_TEXT_FOCUSED, mWishOtherEditText.isFocused());
        outState.putString(SAVE_IMAGE_REF, mWishImagesRef.getPath());
        super.onSaveInstanceState(outState);
    }
}
