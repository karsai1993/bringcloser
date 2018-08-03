package karsai.laszlo.bringcloser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.model.Thought;
import karsai.laszlo.bringcloser.model.Wish;
import timber.log.Timber;

/**
 * Created by Laci on 28/05/2018.
 * Class to help the application by the common fiels
 */

public class ApplicationHelper {

    public static final String USER_NAME_IDENTIFIER = "username";
    public static final String USER_EMAIL_VERIFICATION_IDENTIFIER = "isEmailVerified";
    public static final String USER_PHOTO_URL_IDENTIFIER = "photoUrl";
    public static final String USER_BIRTHDAY_IDENTIFIER = "birthday";
    public static final String USER_TOKENS_IDENTIFIER = "tokensMap";
    public static final String USER_GENDER_IDENTIFIER = "gender";
    public static final String USER_KEY = "user";
    public static final String CONNECTION_KEY = "connection";
    public static final String USERS_NODE = "users";
    public static final String CONNECTIONS_NODE = "connections";
    public static final String MESSAGES_NODE = "messages";
    public static final String WISHES_NODE = "wishes";
    public static final String EVENTS_NODE = "events";
    public static final String THOUGHTS_NODE = "thoughts";
    public static final String CHAT_VISIBILITY_NODE = "chat_visibility";
    public static final String CHAT_TYPING_NODE = "chat_typing";
    public static final String CONNECTION_FROM_UID_IDENTIFIER = "fromUid";
    public static final String CONNECTION_TO_UID_IDENTIFIER = "toUid";
    public static final String OBJECT_TEXT_IDENTIFIER = "text";
    public static final String OBJECT_HAS_ARRIVED_IDENTIFIER = "hasArrived";
    public static final String CONNECTION_CONNECTED_IDENTIFIER = "connectionBit";
    public static final String CONNECTION_TIMESTAMP_IDENTIFIER = "timestamp";
    public static final String INTENT_CHOSEN_USER_KEY = "chosen_user";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_REQUEST = "action_request";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION = "action_connection";
    public static final String NOTIFICATION_INTENT_ACTION_WISH = "action_wish";
    public static final String NOTIFICATION_INTENT_ACTION_EVENT = "action_event";
    public static final String NOTIFICATION_INTENT_ACTION_THOUGHT = "action_thought";
    public static final String NOTIFICATION_INTENT_ACTION_MESSAGE = "action_message";
    public static final String NEW_SENT_REQUEST_INTENT_ACTION_PAGE_CONNECTION = "action_sent_rq";
    public static final String SAVE_RECYCLERVIEW_POS_KEY = "save_recyclerview_pos";
    public static final String FULL_DATE_PATTERN = "yyyyMMddHHmmss";
    public static final String DISPLAY_DATE_PATTERN = "yyyy MMM dd HH:mm";
    public static final String CONNECTION_DETAIL_KEY = "connection_detail";
    public static final String STORAGE_MESSAGE_IMAGES_FOLDER = "message_images";
    public static final String STORAGE_WISH_IMAGES_FOLDER = "wish_images";
    public static final String STORAGE_EVENT_IMAGES_FOLDER = "event_images";
    public static final String STORAGE_THOUGHT_IMAGES_FOLDER = "thought_images";
    public static final String EXTRA_DATA = "extra_data";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_ID = "extra_id";
    public static final String TAG_DATA_PICKER = "data_picker";
    public static final String TAG_TIME_PICKER = "time_picker";
    public static final String SERVICE_TYPE_IDENTIFIER = "service_type_identifier";
    public static final String SERVICE_TYPE_WISH = "service_type_wish";
    public static final String SERVICE_TYPE_EVENT = "service_type_event";
    public static final String SERVICE_CONTENT_FROM_IDENTIFIER = "service_content_from_identifier";
    public static final String SERVICE_CONTENT_TO_IDENTIFIER = "service_content_to_identifier";
    public static final String SERVICE_CONTENT_KEY_IDENTIFIER = "service_content_key_identifier";
    public static final String TYPE_WISH_IDENTIFIER = "type_wish_identifier";
    public static final String TYPE_EVENT_IDENTIFIER = "type_event_identifier";
    public static final String TYPE_THOUGHT_IDENTIFIER = "type_thought_identifier";

    public static final String UPDATE_WIDGET_KEY = "update_widget";
    public static final String FROM_WIDGET_POS_KEY = "from_widget_pos";

    public static final String RECEIVED_DETAIL_FROM_UID_IDENTIFIER = "fromUid";
    public static final String RECEIVED_DETAIL_CONNECTION_FROM_UID_IDENTIFIER = "connectionFromUid";
    public static final String RECEIVED_DETAIL_CONNECTION_TO_UID_IDENTIFIER = "connectionToUid";
    public static final String RECEIVED_DETAIL_EXTRA_PHOTO_URL_IDENTIFIER = "extraPhotoUrl";
    public static final String RECEIVED_DETAIL_TIMESTAMP_IDENTIFIER = "timestamp";
    public static final String RECEIVED_DETAIL_WHEN_TO_ARRIVE_IDENTIFIER = "whenToArrive";
    public static final String RECEIVED_DETAIL_OCCASION_IDENTIFIER = "occasion";
    public static final String RECEIVED_DETAIL_TITLE_IDENTIFIER = "title";
    public static final String RECEIVED_DETAIL_PLACE_IDENTIFIER = "place";
    public static final String RECEIVED_DETAIL_TEXT_IDENTIFIER = "text";
    public static final String RECEIVED_DETAIL_HAS_ARRIVED_IDENTIFIER = "hasArrived";
    public static final String RECEIVED_DETAIL_KEY_IDENTIFIER = "key";

    public static final int CONNECTION_BIT_POS = 1;
    public static final int CONNECTION_BIT_NEG = 0;
    public static final String USER_UID_IDENTIFIER = "uid";

    public static String getServiceUniqueTag(String fromUid, String toUid, String key) {
        return new StringBuilder()
                .append(fromUid)
                .append("_")
                .append(toUid)
                .append("_")
                .append(key)
                .toString();
    }

    public static String getCurrentUTCDateAndTime() {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(currentDate);
    }

    public static String getUTCDateAndTime(Context context, String dateAndTimeToConvert) {
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        Date date;
        try {
            date = sdf.parse(dateAndTimeToConvert);
        } catch (ParseException e) {
            return context.getResources().getString(R.string.data_not_available);
        }
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String getLocalDateAndTimeToDisplay(Context context, String dateAndTimeToConvert) {
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = sdf.parse(dateAndTimeToConvert);
        } catch (ParseException e) {
            return context.getResources().getString(R.string.data_not_available);
        }
        SimpleDateFormat displaySimpleDateFormat = new SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.getDefault());
        displaySimpleDateFormat.setTimeZone(TimeZone.getDefault());
        return displaySimpleDateFormat.format(date);
    }

    public static Date getDateAndTime(String dateAndTimeToConvert) {
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        try {
            return sdf.parse(dateAndTimeToConvert);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isSent(Object object) {
        boolean hasArrived;
        String targetDateAndTimeAsText;
        boolean isExpired;
        if (object instanceof Wish) {
            Wish wish = (Wish) object;
            hasArrived = wish.hasArrived();
            targetDateAndTimeAsText = wish.getWhenToArrive();
        } else if (object instanceof Event) {
            Event event = (Event) object;
            hasArrived = event.hasArrived();
            targetDateAndTimeAsText = event.getWhenToArrive();
        } else {
            return false;
        }
        if (hasArrived) return true;
        Date targetDateAndTime = getDateAndTime(targetDateAndTimeAsText);
        if (targetDateAndTime == null) {
            Timber.wtf("target date - get date and time problem occurred");
            return false;
        }
        String currentDateAndTimeAsText = getCurrentUTCDateAndTime();
        Date currentDateAndTime = getDateAndTime(currentDateAndTimeAsText);
        if (currentDateAndTime == null) {
            Timber.wtf("current date - get date and time problem occurred");
            return false;
        }
        isExpired = currentDateAndTime.after(targetDateAndTime);
        return isExpired;
    }

    public static void deleteImageFromStorage(final Context context, String url) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imageStorageReference;
        if (url != null && !url.isEmpty()) {
            imageStorageReference
                    = firebaseStorage.getReferenceFromUrl(url);
            imageStorageReference.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(
                            context,
                            context.getResources().getString(R.string.problem),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }

    public static List<ChatDetail> getDateInfoNextToMessageData(
            Context context,
            List<MessageDetail> messageDetailList) {
        List<ChatDetail> chatDetailList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        for (MessageDetail messageDetail : messageDetailList) {
            String dateAndTimeAsText = messageDetail.getTimestamp();
            String dateAndTimeLocal = ApplicationHelper.getLocalDateAndTimeToDisplay(
                    context,
                    dateAndTimeAsText
            );
            String [] parts = dateAndTimeLocal.split(" ");
            String dateAsText = parts[0] + " " + parts[1] + " " + parts[2];
            if (!dateList.contains(dateAsText)) {
                dateList.add(dateAsText);
                ChatDetail currDate = new ChatDetail(dateAsText);
                chatDetailList.add(currDate);
            }
            ChatDetail currMessage = new ChatDetail(messageDetail, dateAndTimeLocal);
            chatDetailList.add(currMessage);
        }
        return chatDetailList;
    }

    public static String getPersonalizedRelationshipType(
            Context context,
            String type,
            String toGender,
            String fromGender,
            boolean isPairItemShouldBeReversed) {
        Resources res = context.getResources();
        if (type.equals(res.getString(R.string.relationship_type_other)))
            return res.getString(R.string.relationship_type_acquaintanceship);
        else if (type.equals(res.getString(R.string.relationship_type_lover)) ||
                type.equals(res.getString(R.string.relationship_type_friend)) ||
                type.equals(res.getString(R.string.relationship_type_cousin)))
            return type;
        else if (type.equals(res.getString(R.string.relationship_type_uncle))) {
            if (!isPairItemShouldBeReversed) return type;
            else {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_nephew);
                else return res.getString(R.string.relationship_type_niece);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_aunt))) {
            if (!isPairItemShouldBeReversed) return type;
            else {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_nephew);
                else return res.getString(R.string.relationship_type_niece);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_nephew))) {
            if (!isPairItemShouldBeReversed) return type;
            else {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_uncle);
                else return res.getString(R.string.relationship_type_aunt);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_niece))) {
            if (!isPairItemShouldBeReversed) return type;
            else {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_uncle);
                else return res.getString(R.string.relationship_type_aunt);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_sibling))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none))) return type;
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_sibling_male);
                else return res.getString(R.string.relationship_type_sibling_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none))) return type;
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_sibling_male);
                else return res.getString(R.string.relationship_type_sibling_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_parent))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_child);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_child_male);
                else return res.getString(R.string.relationship_type_child_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_parent);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_parent_male);
                else return res.getString(R.string.relationship_type_parent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_godparent))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_godchild);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_godchild_male);
                else return res.getString(R.string.relationship_type_godchild_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_godparent);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_godparent_male);
                else return res.getString(R.string.relationship_type_godparent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_grandparent))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_grandchild);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_grandchild_male);
                else return res.getString(R.string.relationship_type_grandchild_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_grandparent);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_grandparent_male);
                else return res.getString(R.string.relationship_type_grandparent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_child))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_parent);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_parent_male);
                else return res.getString(R.string.relationship_type_parent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_child);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_child_male);
                else return res.getString(R.string.relationship_type_child_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_godchild))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_godparent);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_godparent_male);
                else return res.getString(R.string.relationship_type_godparent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_godchild);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_godchild_male);
                else return res.getString(R.string.relationship_type_godchild_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_grandchild))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_grandparent);
                else if (fromGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_grandparent_male);
                else return res.getString(R.string.relationship_type_grandparent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none)))
                    return res.getString(R.string.relationship_type_grandchild);
                else if (toGender.equals(res.getString(R.string.gender_male)))
                    return res.getString(R.string.relationship_type_grandchild_male);
                else return res.getString(R.string.relationship_type_grandchild_female);
            }
        } else return "";
    }

    public static void deletePairConnection(String fromUid, final String toUid, final Context context, final String name) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef
                = firebaseDatabase.getReference().child(CONNECTIONS_NODE);
        connectionsRef.orderByChild(CONNECTION_FROM_UID_IDENTIFIER).equalTo(fromUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            if (key == null) {
                                Timber.wtf("key null delete pair connection");
                                continue;
                            }
                            String toUidValue = dataSnapshot
                                    .child(key)
                                    .child(CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class);
                            if (toUidValue == null) {
                                Timber.wtf("to uid null delete pair connection");
                                continue;
                            }
                            if (toUidValue.equals(toUid)) {
                                connectionsRef.child(key).setValue(null);
                            }
                        }
                        Toast.makeText(
                                context,
                                new StringBuilder()
                                        .append(context
                                                .getResources()
                                                .getString(
                                                        R.string.request_deleted_1)
                                        ).append(" ")
                                        .append(name)
                                        .append(" ")
                                        .append(context
                                                .getResources()
                                                .getString(
                                                        R.string.request_deleted_2))
                                        .append("!")
                                        .toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static void saveValueToPrefs(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static String getValueFromPrefs(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}
