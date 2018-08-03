package karsai.laszlo.bringcloser.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity to perform adding a new thought
 */
public class AddNewThoughtActivity extends CommonActivity {

    @BindView(R.id.et_thought_message)
    TextInputEditText mMessageEditText;
    @BindView(R.id.tv_thought_question)
    TextView mQuestionTextView;
    @BindView(R.id.tv_thought_status)
    TextView mStatusTextView;
    @BindView(R.id.switch_thought)
    Switch mStatusSwitch;
    @BindView(R.id.iv_thought_add_photo_from_camera)
    ImageView mCameraPhotoImageView;
    @BindView(R.id.iv_thought_add_photo_from_gallery)
    ImageView mGalleryPhotoImageView;
    @BindView(R.id.tv_thought_photo_alert)
    TextView mNoPhotoAlertTextView;
    @BindView(R.id.iv_thought_added_extra_photo)
    ImageView mAddedExtraPhotoImageView;
    @BindView(R.id.iv_thought_added_extra_photo_delete)
    ImageView mDeleteExtraPhotoImageView;
    @BindView(R.id.ll_thought_add_extra_photo)
    LinearLayout mAddPhotoLinearLayout;
    @BindView(R.id.fab_approve_add_new_thought)
    FloatingActionButton mApproveFab;
    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;

    private ConnectionDetail mConnectionDetail;
    private String mOtherUid;
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
    private DatabaseReference mThoughtsDatabaseRef;
    private String mCurrentUserUid;
    private Bundle mPausedData;
    private Bundle mSavedInstanceState;
    private String mChosenPhotoUrl;

    private final static String SAVE_PHOTO_URL = "photo_url";
    private final static String SAVE_EXTRA_VISIBILITY = "extra_visibility";
    private final static String SAVE_MESSAGE_EDIT_TEXT_FOCUSED = "message_edit_text_focused";
    private final static String SAVE_MESSAGE_EDIT_TEXT_CONTENT = "message_edit_text_content";
    private final static String SAVE_IMAGE_REF = "image_ref";
    private final static String SAVE_SWITCH_STATUS = "switch_status";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_add_new_thought);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

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
                    if (user == null) {
                        Timber.wtf("user null getting user information for new thought");
                        return;
                    }
                    loadData(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mFirebaseStorage = FirebaseStorage.getInstance();
            mImagesRootRef = mFirebaseStorage.getReference()
                    .child(mCurrentUserUid)
                    .child(ApplicationHelper.STORAGE_THOUGHT_IMAGES_FOLDER);
            mImagesRef = mImagesRootRef;
            mConnectionsDatabaseReference = mFirebaseDatabase.getReference()
                    .child(ApplicationHelper.CONNECTIONS_NODE);

            mGalleryPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromFile(AddNewThoughtActivity.this);
                }
            });
            mCameraPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSnackbarView = view;
                    ImageUtils.onClickFromCamera(AddNewThoughtActivity.this);
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
                                    AddNewThoughtActivity.this,
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
                    if (mMessageEditText.getText().toString().isEmpty()) {
                        Snackbar.make(
                                view,
                                getString(R.string.thought_alert_no_message),
                                Snackbar.LENGTH_LONG
                        ).show();
                    } else {
                        if (mAddPhotoLinearLayout.getVisibility() == View.VISIBLE) {
                            mImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String photoUrl = uri.toString();
                                    createThoughtItem(photoUrl);
                                }
                            });
                        } else {
                            createThoughtItem(null);
                        }
                    }
                }
            });
            mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        mStatusTextView.setText(getResources().getString(R.string.thought_status_yes));
                    } else {
                        mStatusTextView.setText(getResources().getString(R.string.thought_status_no));
                    }
                }
            });
            mMessageEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mStatusSwitch.requestFocus();
                    } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        onSupportNavigateUp();
                    }
                    return false;
                }
            });
        } else {
            Timber.wtf("new thought received data null problem");
        }
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
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

    private void createThoughtItem(final String photoUrl) {
        final String timestamp = ApplicationHelper.getCurrentUTCDateAndTime();
        mConnectionsQuery = mConnectionsDatabaseReference
                .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
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
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class);
                    if (toUidValue == null) {
                        Timber.wtf("to uid not found connection to");
                        continue;
                    }
                    if (toUidValue.equals(mConnectionDetail.getToUid())) {
                        mThoughtsDatabaseRef = mConnectionsDatabaseReference
                                .child(key)
                                .child(ApplicationHelper.THOUGHTS_NODE);
                        DatabaseReference databaseReference
                                = mThoughtsDatabaseRef.push();
                        Thought thought = new Thought(
                                mCurrentUserUid,
                                mConnectionDetail.getFromUid(),
                                mConnectionDetail.getToUid(),
                                photoUrl,
                                timestamp,
                                mMessageEditText.getText().toString(),
                                mStatusSwitch.isChecked(),
                                databaseReference.getKey()
                        );
                        databaseReference.setValue(thought).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageUtils.onRequestPermissionResult(
                AddNewThoughtActivity.this,
                requestCode,
                grantResults
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final StorageReference tempRef = mImagesRef;
        mImagesRef = mImagesRootRef.child(
                ApplicationHelper.getCurrentUTCDateAndTime()
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
                                            AddNewThoughtActivity.this,
                                            prevPhotoUrl
                                    );
                                    mNoPhotoAlertTextView.setVisibility(View.GONE);
                                    mAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                                    ImageUtils.setPhoto(
                                            AddNewThoughtActivity.this,
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
                                    AddNewThoughtActivity.this,
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
                AddNewThoughtActivity.this,
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
                        .append(getString(R.string.thought_question_1))
                        .append(name)
                        .append(getString(R.string.thought_question_2))
                        .toString()
        );
        applyPauseOrRotationHandling(mPausedData);
        applyPauseOrRotationHandling(mSavedInstanceState);
        mPausedData = null;
        mSavedInstanceState = null;
    }

    private void applySwitchStatusHandler() {
        mStatusTextView.setText(
                mStatusSwitch.isChecked() ?
                        getResources().getString(R.string.thought_status_yes) :
                        getResources().getString(R.string.thought_status_no)
        );
    }

    private void applyPauseOrRotationHandling(Bundle bundle) {
        if (bundle != null) {
            String message = bundle.getString(SAVE_MESSAGE_EDIT_TEXT_CONTENT);
            mMessageEditText.setText(message);
            if (bundle.getBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED)) {
                mMessageEditText.requestFocus();
                mMessageEditText.setSelection(message != null ? message.length() : 0);
            }
            if (bundle.getBoolean(SAVE_SWITCH_STATUS)) {
                mStatusSwitch.setChecked(true);
            }
            boolean isExtraPhotoVisible = bundle.getBoolean(SAVE_EXTRA_VISIBILITY);
            if (isExtraPhotoVisible) {
                mNoPhotoAlertTextView.setVisibility(View.GONE);
                mAddPhotoLinearLayout.setVisibility(View.VISIBLE);
                mChosenPhotoUrl = bundle.getString(SAVE_PHOTO_URL);
                String imageRef = bundle.getString(SAVE_IMAGE_REF);
                if (imageRef == null) {
                    Timber.wtf("imageref not after pause or rotation");
                    return;
                }
                mImagesRef = mFirebaseStorage.getReference(imageRef);
                ImageUtils.setPhoto(
                        AddNewThoughtActivity.this,
                        mChosenPhotoUrl,
                        mAddedExtraPhotoImageView,
                        false
                );
            }
        } else {
            applySwitchStatusHandler();
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
        mPausedData.putString(SAVE_MESSAGE_EDIT_TEXT_CONTENT, mMessageEditText.getText().toString());
        mPausedData.putBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED, mMessageEditText.isFocused());
        mPausedData.putBoolean(SAVE_SWITCH_STATUS, mStatusSwitch.isChecked());
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
        outState.putString(SAVE_MESSAGE_EDIT_TEXT_CONTENT, mMessageEditText.getText().toString());
        outState.putBoolean(SAVE_MESSAGE_EDIT_TEXT_FOCUSED, mMessageEditText.isFocused());
        outState.putBoolean(SAVE_SWITCH_STATUS, mStatusSwitch.isChecked());
        boolean isExtraPhotoVisible = mAddPhotoLinearLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(SAVE_EXTRA_VISIBILITY, isExtraPhotoVisible);
        if (isExtraPhotoVisible) {
            outState.putString(SAVE_PHOTO_URL, mChosenPhotoUrl);
        }
        outState.putString(SAVE_IMAGE_REF, mImagesRef.getPath());
        super.onSaveInstanceState(outState);
    }
}
