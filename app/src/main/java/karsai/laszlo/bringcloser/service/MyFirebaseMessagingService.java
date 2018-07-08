package karsai.laszlo.bringcloser.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import karsai.laszlo.bringcloser.ApplicationHelper;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.utils.NotificationUtils;

/**
 * Created by Laci on 10/06/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String REQUEST_IDENTIFIER = "request";
    private static final String APPROVAL_IDENTIFIER = "approval";
    private static final int REQUEST_NOTIFICATION_ID = 1;
    private static final int APPROVAL_NOTIFICATION_ID = 2;

    private static final String TYPE_KEY = "type";
    private static final String NAME_KEY = "name";
    private static final String PHOTO_URL_KEY = "photoUrl";

    private static final String NO_DATA = "null";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String type = data.get(TYPE_KEY);
        String name = data.get(NAME_KEY);
        String photoUrl = data.get(PHOTO_URL_KEY);
        photoUrl = !photoUrl.equals(NO_DATA) ? photoUrl : null;
        String title = "";
        String body = "";
        int notificationId = 0;
        String action = "";

        if (type.equals(REQUEST_IDENTIFIER)) {
            title = getApplicationContext().getResources().getString(R.string.new_request_title);
            body = new StringBuilder()
                    .append(name)
                    .append(" ")
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_request_body)
                    ).toString();
            notificationId = REQUEST_NOTIFICATION_ID;
            action = ApplicationHelper.NOTIFICATION_INTENT_ACTION_PAGE_REQUEST;
        } else if (type.equals(APPROVAL_IDENTIFIER)) {
            title = getApplicationContext().getResources().getString(R.string.new_approval_title);
            body = new StringBuilder()
                    .append(name)
                    .append(" ")
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_approval_body)
                    ).toString();
            notificationId = APPROVAL_NOTIFICATION_ID;
            action = ApplicationHelper.NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION;
        }

        NotificationUtils.addNotification(
                getApplicationContext(),
                title,
                body,
                photoUrl,
                notificationId,
                action
        );
        super.onMessageReceived(remoteMessage);
    }
}
