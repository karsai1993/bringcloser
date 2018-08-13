package karsai.laszlo.bringcloser.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import karsai.laszlo.bringcloser.R;

import java.util.Arrays;

import karsai.laszlo.bringcloser.utils.NetworkUtils;

/**
 * Created by Laci on 24/06/2018.
 * Activity to start the app with respect to whether the user is signed in or not
 */
public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFirebaseUser;
    private GoogleApiAvailability mGoogleApiAvailability;

    private static final int RC_SIGN_IN = 123;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.mipmap.ic_launcher)
                                    .setTosAndPrivacyPolicyUrls(
                                            getResources().getString(R.string.terms_of_use),
                                            getResources().getString(R.string.privacy_policy)
                                    ).setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.FacebookBuilder().build())
                                    ).build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void checkAndActPlayServices() {
        if (!checkPlayServices(this, mGoogleApiAvailability)) {
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

    private boolean checkPlayServices(
            Context context,
            GoogleApiAvailability googleApiAvailability) {
        Activity activity = (Activity) context;
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(
                        activity,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST
                ).show();
            }
            return false;
        }
        return true;
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
        if (NetworkUtils.isNetworkAvailable(this)) {
            checkAndActPlayServices();
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        } else {
            setContentView(R.layout.no_internet_welcome);
            Button retryBtn = findViewById(R.id.btn_retry);
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recreate();
                }
            });
        }
    }
}
