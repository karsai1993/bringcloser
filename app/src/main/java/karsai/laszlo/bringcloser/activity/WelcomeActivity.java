package karsai.laszlo.bringcloser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.NetworkUtils;

/**
 * Created by Laci on 24/06/2018.
 */

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFirebaseUser;
    private GoogleApiAvailability mGoogleApiAvailability;

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.no_internet_message),
                    Toast.LENGTH_LONG
            ).show();
            finish();
        } else {
            mGoogleApiAvailability = GoogleApiAvailability.getInstance();
            checkAndActPlayServices();
            mFirebaseAuth = FirebaseAuth.getInstance();
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    mFirebaseUser = firebaseAuth.getCurrentUser();
                    if (mFirebaseUser != null) {
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(true)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                                new AuthUI.IdpConfig.GoogleBuilder().build())
                                        ).build(),
                                RC_SIGN_IN);
                    }
                }
            };
        }
    }

    private void checkAndActPlayServices() {
        if (!ApplicationHelper.checkPlayServices(this, mGoogleApiAvailability)) {
            mGoogleApiAvailability.makeGooglePlayServicesAvailable(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getResources().getString(R.string.play_services_alert),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndActPlayServices();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
