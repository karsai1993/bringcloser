package karsai.laszlo.bringcloser.ui.screens.settings;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.ui.screens.welcome.WelcomeActivity;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.DialogUtils;
import karsai.laszlo.bringcloser.utils.ImageUtils;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG_DATA_PICKER = "data_picker";
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


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersRootDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private StorageReference mUserStorageReference;
    private StorageReference mUserProfilePictureStorageReference;
    private FirebaseStorage mFirebaseStorage;
    private User mCurrentUser;
    private List<String> mGenderOptionList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mUserPhoto = findViewById(R.id.iv_settings_user_photo);
        mDisplayedName = findViewById(R.id.tv_settings_displayed_name);
        mBirthday = findViewById(R.id.tv_settings_birthday);
        mGenderOptionList = ApplicationHelper.getGenderOptionsList(getApplicationContext());
        mGenderOptions = findViewById(R.id.spinner_settings_gender);
        mGenderOptions.setOnItemSelectedListener(this);
        mSettingsVerificationStatus = findViewById(R.id.tv_settings_verification);
        mSettingsVerificationChange = findViewById(R.id.tv_settings_verification_change);
        Intent receivedData = getIntent();
        mCurrentUser = receivedData.getParcelableExtra(ApplicationHelper.USER_KEY);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersRootDatabaseReference = mFirebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE);
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
                final EditText input = new EditText(getApplicationContext());
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
                                    getApplicationContext(),
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
                DialogFragment datePickerFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_USER, mCurrentUser);
                datePickerFragment.setArguments(bundle);
                datePickerFragment.show(getSupportFragmentManager(), TAG_DATA_PICKER);
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
        if (gender == null || gender.equals(context.getResources().getString(R.string.gender_none))) {
            mGenderOptions.setSelection(0, true);
        } else if (gender.equals(context.getResources().getString(R.string.gender_male))){
            mGenderOptions.setSelection(1, true);
        } else {
            mGenderOptions.setSelection(2, true);
        }
        ImageUtils.setUserPhoto(context, photoUrl, mUserPhoto);
        mDisplayedName.setText(username);
        if (birthday != null) mBirthday.setText(birthday);
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
        updateValues.put("/" + ApplicationHelper.USER_BIRTHDAY_IDENTIFIER, birthday);
        updateValues.put("/" + ApplicationHelper.USER_PHOTO_URL_IDENTIFIER, photoUrl);
        updateValues.put("/" + ApplicationHelper.USER_NAME_IDENTIFIER, username);
        updateValues.put("/" + ApplicationHelper.USER_GENDER_IDENTIFIER, gender);
        mUserDatabaseReference.updateChildren(updateValues);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        mCurrentUser.setGender((String) adapterView.getItemAtPosition(position));
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
                    deleteAccount();
                }
            };
            View dialogViewDelete = LayoutInflater.from(this)
                    .inflate(R.layout.delete_account, null, false);
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
        deleteRelatedDatabaseData();
        deleteRelatedStorageData();
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
                "/" + ApplicationHelper.USER_BIRTHDAY_IDENTIFIER, mCurrentUser.getBirthday()
        );
        updateValues.put(
                "/" + ApplicationHelper.USER_PHOTO_URL_IDENTIFIER, mCurrentUser.getPhotoUrl()
        );
        updateValues.put(
                "/" + ApplicationHelper.USER_NAME_IDENTIFIER, mCurrentUser.getUsername()
        );
        updateValues.put(
                "/" + ApplicationHelper.USER_GENDER_IDENTIFIER, mCurrentUser.getGender()
        );
        updateValues.put(
                "/" + ApplicationHelper.USER_EMAIL_VERIFICATION_IDENTIFIER,
                mCurrentUser.getIsEmailVerified()
        );
        mUserDatabaseReference.updateChildren(updateValues);
        mUserDatabaseReference.child(ApplicationHelper.USER_TOKENS_IDENTIFIER)
                .setValue(mCurrentUser.getTokensMap());
    }

    private void deleteRelatedDatabaseData() {
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("/" + mFirebaseUser.getUid(), null);
        mUsersRootDatabaseReference.updateChildren(updateValues);
        ApplicationHelper.deleteSingleConnection(mFirebaseUser.getUid());
    }

    private void deleteRelatedStorageData() {
        String currentPhotoUrl = mCurrentUser.getPhotoUrl();
        if (currentPhotoUrl != null && currentPhotoUrl.contains(FIREBASE_PHOTO_IDENTIFIER)) {
            mUserProfilePictureStorageReference
                    = mFirebaseStorage.getReferenceFromUrl(currentPhotoUrl);
            mUserProfilePictureStorageReference.delete();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public DatePickerFragment(){}

        private Calendar mActualCalendar;
        private User mCurrentUser;
        private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle receivedData = getArguments();
            mCurrentUser = receivedData.getParcelable(KEY_USER);
            final TextView birthdayTextView = getActivity().findViewById(R.id.tv_settings_birthday);
            final DatabaseReference userRef = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.USERS_NODE).child(mCurrentUser.getUid());
            mActualCalendar = Calendar.getInstance();
            int year = mActualCalendar.get(Calendar.YEAR);
            int month = mActualCalendar.get(Calendar.MONTH);
            int day = mActualCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                    android.app.AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);
            dialog.setButton(
                    DialogInterface.BUTTON_NEUTRAL,
                    getResources().getString(R.string.settings_birthday_set_default),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String birthday = getResources().getString(R.string.settings_birthday_default);
                    mCurrentUser.setBirthday(birthday);
                    Map<String, Object> updateValues = new HashMap<>();
                    updateValues.put("/" + ApplicationHelper.USER_BIRTHDAY_IDENTIFIER, birthday);
                    userRef.updateChildren(updateValues);
                    birthdayTextView.setText(birthday);
                }
            });
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            final TextView birthdayTextView = getActivity().findViewById(R.id.tv_settings_birthday);
            final DatabaseReference userRef = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.USERS_NODE).child(mCurrentUser.getUid());
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year, month, day);
            if (newCalendar.getTime().after(mActualCalendar.getTime())) {
                Toast.makeText(
                        getContext(),
                        getResources().getString(R.string.wrong_date_selected_message),
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
                updateValues.put("/" + ApplicationHelper.USER_BIRTHDAY_IDENTIFIER, date);
                userRef.updateChildren(updateValues);
                birthdayTextView.setText(date);
            }
        }
    }
}
