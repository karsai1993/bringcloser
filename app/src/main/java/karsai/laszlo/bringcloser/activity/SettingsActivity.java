package karsai.laszlo.bringcloser.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.model.Wish;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity to show the profile settings of the user
 */
public class SettingsActivity extends CommonActivity implements AdapterView.OnItemSelectedListener {

    private static final String PROFILE_PHOTO_FILE_NAME = "profile_picture";
    private static final String FIREBASE_PHOTO_IDENTIFIER = "firebasestorage";
    private static final String KEY_USER = "user";
    private static final int DIALOG_PADDING = 10;

    @BindView(R.id.iv_settings_user_photo)
    ImageView mUserPhoto;
    @BindView(R.id.tv_settings_displayed_name)
    TextView mDisplayedName;
    @BindView(R.id.tv_settings_birthday)
    TextView mBirthday;
    @BindView(R.id.spinner_settings_gender)
    Spinner mGenderOptions;
    @BindView(R.id.tv_settings_verification)
    TextView mSettingsVerificationStatus;
    @BindView(R.id.tv_settings_verification_change)
    TextView mSettingsVerificationChange;
    @BindView(R.id.tv_settings_displayed_name_change)
    TextView mSettingsDisplayedNameChange;
    @BindView(R.id.tv_settings_birthday_change)
    TextView mSettingsBirthdayChange;
    @BindView(R.id.fab_user_photo_add_file)
    FloatingActionButton mSettingsPhotoFromFile;
    @BindView(R.id.fab_user_photo_add_camera)
    FloatingActionButton mSettingsPhotoFromCamera;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;
    @BindView(R.id.tv_analysis_status)
    TextView mAnalysisStatus;
    @BindView(R.id.switch_analysis)
    Switch mAnalysisSwitch;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersRootDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private StorageReference mUserStorageReference;
    private StorageReference mUserProfilePictureStorageReference;
    private FirebaseStorage mFirebaseStorage;
    private User mCurrentUser;
    private List<String> mGenderOptionList;
    private List<String> mGenderOptionsIdList;
    private View mSnackbarView;
    private FirebaseUser mFirebaseUser;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageUtils.onRequestPermissionResult(
                SettingsActivity.this,
                requestCode,
                grantResults
        );
    }

    private List<String> getGenderOptionsList() {
        List<String> genderOptionsList = new ArrayList<>();
        genderOptionsList.add(getResources().getString(R.string.gender_none));
        genderOptionsList.add(getResources().getString(R.string.gender_male));
        genderOptionsList.add(getResources().getString(R.string.gender_female));
        return genderOptionsList;
    }

    private List<String> getGenderOptionsIdList() {
        List<String> genderOptionsIdList = new ArrayList<>();
        genderOptionsIdList.add(getResources().getString(R.string.gender_none_id));
        genderOptionsIdList.add(getResources().getString(R.string.gender_male_id));
        genderOptionsIdList.add(getResources().getString(R.string.gender_female_id));
        return genderOptionsIdList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mUserPhoto = findViewById(R.id.iv_settings_user_photo);
        mDisplayedName = findViewById(R.id.tv_settings_displayed_name);
        mBirthday = findViewById(R.id.tv_settings_birthday);
        mGenderOptionList = getGenderOptionsList();
        mGenderOptionsIdList = getGenderOptionsIdList();
        mGenderOptions = findViewById(R.id.spinner_settings_gender);
        mGenderOptions.setOnItemSelectedListener(this);
        mSettingsVerificationStatus = findViewById(R.id.tv_settings_verification);
        mSettingsVerificationChange = findViewById(R.id.tv_settings_verification_change);
        Intent receivedData = getIntent();
        mCurrentUser = receivedData.getParcelableExtra(ApplicationUtils.USER_KEY);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersRootDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationUtils.USERS_NODE);
        mUserDatabaseReference = mUsersRootDatabaseReference.child(mFirebaseUser.getUid());
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserStorageReference = mFirebaseStorage.getReference().child(mFirebaseUser.getUid());
        populateUI(getApplicationContext());
        mSettingsPhotoFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirebaseUser.isEmailVerified()) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromFile(SettingsActivity.this);
                } else {
                    Snackbar.make(
                            view,
                            getResources().getString(R.string.not_verified_email_address_message),
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }
        });
        mSettingsPhotoFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirebaseUser.isEmailVerified()) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromCamera(SettingsActivity.this);
                } else {
                    Snackbar.make(
                            view,
                            getResources().getString(R.string.not_verified_email_address_message),
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }
        });
        mSettingsDisplayedNameChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(SettingsActivity.this);
                String inputText = mCurrentUser.getUsername();
                input.setText(inputText);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                DialogInterface.OnClickListener dialogOnPositiveBtnClickListener
                        = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String newName = input.getText().toString();
                        if (newName.length() == 0) {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    getResources().getString(R.string.zero_character_name_message),
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            mCurrentUser.setUsername(newName);
                            populateUI(getApplicationContext());
                            updateDB(mCurrentUser);
                        }
                    }
                };
                DialogUtils.onDialogRequest(
                        SettingsActivity.this,
                        getResources().getString(R.string.dialog_settings_name),
                        input,
                        dialogOnPositiveBtnClickListener,
                        R.style.DialogLeftRightTheme
                );
            }
        });
        mSettingsBirthdayChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePickerFragment = new BirthdayDatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_USER, mCurrentUser);
                datePickerFragment.setArguments(bundle);
                datePickerFragment.show(getSupportFragmentManager(), ApplicationUtils.TAG_DATA_PICKER);
            }
        });
        mSettingsVerificationChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView verificationInfoTextView = new TextView(getApplicationContext());
                verificationInfoTextView.setText(
                        getResources().getString(R.string.settings_verification_dialog_info)
                );
                verificationInfoTextView.setPadding(
                        DIALOG_PADDING,
                        DIALOG_PADDING,
                        DIALOG_PADDING,
                        DIALOG_PADDING
                );
                DialogInterface.OnClickListener onClickListener
                        = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mFirebaseUser.sendEmailVerification();
                        mSettingsVerificationStatus.setText(
                                getResources().getString(R.string.settings_verification_ongoing)
                        );
                        mSettingsVerificationChange.setVisibility(View.GONE);
                    }
                };
                DialogUtils.onDialogRequest(
                        SettingsActivity.this,
                        getResources().getString(R.string.settings_verification_dialog_title),
                        verificationInfoTextView,
                        onClickListener,
                        R.style.DialogUpDownTheme
                );
            }
        });
        String storedAnalysisIdentifier = ApplicationUtils.getValueFromPrefs(
                this,
                ApplicationUtils.EMOJI_KEY
        );
        if (storedAnalysisIdentifier == null) {
            ApplicationUtils.saveValueToPrefs(
                    this,
                    ApplicationUtils.EMOJI_KEY,
                    ApplicationUtils.EMOJI_ENABLE_IDENTIFIER
            );
            mAnalysisStatus.setText(getResources().getString(R.string.status_yes));
            mAnalysisSwitch.setChecked(true);
        } else if (storedAnalysisIdentifier.equals(ApplicationUtils.EMOJI_ENABLE_IDENTIFIER)) {
            mAnalysisStatus.setText(getResources().getString(R.string.status_yes));
            mAnalysisSwitch.setChecked(true);
        } else {
            mAnalysisStatus.setText(getResources().getString(R.string.status_no));
            mAnalysisSwitch.setChecked(false);
        }
        mAnalysisSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String valueToStore;
                String valueToDisplay;
                if (isChecked) {
                    valueToStore = ApplicationUtils.EMOJI_ENABLE_IDENTIFIER;
                    valueToDisplay = getResources().getString(R.string.status_yes);
                } else {
                    valueToStore = ApplicationUtils.EMOJI_DISENABLE_IDENTIFIER;
                    valueToDisplay = getResources().getString(R.string.status_no);
                }
                ApplicationUtils.saveValueToPrefs(
                        SettingsActivity.this,
                        ApplicationUtils.EMOJI_KEY,
                        valueToStore
                );
                mAnalysisStatus.setText(valueToDisplay);
                mAnalysisSwitch.setChecked(isChecked);
            }
        });
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final StorageReference photoRef
                = mUserStorageReference.child(PROFILE_PHOTO_FILE_NAME);
        OnSuccessListener<UploadTask.TaskSnapshot> uploadOnSuccessListener
                = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mCurrentUser.setPhotoUrl(uri.toString());
                        populateUI(getApplicationContext());
                        updateDB(mCurrentUser);
                    }
                });
            }
        };
        ImageUtils.onActivityResult(
                SettingsActivity.this,
                mSnackbarView,
                requestCode,
                resultCode,
                data,
                photoRef,
                uploadOnSuccessListener
        );
    }

    private void populateUI(Context context) {
        String photoUrl = mCurrentUser.getPhotoUrl();
        String username = mCurrentUser.getUsername();
        String birthday = mCurrentUser.getBirthday();
        String gender = mCurrentUser.getGender();
        ArrayAdapter<String> genderListAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                mGenderOptionList
        );
        mGenderOptions.setAdapter(genderListAdapter);
        if (gender == null || gender.equals(context.getResources().getString(R.string.gender_none_id))) {
            mGenderOptions.setSelection(0, true);
        } else if (gender.equals(context.getResources().getString(R.string.gender_male_id))){
            mGenderOptions.setSelection(1, true);
        } else {
            mGenderOptions.setSelection(2, true);
        }
        ImageUtils.setPhoto(context, photoUrl, mUserPhoto, true);
        mDisplayedName.setText(username);
        if (birthday != null) {
            if (birthday.equals(getResources().getString(R.string.settings_birthday_default_id))) {
                mBirthday.setText(getResources().getString(R.string.settings_birthday_default));
            } else {
                mBirthday.setText(birthday);
            }
        }
        if (mCurrentUser.getIsEmailVerified()) {
            mSettingsVerificationStatus.setText(
                    context.getResources().getString(R.string.settings_verification_positive)
            );
            mSettingsVerificationChange.setVisibility(View.GONE);
        } else {
            mSettingsVerificationStatus.setText(
                    context.getResources().getString(R.string.settings_verification_negative)
            );
            mSettingsVerificationChange.setVisibility(View.VISIBLE);
        }
    }

    private void updateDB(User user) {
        String photoUrl = user.getPhotoUrl();
        String username = user.getUsername();
        String birthday = user.getBirthday();
        String gender = user.getGender();
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("/" + ApplicationUtils.USER_BIRTHDAY_IDENTIFIER, birthday);
        updateValues.put("/" + ApplicationUtils.USER_PHOTO_URL_IDENTIFIER, photoUrl);
        updateValues.put("/" + ApplicationUtils.USER_NAME_IDENTIFIER, username);
        updateValues.put("/" + ApplicationUtils.USER_GENDER_IDENTIFIER, gender);
        mUserDatabaseReference.updateChildren(updateValues);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        mCurrentUser.setGender(mGenderOptionsIdList.get(position));
        updateDB(mCurrentUser);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_account) {
            DialogInterface.OnClickListener onDeletePositiveBtnListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteRelatedStorageData();
                    deleteRelatedDatabaseData();
                }
            };
            View dialogViewDelete = LayoutInflater.from(this)
                    .inflate(R.layout.delete_account,
                            (ViewGroup) getWindow().getDecorView().getRootView(),
                            false);
            DialogUtils.onDialogRequest(
                    SettingsActivity.this,
                    getResources().getString(R.string.delete_account_dialog_title),
                    dialogViewDelete,
                    onDeletePositiveBtnListener,
                    R.style.DialogUpDownTheme
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAccount() {
        mFirebaseUser.delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(
                        findViewById(R.id.fab_user_photo_add_file),
                        getResources().getString(R.string.re_authenticate_request),
                        Snackbar.LENGTH_LONG
                ).show();
                restoreRelatedDatabaseData();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
                finish();
            }
        });
    }

    private void restoreRelatedDatabaseData() {
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put(
                "/" + ApplicationUtils.USER_BIRTHDAY_IDENTIFIER, mCurrentUser.getBirthday()
        );
        updateValues.put(
                "/" + ApplicationUtils.USER_PHOTO_URL_IDENTIFIER, mCurrentUser.getPhotoUrl()
        );
        updateValues.put(
                "/" + ApplicationUtils.USER_NAME_IDENTIFIER, mCurrentUser.getUsername()
        );
        updateValues.put(
                "/" + ApplicationUtils.USER_GENDER_IDENTIFIER, mCurrentUser.getGender()
        );
        updateValues.put(
                "/" + ApplicationUtils.USER_UID_IDENTIFIER, mCurrentUser.getUid()
        );
        updateValues.put(
                "/" + ApplicationUtils.USER_EMAIL_VERIFICATION_IDENTIFIER,
                mCurrentUser.getIsEmailVerified()
        );
        mUserDatabaseReference.updateChildren(updateValues);
        mUserDatabaseReference.child(ApplicationUtils.USER_TOKENS_IDENTIFIER)
                .setValue(mCurrentUser.getTokensMap());
    }

    private void deleteRelatedDatabaseData() {
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("/" + mFirebaseUser.getUid(), null);
        mUsersRootDatabaseReference.updateChildren(updateValues);
        String currUid = mFirebaseUser.getUid();
        deleteSingleMemoryElements(SettingsActivity.this, currUid);
    }

    private void deleteSingleMemoryElements(
            final Context context,
            final String uid) {
        final DatabaseReference connectionsRef
                = mFirebaseDatabase.getReference().child(ApplicationUtils.CONNECTIONS_NODE);
        connectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connSnapshot : dataSnapshot.getChildren()) {
                    String key = connSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null delete single memory elements connection identifier" + uid);
                        continue;
                    }
                    int count = 0;
                    List<String> keyList = new ArrayList<>();
                    if (key.contains(uid)) {
                        count++;
                        keyList.add(key);
                    }
                    Toast.makeText(context,
                            new StringBuilder()
                            .append(context.getResources().getString(R.string.delete_account_conn_number_1))
                            .append(count)
                            .append(context.getResources().getString(R.string.delete_account_conn_number_2))
                            .toString(),
                            Toast.LENGTH_LONG
                    ).show();
                    int currCounter = 0;
                    for (String currentKey : keyList) {
                        currCounter ++;
                        DatabaseReference messagesDatabaseRef = connectionsRef
                                .child(currentKey)
                                .child(ApplicationUtils.MESSAGES_NODE);
                        final DatabaseReference wishesDatabaseRef = connectionsRef
                                .child(currentKey)
                                .child(ApplicationUtils.WISHES_NODE);
                        final DatabaseReference eventsDatabaseRef = connectionsRef
                                .child(currentKey)
                                .child(ApplicationUtils.EVENTS_NODE);
                        final DatabaseReference thoughtsDatabaseRef = connectionsRef
                                .child(currentKey)
                                .child(ApplicationUtils.THOUGHTS_NODE);
                        deleteSingleMemoryElements(
                                messagesDatabaseRef,
                                wishesDatabaseRef,
                                eventsDatabaseRef,
                                thoughtsDatabaseRef,
                                context
                        );
                        if (currCounter == count) {
                            deleteSingleConnection(uid);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteSingleMemoryElements(
            DatabaseReference messagesDatabaseRef,
            final DatabaseReference wishesDatabaseRef,
            final DatabaseReference eventsDatabaseRef,
            final DatabaseReference thoughtsDatabaseRef,
            final Context context) {
        messagesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message == null) continue;
                    ApplicationUtils.deleteImageFromStorage(context, message.getPhotoUrl());
                }
                wishesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Wish wish = snapshot.getValue(Wish.class);
                            if (wish == null) continue;
                            ApplicationUtils.deleteImageFromStorage(context, wish.getExtraPhotoUrl());
                        }
                        eventsDatabaseRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Event event = snapshot.getValue(Event.class);
                                            if (event == null) continue;
                                            ApplicationUtils.deleteImageFromStorage(context, event.getExtraPhotoUrl());
                                        }
                                        thoughtsDatabaseRef.addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            Thought thought = snapshot.getValue(Thought.class);
                                                            if (thought == null) continue;
                                                            ApplicationUtils.deleteImageFromStorage(context, thought.getExtraPhotoUrl());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteSingleConnection(final String uid) {
        final DatabaseReference connectionsRef = mFirebaseDatabase.getReference().child(
                ApplicationUtils.CONNECTIONS_NODE);
        final DatabaseReference visibilityRef
                = mFirebaseDatabase.getReference().child(ApplicationUtils.CHAT_VISIBILITY_NODE);
        final DatabaseReference typingRef
                = mFirebaseDatabase.getReference().child(ApplicationUtils.CHAT_TYPING_NODE);
        connectionsRef.orderByChild(ApplicationUtils.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null delete single connection connection from uid identifier" + uid);
                                continue;
                            }
                            connectionsRef.child(key).setValue(null);
                            visibilityRef.child(key).setValue(null);
                            typingRef.child(key).setValue(null);
                        }
                        connectionsRef.orderByChild(ApplicationUtils.CONNECTION_TO_UID_IDENTIFIER)
                                .equalTo(uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                                            String key = connectionSnapshot.getKey();
                                            if (key == null) {
                                                Timber.wtf("key null delete single connection connection to uid identifier" + uid);
                                                continue;
                                            }
                                            connectionsRef.child(key).setValue(null);
                                            visibilityRef.child(key).setValue(null);
                                            typingRef.child(key).setValue(null);
                                        }
                                        deleteAccount();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteRelatedStorageData() {
        String currentPhotoUrl = mCurrentUser.getPhotoUrl();
        if (currentPhotoUrl != null && currentPhotoUrl.contains(FIREBASE_PHOTO_IDENTIFIER)) {
            mUserProfilePictureStorageReference
                    = mFirebaseStorage.getReferenceFromUrl(currentPhotoUrl);
            mUserProfilePictureStorageReference.delete();
        }
    }

    public static class BirthdayDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Activity mActivity;
        private Calendar mActualCalendar;
        private User mCurrentUser;
        private FirebaseDatabase mFirebaseDatabase;

        public BirthdayDatePickerFragment() {}

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mActivity = getActivity();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mActualCalendar = Calendar.getInstance();
            int year = mActualCalendar.get(Calendar.YEAR);
            int month = mActualCalendar.get(Calendar.MONTH);
            int day = mActualCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                dialog = new DatePickerDialog(mActivity,
                        AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);
            } else {
                dialog = new DatePickerDialog(mActivity,
                        R.style.AppTheme_DialogTheme,this,year,month,day);
            }
            Bundle receivedData = getArguments();
            if (receivedData != null) {
                mCurrentUser = receivedData.getParcelable(KEY_USER);
                final TextView birthdayTextView = mActivity.findViewById(R.id.tv_settings_birthday);
                final DatabaseReference userRef = mFirebaseDatabase.getReference()
                        .child(ApplicationUtils.USERS_NODE).child(mCurrentUser.getUid());
                dialog.setButton(
                        DialogInterface.BUTTON_NEUTRAL,
                        getResources().getString(R.string.settings_birthday_set_default),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String birthday = getResources()
                                        .getString(R.string.settings_birthday_default_id);
                                mCurrentUser.setBirthday(birthday);
                                Map<String, Object> updateValues = new HashMap<>();
                                updateValues.put(
                                        "/" + ApplicationUtils.USER_BIRTHDAY_IDENTIFIER,
                                        birthday);
                                userRef.updateChildren(updateValues);
                                birthdayTextView.setText(
                                        getResources()
                                                .getString(R.string.settings_birthday_default));
                            }
                        });
            } else {
                Timber.e("Birthday picker with null received data error");
            }
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            final TextView birthdayTextView = mActivity.findViewById(R.id.tv_settings_birthday);
            final DatabaseReference userRef = mFirebaseDatabase.getReference()
                    .child(ApplicationUtils.USERS_NODE).child(mCurrentUser.getUid());
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year, month, day);
            if (newCalendar.getTime().after(mActualCalendar.getTime())) {
                Toast.makeText(
                        getContext(),
                        getResources().getString(R.string.wrong_date_selected_message_before),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                String increasedMonth = String.valueOf(month + 1);
                String date = new StringBuilder()
                        .append(year)
                        .append("-")
                        .append(increasedMonth.length() == 1 ? "0" + increasedMonth : increasedMonth)
                        .append("-")
                        .append(day < 10 ? "0" + day : day)
                        .toString();
                mCurrentUser.setBirthday(date);
                Map<String, Object> updateValues = new HashMap<>();
                updateValues.put("/" + ApplicationUtils.USER_BIRTHDAY_IDENTIFIER, date);
                userRef.updateChildren(updateValues);
                birthdayTextView.setText(date);
            }
        }
    }
}
