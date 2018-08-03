package karsai.laszlo.bringcloser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import karsai.laszlo.bringcloser.R;

/**
 * Activity to show the details about the application
 */
public class AboutActivity extends CommonActivity {

    @BindView(R.id.tv_no_internet)
    TextView mNoInternetAlertTextView;
    @BindView(R.id.tv_version)
    TextView mVersion;

    private static final String LICENSE_URL_FIREBASE = "https://developer.android.com/studio/terms";
    private static final String LICENSE_URL_FIREBASE_UI
            = "https://github.com/firebase/FirebaseUI-Android/blob/master/LICENSE";
    private static final String LICENSE_URL_GLIDE
            = "https://github.com/bumptech/glide/blob/master/LICENSE";
    private static final String LICENSE_URL_BUTTERKNIFE
            = "https://github.com/JakeWharton/butterknife/blob/master/LICENSE.txt";
    private static final String LICENSE_URL_FASTSCROLLER
            = "https://github.com/FutureMind/recycler-fast-scroll/blob/master/LICENSE";
    private static final String LICENSE_URL_FACEBOOK_LOGIN
            = "https://github.com/facebook/facebook-android-sdk/blob/master/LICENSE.txt";
    private static final String LICENSE_URL_JOBDISPATCHER
            = "https://github.com/firebase/firebase-jobdispatcher-android/blob/master/LICENSE";
    private static final String LICENSE_URL_TIMBER
            = "https://github.com/JakeWharton/timber/blob/master/LICENSE.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        try {
            String version = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(
                            getApplicationContext().getPackageName(),
                            0
                    ).versionName;
            mVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            mVersion.setVisibility(View.GONE);
        }
    }

    @Override
    protected TextView getNoInternetAlertTextView() {
        return mNoInternetAlertTextView;
    }

    public void onOpenBrowserClick(View view) {
        String url = "";
        switch (view.getId()) {
            case R.id.iv_about_open_firebase:
                url = LICENSE_URL_FIREBASE;
                break;
            case R.id.iv_about_open_firebase_ui:
                url = LICENSE_URL_FIREBASE_UI;
                break;
            case R.id.iv_about_open_glide:
                url = LICENSE_URL_GLIDE;
                break;
            case R.id.iv_about_open_butterknife:
                url = LICENSE_URL_BUTTERKNIFE;
                break;
            case R.id.iv_about_open_facebook_login:
                url = LICENSE_URL_FACEBOOK_LOGIN;
                break;
            case R.id.iv_about_open_fastscroller:
                url = LICENSE_URL_FASTSCROLLER;
                break;
            case R.id.iv_about_open_firebase_jobdispatcher:
                url = LICENSE_URL_JOBDISPATCHER;
                break;
            case R.id.iv_about_open_timber:
                url = LICENSE_URL_TIMBER;
                break;
            case R.id.iv_about_open_icons8:
                url = getResources().getString(R.string.about_icons_content_icons8);
                break;
            case R.id.iv_about_open_google_icons:
                url = getResources().getString(R.string.about_icons_content_google_icons);
                break;
            default:
                break;
        }
        if (!url.isEmpty()) {
            openWebPage(url);
        }
    }

    private void openWebPage (String url) {
        Uri webPage = Uri.parse(url);
        Intent openWebPageIntent = new Intent(Intent.ACTION_VIEW, webPage);
        if (openWebPageIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(openWebPageIntent);
        }
    }
}
