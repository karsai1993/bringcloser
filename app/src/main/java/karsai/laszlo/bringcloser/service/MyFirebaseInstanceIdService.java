package karsai.laszlo.bringcloser.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import karsai.laszlo.bringcloser.ApplicationHelper;

/**
 * Created by Laci on 10/06/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        ApplicationHelper.saveTokenToPrefs(
                getApplicationContext(),
                ApplicationHelper.TOKEN_NEW_STORAGE_KEY,
                refreshedToken
        );
    }
}
