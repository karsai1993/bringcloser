package karsai.laszlo.bringcloser.service;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.User;
import karsai.laszlo.bringcloser.utils.NotificationUtils;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 10/06/2018.
 * Service to handle messaging and token refresh happenings
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String REQUEST_IDENTIFIER = "request";
    private static final String APPROVAL_IDENTIFIER = "approval";
    private static final String WISH_IDENTIFIER = "wish";
    private static final String EVENT_IDENTIFIER = "event";
    private static final String THOUGHT_IDENTIFIER = "thought";
    private static final String MESSAGE_IDENTIFIER = "message";
    private static final String PRIVACY_CHANGED_IDENTIFIER = "privacy";
    private static final String TERMS_CHANGED_IDENTIFIER = "terms";
    private static final String UNUSED_IDENTIFIER = "unused";
    private static final int REQUEST_NOTIFICATION_ID = 1;
    private static final int APPROVAL_NOTIFICATION_ID = 2;
    private static final int WISH_NOTIFICATION_ID = 3;
    private static final int EVENT_NOTIFICATION_ID = 4;
    private static final int THOUGHT_NOTIFICATION_ID = 5;
    private static final int PRIVACY_CHANGED_NOTIFICATION_ID = 6;
    private static final int TERMS_CHANGED_NOTIFICATION_ID = 7;
    private static final int UNUSED_NOTIFICATION_ID = 8;

    private int mMessageNotificationId;

    private static final String TYPE_KEY = "type";
    private static final String NAME_KEY = "name";
    private static final String PHOTO_URL_KEY = "photoUrl";
    private static final String CONTENT_KEY = "content";
    private static final String EXTRA_PHOTO_URL_KEY = "extraPhotoUrl";
    private static final String FROM_UID_KEY = "fromUid";
    private static final String FROM_GENDER_KEY = "fromGender";
    private static final String FROM_BIRTHDAY_KEY = "fromBirthday";
    private static final String TO_UID_KEY = "toUid";
    private static final String TO_NAME_KEY = "toName";
    private static final String TO_PHOTO_URL_KEY = "toPhotoUrl";
    private static final String TO_GENDER_KEY = "toGender";
    private static final String TO_BIRTHDAY_KEY = "toBirthday";
    private static final String CONNECTION_TYPE_KEY = "connectionType";
    private static final String TIMESTAMP_KEY = "timestamp";

    //private static final String SHOULD_RECEIVE_KEY = "shouldReceive";
    //private static final String SHOULD_RECEIVE_DEFAULT = "all";

    private static final String NOTIFICATION_ID_GENERAL_KEY = "notification_id_general";

    private static final String NO_DATA_UNDEFINED = "undefined";
    private static final String NO_DATA_NULL = "null";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        String type = data.get(TYPE_KEY);
        String name = data.get(NAME_KEY);
        String photoUrl = data.get(PHOTO_URL_KEY);
        photoUrl = photoUrl != null && !photoUrl.equals(NO_DATA_NULL) ? photoUrl : null;
        String content = data.get(CONTENT_KEY);
        String extraPhotoUrl = data.get(EXTRA_PHOTO_URL_KEY);
        String fromUid = data.get(FROM_UID_KEY);
        String fromGender = data.get(FROM_GENDER_KEY);
        String fromBirthday = data.get(FROM_BIRTHDAY_KEY);
        String toUid = data.get(TO_UID_KEY);
        String toName = data.get(TO_NAME_KEY);
        String toPhotoUrl = data.get(TO_PHOTO_URL_KEY);
        String toGender = data.get(TO_GENDER_KEY);
        String toBirthday = data.get(TO_BIRTHDAY_KEY);
        String connectionType = data.get(CONNECTION_TYPE_KEY);
        String timestamp = data.get(TIMESTAMP_KEY);
        //String shouldReceiveUid = data.get(SHOULD_RECEIVE_KEY);

        String title = "";
        String body = "";
        int notificationId = 0;
        String action = "";
        ConnectionDetail connectionDetail = null;

        if (type.equals(REQUEST_IDENTIFIER)) {
            title = getApplicationContext().getResources().getString(R.string.new_request_title);
            body = new StringBuilder()
                    .append(name)
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_request_body)
                    ).toString();
            notificationId = REQUEST_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_REQUEST;
        } else if (type.equals(APPROVAL_IDENTIFIER)) {
            title = getApplicationContext().getResources().getString(R.string.new_approval_title);
            body = new StringBuilder()
                    .append(name)
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_approval_body)
                    ).toString();
            notificationId = APPROVAL_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION;
        } else if (type.equals(WISH_IDENTIFIER)) {
            title = new StringBuilder()
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_wish_title)
                    ).append(name)
                    .toString();
            body = content;
            notificationId = WISH_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_WISH;
        } else if (type.equals(EVENT_IDENTIFIER)) {
            title = new StringBuilder()
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_event_title)
                    ).append(name)
                    .toString();
            body = content;
            notificationId = EVENT_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_EVENT;
        } else if (type.equals(THOUGHT_IDENTIFIER)) {
            title = new StringBuilder()
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_thought_title)
                    ).append(name)
                    .toString();
            body = content;
            notificationId = THOUGHT_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_THOUGHT;
        } else if (type.equals(MESSAGE_IDENTIFIER)) {
            title = new StringBuilder()
                    .append(
                            getApplicationContext()
                                    .getResources()
                                    .getString(R.string.new_message_title)
                    ).append(name)
                    .toString();
            if (extraPhotoUrl.equals(NO_DATA_UNDEFINED)) {
                body = content;
            } else {
                body = getApplicationContext().getResources().getString(R.string.new_message_image_body);
            }
            String uniqueNotificationIdKey = fromUid + "-" + toUid;
            String storedUniqueId = ApplicationUtils.getValueFromPrefs(
                    getApplicationContext(),
                    uniqueNotificationIdKey
            );
            if (storedUniqueId == null) {
                String storedGeneralId = ApplicationUtils.getValueFromPrefs(
                        getApplicationContext(),
                        NOTIFICATION_ID_GENERAL_KEY);
                if (storedGeneralId == null) {
                    mMessageNotificationId = 11;
                } else {
                    mMessageNotificationId = Integer.parseInt(storedGeneralId) + 1;
                }
                ApplicationUtils.saveValueToPrefs(
                        getApplicationContext(),
                        NOTIFICATION_ID_GENERAL_KEY,
                        String.valueOf(mMessageNotificationId)
                );
                ApplicationUtils.saveValueToPrefs(
                        getApplicationContext(),
                        uniqueNotificationIdKey,
                        String.valueOf(mMessageNotificationId)
                );
            } else {
                mMessageNotificationId = Integer.parseInt(storedUniqueId);
            }

            notificationId = mMessageNotificationId;
            action = ApplicationUtils.NOTIFICATION_INTENT_ACTION_MESSAGE;
            connectionDetail = new ConnectionDetail(
                    fromUid,
                    name,
                    fromGender,
                    photoUrl,
                    fromBirthday,
                    toUid,
                    toName,
                    toGender,
                    toPhotoUrl,
                    toBirthday,
                    connectionType,
                    1,
                    timestamp
            );
        } else if (type.equals(PRIVACY_CHANGED_IDENTIFIER)) {
            title = getApplicationContext()
                    .getResources()
                    .getString(R.string.privacy_changed_title);
            body = getApplicationContext()
                    .getResources()
                    .getString(R.string.doc_changed_message);
            notificationId = PRIVACY_CHANGED_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_PRIVACY;
        } else if (type.equals(TERMS_CHANGED_IDENTIFIER)) {
            title = getApplicationContext()
                    .getResources()
                    .getString(R.string.terms_changed_title);
            body = getApplicationContext()
                    .getResources()
                    .getString(R.string.doc_changed_message);
            notificationId = TERMS_CHANGED_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_TERMS;
        } else if (type.equals(UNUSED_IDENTIFIER)) {
            title = getApplicationContext().getResources().getString(R.string.unused_data_notification_title);
            body = getApplicationContext().getResources().getString(R.string.unused_data_notification_body);
            notificationId = UNUSED_NOTIFICATION_ID;
            action = ApplicationUtils.NOTIFICATION_INTENT_UNUSED;
        }

        /*String currentUid = FirebaseAuth.getInstance().getUid();
        if (shouldReceiveUid.equals(SHOULD_RECEIVE_DEFAULT)
                || (currentUid != null && currentUid.equals(shouldReceiveUid))) {
            NotificationUtils.addFunctionsNotification(
                    getApplicationContext(),
                    title,
                    body,
                    photoUrl,
                    notificationId,
                    action,
                    connectionDetail
            );
        }*/
        NotificationUtils.addFunctionsNotification(
                getApplicationContext(),
                title,
                body,
                photoUrl,
                notificationId,
                action,
                connectionDetail
        );
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String token) {
        updateTokenInDatabase(token);
    }

    private void updateTokenInDatabase(String token) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child(ApplicationUtils.USERS_NODE);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            databaseReference
                    .child(uid)
                    .child(ApplicationUtils.USER_TOKENS_IDENTIFIER)
                    .child(token)
                    .setValue(true);
        }
    }
}
