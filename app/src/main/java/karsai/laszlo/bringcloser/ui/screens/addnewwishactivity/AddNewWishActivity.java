package karsai.laszlo.bringcloser.ui.screens.addnewwishactivity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.fragment.DatePickerFragment;
import karsai.laszlo.bringcloser.fragment.TimePickerFragment;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.service.UpdateStateService;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class AddNewWishActivity extends AppCompatActivity {

    @BindView(R.id.spinner_wish)
    Spinner mSpinner;
    @BindView(R.id.et_wish)
    EditText mWishEditText;
    @BindView(R.id.et_wish_other)
    EditText mWishOtherEditText;
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

    private ConnectionDetail mConnectionDetail;
    private String mOtherUid;
    private String mType;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseRef;
    private ValueEventListener mUserValueEventListener;
    private View mSnackbarView;
    private FirebaseUser mFirebaseUser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_wish);
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
            mType = receivedData.getStringExtra(ApplicationHelper.EXTRA_TYPE);

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
            mWishDateSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationHelper.EXTRA_ID, R.id.tv_wish_selected_date);
                    datePickerFragment.setArguments(bundle);
                    datePickerFragment.show(getSupportFragmentManager(), ApplicationHelper.TAG_DATA_PICKER);
                }
            });
            mWishTimeSelectorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment timePickerFragment = new TimePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ApplicationHelper.EXTRA_ID, R.id.tv_wish_selected_time);
                    timePickerFragment.setArguments(bundle);
                    timePickerFragment.show(getSupportFragmentManager(), ApplicationHelper.TAG_TIME_PICKER);
                }
            });
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            mFirebaseStorage = FirebaseStorage.getInstance();
            mWishImagesRootRef = mFirebaseStorage.getReference()
                    .child(mFirebaseUser.getUid())
                    .child(ApplicationHelper.STORAGE_WISH_IMAGES_FOLDER);
            mWishImagesRef = mWishImagesRootRef;
            mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.CONNECTIONS_NODE);
            mWishGalleryPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromFile(AddNewWishActivity.this);
                }
            });
            mWishCameraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
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
                            ApplicationHelper.deleteImageFromStorage(
                                    AddNewWishActivity.this,
                                    photoUrl
                            );
                            mWishAddPhotoLinearLayout.setVisibility(View.GONE);
                            mWishNoPhotoAlertTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            mApproveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSpinner.getSelectedItem().equals(getString(R.string.wish_default_occasion))) {
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_occasion),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mSpinner.getSelectedItem()
                            .equals(getString(R.string.wish_other_occasion))
                            && mWishOtherEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_occasion_specified),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.wish_alert_no_text),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishSelectedDateTextView.getText().toString()
                            .equals(getString(R.string.selected_date_default))) {
                        Snackbar.make(
                                view,
                                getString(R.string.alert_no_date),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else if (mWishSelectedTimeTextView.getText().toString()
                            .equals(getString(R.string.selected_time_default))) {
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
        }
    }

    private void deletePhotoIfExists() {
        if (mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
            mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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

    private void createWishItem(final String photoUrl) {
        String dateComposition = new StringBuilder()
                .append(mWishSelectedDateTextView.getText().toString())
                .append(ApplicationHelper.DATE_SPLITTER)
                .append(mWishSelectedTimeTextView.getText().toString())
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
                                mWishesDatabaseRef = mConnectionsDatabaseReference
                                        .child(key)
                                        .child(ApplicationHelper.WISHES_NODE);
                                DatabaseReference databaseReference
                                        = mWishesDatabaseRef.push();
                                String selectedOccasion = (String) mSpinner.getSelectedItem();
                                if (selectedOccasion.equals(getString(R.string.wish_other_occasion))) {
                                    selectedOccasion = mWishOtherEditText.getText().toString();
                                }
                                final Wish wish = new Wish(
                                        mCurrentUserUid,
                                        mConnectionDetail.getFromUid(),
                                        mConnectionDetail.getToUid(),
                                        photoUrl,
                                        finalWhenToArrive,
                                        selectedOccasion,
                                        mWishEditText.getText().toString(),
                                        false,
                                        databaseReference.getKey()
                                );
                                databaseReference.setValue(wish).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startUpdateService(wish);
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

    private void startUpdateService(Wish wish) {
        FirebaseJobDispatcher dispatcher
                = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        Bundle extraBundle = new Bundle();
        extraBundle.putString(
                ApplicationHelper.SERVICE_TYPE_IDENTIFIER,
                ApplicationHelper.SERVICE_TYPE_WISH
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_FROM_IDENTIFIER,
                wish.getConnectionFromUid()
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_TO_IDENTIFIER,
                wish.getConnectionToUid()
        );
        extraBundle.putString(
                ApplicationHelper.SERVICE_CONTENT_KEY_IDENTIFIER,
                wish.getKey()
        );
        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(UpdateStateService.class)
                // uniquely identifies the job
                .setTag(ApplicationHelper.getServiceUniqueTag(
                            wish.getConnectionFromUid(),
                            wish.getConnectionToUid(),
                            wish.getKey())
                )
                // one-off job
                //.setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                .setTrigger(getTriggerTime(wish))
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

    private JobTrigger getTriggerTime(Wish wish) {
        String targetTime = wish.getWhenToArrive() + ":00";
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
        final StorageReference tempRef = mWishImagesRef;
        mWishImagesRef = mWishImagesRootRef.child(
                ApplicationHelper.getCurrentUTCDateAndTime(
                        ApplicationHelper.DATE_PATTERN_FULL_STORAGE
                )
        );
        OnSuccessListener<UploadTask.TaskSnapshot> uploadOnSuccessListener
                = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mWishImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        final String photoUrl = uri.toString();
                        if (mWishAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
                            tempRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri prevUri) {
                                    String prevPhotoUrl = prevUri.toString();
                                    ApplicationHelper.deleteImageFromStorage(
                                            AddNewWishActivity.this,
                                            prevPhotoUrl
                                    );
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
                        } else {
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
                    }
                });
            }
        };
        ImageUtils.onActivityResult(
                AddNewWishActivity.this,
                mSnackbarView,
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
                mWishOtherEditText.setVisibility(View.VISIBLE);
                String otherEditTextContent = bundle.getString(SAVE_OTHER_EDIT_TEXT_CONTENT);
                mWishOtherEditText.setText(otherEditTextContent);
                if (bundle.getBoolean(SAVE_OTHER_EDIT_TEXT_FOCUSED)) {
                    mWishOtherEditText.requestFocus();
                    mWishOtherEditText.setSelection(otherEditTextContent.length());
                }
            }
            if (bundle.getBoolean(SAVE_EDIT_TEXT_FOCUSED)) {
                mWishEditText.requestFocus();
                mWishEditText.setSelection(content.length());
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
        SimpleDateFormat sdf = new SimpleDateFormat(ApplicationHelper.DATE_PATTERN, Locale.getDefault());
        Date currDate = new Date();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.YEAR, 1);
        Date plusOneYearDate = calendar.getTime();
        sdf.setTimeZone(TimeZone.getDefault());
        String currDateAsText = sdf.format(currDate);
        String plusOneYearDateAsText = sdf.format(plusOneYearDate);
        String currYear = currDateAsText.split("-")[0];
        String nextYear = plusOneYearDateAsText.split("-")[0];
        String dateToSet = getString(R.string.selected_date_default);
        if (selectedItem.equals(getString(R.string.wish_birthday_occasion))) {
            if (birthday != null && !birthday.equals(getString(R.string.settings_birthday_default))) {
                String [] birthdayParts = birthday.split("-");
                dateToSet = getDateToSet(
                        currDate, sdf, currYear, nextYear, birthdayParts[1], birthdayParts[2]);
            }
        } else if (selectedItem.equals(getString(R.string.wish_christmas_occasion))) {
            dateToSet = getDateToSet(
                    currDate, sdf, currYear, nextYear, "12", "24");
        } else if (selectedItem.equals(getString(R.string.wish_new_year_occasion))) {
            dateToSet = getDateToSet(
                    currDate, sdf, currYear, nextYear, "01", "01");
        } else if (selectedItem.equals(getString(R.string.wish_valentine_day_occasion))) {
            dateToSet = getDateToSet(
                    currDate, sdf, currYear, nextYear, "02", "14");
        } else if (selectedItem.equals(getString(R.string.wish_women_day_occasion))) {
            dateToSet = getDateToSet(
                    currDate, sdf, currYear, nextYear, "03", "08");
        }
        mWishSelectedDateTextView.setText(dateToSet);
    }

    private String getDateToSet(
            Date currDate,
            SimpleDateFormat sdf,
            String currYear,
            String nextYear,
            String month,
            String day) {
        String dateToSet = currYear + "-" + month + "-" + day;
        try {
            Date tempDate = sdf.parse(dateToSet);
            if (tempDate.before(currDate)) {
                dateToSet = nextYear + "-" + month + "-" + day;
            }
        } catch (ParseException e) {
            dateToSet = "";
        }
        return dateToSet;
    }

    private void applySpinnerSelectionHandler(String selectedItem, String name) {
        mWishOtherEditText.setVisibility(View.GONE);
        if (selectedItem.equals(getString(R.string.wish_default_occasion))) {
            mWishEditText.setText("");
        } else if (selectedItem.equals(getString(R.string.wish_other_occasion))) {
            mWishOtherEditText.setVisibility(View.VISIBLE);
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
            if (mWishOtherEditText.getVisibility() != View.VISIBLE) {
                mWishEditText.setSelection(message.length());
            }
        }
    }

    private List<String> getPossibleOccasions(String gender) {
        List<String> occasionsList = new ArrayList<>();
        occasionsList.add(getString(R.string.wish_default_occasion));
        String[] basicOccasions = getResources().getStringArray(R.array.wish_basic_occasions);
        occasionsList.addAll(Arrays.asList(basicOccasions));
        if (mType.equals(getString(R.string.relationship_type_lover))) {
            String[] loveOccasions = getResources().getStringArray(R.array.wish_love_occasions);
            occasionsList.addAll(Arrays.asList(loveOccasions));
        }
        if (gender.equals(getString(R.string.gender_female))) {
            String[] femaleOccasions = getResources().getStringArray(R.array.wish_female_occasions);
            occasionsList.addAll(Arrays.asList(femaleOccasions));
        }
        String displayedType = ApplicationHelper.getPersonalizedRelationshipType(
                this,
                mType,
                gender,
                null,
                false
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
        boolean isOtherEditTextVisible = mWishOtherEditText.getVisibility() == View.VISIBLE;
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
        boolean isOtherEditTextVisible = mWishOtherEditText.getVisibility() == View.VISIBLE;
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
