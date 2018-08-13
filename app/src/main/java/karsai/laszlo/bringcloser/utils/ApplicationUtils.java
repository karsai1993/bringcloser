package karsai.laszlo.bringcloser.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.Event;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.model.UnusedPhotoDetail;
import karsai.laszlo.bringcloser.model.Wish;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 28/05/2018.
 * Class to help the application by the common fiels
 */

public class ApplicationUtils {

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
    public static final String UNUSED_NODE = "unused";
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
    public static final String NOTIFICATION_INTENT_PRIVACY = "action_privacy";
    public static final String NOTIFICATION_INTENT_TERMS = "action_terms";
    public static final String NOTIFICATION_INTENT_UNUSED = "action_unused";
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

    private static final String EMOJI_HEART_CODE = "<3";
    private static final String EMOJI_HEART_VALUE = "\u2764\ufe0f";
    private static final String EMOJI_HAPPY_WITH_HALO_CODE = "o:\\)";
    private static final String EMOJI_HAPPY_WITH_HALO_VALUE = "\uD83D\uDE07";
    private static final String EMOJI_HAPPY_WITH_HORNS_CODE = "3:\\)";
    private static final String EMOJI_HAPPY_WITH_HORNS_VALUE = "\uD83D\uDE08";
    private static final String EMOJI_RELIEVED_CODE = "u:\\)";
    private static final String EMOJI_RELIEVED_VALUE = "\uD83D\uDE0C";
    private static final String EMOJI_HAPPY_CODE = ":\\)";
    private static final String EMOJI_HAPPY_VALUE = "\uD83D\uDE0A";
    private static final String EMOJI_DIZZY_CODE = "x:d";
    private static final String EMOJI_DIZZY_VALUE = "\uD83D\uDE35";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_AND_OPEN_EYES_CODE = ":d";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_AND_OPEN_EYES_VALUE = "\uD83D\uDE03";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_CODE = ":D";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_VALUE = "\uD83D\uDE04";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_WITH_SWEAT_CODE = ":'D";
    private static final String EMOJI_HAPPY_WITH_OPEN_MOUTH_WITH_SWEAT_VALUE = "\uD83D\uDE05";
    private static final String EMOJI_HAPPY_WITH_TONGUE_CODE = ":p\\)";
    private static final String EMOJI_HAPPY_WITH_TONGUE_VALUE = "\uD83D\uDE0B";
    private static final String EMOJI_HAPPY_WITH_TIGHTLY_CLOSED_EYES_CODE = "x-D";
    private static final String EMOJI_HAPPY_WITH_TIGHTLY_CLOSED_EYES_VALUE = "\uD83D\uDE06";
    private static final String EMOJI_HAPPY_WITH_HEART_SHAPED_EYES_CODE = "o-\\)";
    private static final String EMOJI_HAPPY_WITH_HEART_SHAPED_EYES_VALUE = "\uD83D\uDE0D";
    private static final String EMOJI_HAPPY_WITH_SUNGLASSES_CODE = "B\\)";
    private static final String EMOJI_HAPPY_WITH_SUNGLASSES_VALUE = "\uD83D\uDE0E";
    private static final String EMOJI_NEUTRAL_CODE = "o_o";
    private static final String EMOJI_NEUTRAL_VALUE = "\uD83D\uDE10";
    private static final String EMOJI_EXPRESSIONLESS_CODE = "-_-";
    private static final String EMOJI_EXPRESSIONLESS_VALUE = "\uD83D\uDE11";
    private static final String EMOJI_WINK_CODE = ";\\)";
    private static final String EMOJI_WINK_VALUE = "\uD83D\uDE09";
    private static final String EMOJI_PENSIVE_CODE = "u_u";
    private static final String EMOJI_PENSIVE_VALUE = "\uD83D\uDE14";
    private static final String EMOJI_CONFUSED_CODE = ":-\\(";
    private static final String EMOJI_CONFUSED_VALUE = "\uD83D\uDE15";
    private static final String EMOJI_CONFOUNDED_CODE = ":-3";
    private static final String EMOJI_CONFOUNDED_VALUE = "\uD83D\uDE16";
    private static final String EMOJI_THROWING_KISS_CODE = ":'\\*";
    private static final String EMOJI_THROWING_KISS_VALUE = "\uD83D\uDE18";
    private static final String EMOJI_KISS_CODE = ":\\*";
    private static final String EMOJI_KISS_VALUE = "\uD83D\uDE19";
    private static final String EMOJI_TONGUE_CODE = ":p";
    private static final String EMOJI_TONGUE_VALUE = "\uD83D\uDE1B";
    private static final String EMOJI_TONGUE_WITH_WINK_CODE = ":P";
    private static final String EMOJI_TONGUE_WITH_WINK_VALUE = "\uD83D\uDE1C";
    private static final String EMOJI_TONGUE_WITH_TIGHTLY_CLOSED_EYES_CODE = "x-p";
    private static final String EMOJI_TONGUE_WITH_TIGHTLY_CLOSED_EYES_VALUE = "\uD83D\uDE1D";
    private static final String EMOJI_SURPRISED_CODE = ":O";
    private static final String EMOJI_SURPRISED_VALUE = "\uD83D\uDE2E";
    private static final String EMOJI_HUSHED_CODE = ":o";
    private static final String EMOJI_HUSHED_VALUE = "\uD83D\uDE2F";
    private static final String EMOJI_SCREAMING_CODE = ":'o";
    private static final String EMOJI_SCREAMING_VALUE = "\uD83D\uDE31";
    private static final String EMOJI_SLEEPING_CODE = ":z";
    private static final String EMOJI_SLEEPING_VALUE = "\uD83D\uDE34";
    private static final String EMOJI_SAD_WITH_SWEAT_CODE = ":'\\(";
    private static final String EMOJI_SAD_WITH_SWEAT_VALUE = "\uD83D\uDE13";
    private static final String EMOJI_WORRIED_CODE = "o:\\(";
    private static final String EMOJI_WORRIED_VALUE = "\uD83D\uDE1F";
    private static final String EMOJI_SAD_CODE = ":\\(";
    private static final String EMOJI_SAD_VALUE = "\uD83D\uDE1E";
    private static final String EMOJI_ANGRY_CODE = ":@";
    private static final String EMOJI_ANGRY_VALUE = "\uD83D\uDE20";
    private static final String EMOJI_CRYING_CODE = "':";
    private static final String EMOJI_CRYING_VALUE = "\uD83D\uDE22";
    private static final String EMOJI_ANGUISHED_CODE = ":a";
    private static final String EMOJI_ANGUISHED_VALUE = "\uD83D\uDE27";
    private static final String EMOJI_FEARFUL_CODE = ":'a";
    private static final String EMOJI_FEARFUL_VALUE = "\uD83D\uDE28";
    private static final String EMOJI_TIRED_CODE = ":t";
    private static final String EMOJI_TIRED_VALUE = "\uD83D\uDE2B";
    private static final String EMOJI_SMIRK_CODE = "o\\)";
    private static final String EMOJI_SMIRK_VALUE = "\uD83D\uDE0F";

    public static final String EMOJI_ENABLE_IDENTIFIER = "1";
    public static final String EMOJI_DISENABLE_IDENTIFIER = "0";
    public static final String EMOJI_KEY = "emoji_key";

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

    public static String getLocalDateAndTime(Context context, String dateAndTimeToConvert) {
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = sdf.parse(dateAndTimeToConvert);
        } catch (ParseException e) {
            return context.getResources().getString(R.string.data_not_available);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FULL_DATE_PATTERN, Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(date);
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

    public static void deleteImageFromStorage(
            final Context context,
            final String url,
            final String fromUid,
            final String toUid) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imageStorageReference;
        if (url != null && !url.isEmpty()) {
            imageStorageReference
                    = firebaseStorage.getReferenceFromUrl(url);
            imageStorageReference.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (toUid == null || fromUid == null) {
                        Toast.makeText(
                                context,
                                context.getResources().getString(R.string.problem)
                                + ": " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        UnusedPhotoDetail unusedPhotoDetail = new UnusedPhotoDetail(
                                fromUid,
                                toUid,
                                url
                        );
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        firebaseDatabase.getReference()
                                .child(ApplicationUtils.UNUSED_NODE)
                                .child(fromUid + "_" + toUid)
                                .push()
                                .setValue(unusedPhotoDetail);
                    }
                }
            });
        }
    }

    public static void openWebPage (Context context, String url) {
        Uri webPage = Uri.parse(url);
        Intent openWebPageIntent = new Intent(Intent.ACTION_VIEW, webPage);
        if (openWebPageIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(openWebPageIntent);
        }
    }

    public static List<ChatDetail> getDateInfoNextToMessageData(
            Context context,
            List<MessageDetail> messageDetailList) {
        List<ChatDetail> chatDetailList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        for (MessageDetail messageDetail : messageDetailList) {
            String dateAndTimeAsText = messageDetail.getTimestamp();
            String dateAndTimeLocal = ApplicationUtils.getLocalDateAndTimeToDisplay(
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
        if (type.equals(res.getString(R.string.relationship_type_other_id)))
            return res.getString(R.string.relationship_type_acquaintanceship);
        else if (type.equals(res.getString(R.string.relationship_type_lover_id)))
            return res.getString(R.string.relationship_type_lover);
        else if (type.equals(res.getString(R.string.relationship_type_friend_id)))
            return res.getString(R.string.relationship_type_friend);
        else if (type.equals(res.getString(R.string.relationship_type_cousin_id)))
            return res.getString(R.string.relationship_type_cousin);
        else if (type.equals(res.getString(R.string.relationship_type_uncle_id))) {
            if (!isPairItemShouldBeReversed) return res.getString(R.string.relationship_type_uncle);
            else {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_nephew);
                else return res.getString(R.string.relationship_type_niece);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_aunt_id))) {
            if (!isPairItemShouldBeReversed) return res.getString(R.string.relationship_type_aunt);
            else {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_nephew);
                else return res.getString(R.string.relationship_type_niece);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_nephew_id))) {
            if (!isPairItemShouldBeReversed) return res.getString(R.string.relationship_type_nephew);
            else {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_uncle);
                else return res.getString(R.string.relationship_type_aunt);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_niece_id))) {
            if (!isPairItemShouldBeReversed) return res.getString(R.string.relationship_type_niece);
            else {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_relative);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_uncle);
                else return res.getString(R.string.relationship_type_aunt);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_sibling_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_sibling);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_sibling_male);
                else return res.getString(R.string.relationship_type_sibling_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_sibling);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_sibling_male);
                else return res.getString(R.string.relationship_type_sibling_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_parent_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_child);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_child_male);
                else return res.getString(R.string.relationship_type_child_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_parent);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_parent_male);
                else return res.getString(R.string.relationship_type_parent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_godparent_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_godchild);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_godchild_male);
                else return res.getString(R.string.relationship_type_godchild_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_godparent);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_godparent_male);
                else return res.getString(R.string.relationship_type_godparent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_grandparent_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_grandchild);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_grandchild_male);
                else return res.getString(R.string.relationship_type_grandchild_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_grandparent);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_grandparent_male);
                else return res.getString(R.string.relationship_type_grandparent_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_child_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_parent);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_parent_male);
                else return res.getString(R.string.relationship_type_parent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_child);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_child_male);
                else return res.getString(R.string.relationship_type_child_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_godchild_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_godparent);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_godparent_male);
                else return res.getString(R.string.relationship_type_godparent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_godchild);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_godchild_male);
                else return res.getString(R.string.relationship_type_godchild_female);
            }
        } else if (type.equals(res.getString(R.string.relationship_type_grandchild_id))) {
            if (isPairItemShouldBeReversed) {
                if (fromGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_grandparent);
                else if (fromGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_grandparent_male);
                else return res.getString(R.string.relationship_type_grandparent_female);
            } else {
                if (toGender.equals(res.getString(R.string.gender_none_id)))
                    return res.getString(R.string.relationship_type_grandchild);
                else if (toGender.equals(res.getString(R.string.gender_male_id)))
                    return res.getString(R.string.relationship_type_grandchild_male);
                else return res.getString(R.string.relationship_type_grandchild_female);
            }
        } else return "";
    }

    public static String getTranslatedWishOccasion(Context context, String occasion) {
        if (context == null) return occasion;
        Resources res = context.getResources();
        if (occasion.equals(res.getString(R.string.wish_birthday_occasion_id))) {
            occasion = res.getString(R.string.wish_birthday_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_name_day_occasion_id))) {
            occasion = res.getString(R.string.wish_name_day_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_christmas_occasion_id))) {
            occasion = res.getString(R.string.wish_christmas_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_new_year_occasion_id))) {
            occasion = res.getString(R.string.wish_new_year_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_easter_occasion_id))) {
            occasion = res.getString(R.string.wish_easter_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_valentine_day_occasion_id))) {
            occasion = res.getString(R.string.wish_valentine_day_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_anniversary_occasion_id))) {
            occasion = res.getString(R.string.wish_anniversary_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_women_day_occasion_id))) {
            occasion = res.getString(R.string.wish_women_day_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_mother_day_occasion_id))) {
            occasion = res.getString(R.string.wish_mother_day_occasion);
        } else if (occasion.equals(res.getString(R.string.wish_father_day_occasion_id))) {
            occasion = res.getString(R.string.wish_father_day_occasion);
        }
        return occasion;
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
                                        ).append(name)
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

    public static String convertTextToEmojiIfNeeded(Context context, String text) {
        String storedAnalysisIdentifier = getValueFromPrefs(context, EMOJI_KEY);
        if (storedAnalysisIdentifier == null
                || storedAnalysisIdentifier.equals(EMOJI_ENABLE_IDENTIFIER)) {
            List<String> emojiCodeList = getEmojiCodeList();
            List<String> emojiValueList = getEmojiValueList();
            for (int i = 0; i < emojiCodeList.size(); i++) {
                String currCode = emojiCodeList.get(i);
                String currValue = emojiValueList.get(i);
                if (currCode != null && currValue != null) {
                    text = text.replaceAll(currCode, currValue);
                }
            }
        }
        return text;
    }

    public static List<String> getEmojiCodeList() {
        List<String> emojiCodeList = new ArrayList<>();
        emojiCodeList.add(EMOJI_HEART_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_HALO_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_HORNS_CODE);
        emojiCodeList.add(EMOJI_RELIEVED_CODE);
        emojiCodeList.add(EMOJI_HAPPY_CODE);
        emojiCodeList.add(EMOJI_DIZZY_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_AND_OPEN_EYES_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_WITH_SWEAT_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_TONGUE_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_TIGHTLY_CLOSED_EYES_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_HEART_SHAPED_EYES_CODE);
        emojiCodeList.add(EMOJI_HAPPY_WITH_SUNGLASSES_CODE);
        emojiCodeList.add(EMOJI_NEUTRAL_CODE);
        emojiCodeList.add(EMOJI_EXPRESSIONLESS_CODE);
        emojiCodeList.add(EMOJI_WINK_CODE);
        emojiCodeList.add(EMOJI_PENSIVE_CODE);
        emojiCodeList.add(EMOJI_CONFUSED_CODE);
        emojiCodeList.add(EMOJI_CONFOUNDED_CODE);
        emojiCodeList.add(EMOJI_THROWING_KISS_CODE);
        emojiCodeList.add(EMOJI_KISS_CODE);
        emojiCodeList.add(EMOJI_TONGUE_CODE);
        emojiCodeList.add(EMOJI_TONGUE_WITH_WINK_CODE);
        emojiCodeList.add(EMOJI_TONGUE_WITH_TIGHTLY_CLOSED_EYES_CODE);
        emojiCodeList.add(EMOJI_SURPRISED_CODE);
        emojiCodeList.add(EMOJI_HUSHED_CODE);
        emojiCodeList.add(EMOJI_SCREAMING_CODE);
        emojiCodeList.add(EMOJI_SLEEPING_CODE);
        emojiCodeList.add(EMOJI_SAD_WITH_SWEAT_CODE);
        emojiCodeList.add(EMOJI_WORRIED_CODE);
        emojiCodeList.add(EMOJI_SAD_CODE);
        emojiCodeList.add(EMOJI_ANGRY_CODE);
        emojiCodeList.add(EMOJI_CRYING_CODE);
        emojiCodeList.add(EMOJI_ANGUISHED_CODE);
        emojiCodeList.add(EMOJI_FEARFUL_CODE);
        emojiCodeList.add(EMOJI_TIRED_CODE);
        emojiCodeList.add(EMOJI_SMIRK_CODE);
        return emojiCodeList;
    }

    public static List<String> getEmojiValueList() {
        List<String> emojiValueList = new ArrayList<>();
        emojiValueList.add(EMOJI_HEART_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_HALO_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_HORNS_VALUE);
        emojiValueList.add(EMOJI_RELIEVED_VALUE);
        emojiValueList.add(EMOJI_HAPPY_VALUE);
        emojiValueList.add(EMOJI_DIZZY_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_AND_OPEN_EYES_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_OPEN_MOUTH_WITH_SWEAT_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_TONGUE_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_TIGHTLY_CLOSED_EYES_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_HEART_SHAPED_EYES_VALUE);
        emojiValueList.add(EMOJI_HAPPY_WITH_SUNGLASSES_VALUE);
        emojiValueList.add(EMOJI_NEUTRAL_VALUE);
        emojiValueList.add(EMOJI_EXPRESSIONLESS_VALUE);
        emojiValueList.add(EMOJI_WINK_VALUE);
        emojiValueList.add(EMOJI_PENSIVE_VALUE);
        emojiValueList.add(EMOJI_CONFUSED_VALUE);
        emojiValueList.add(EMOJI_CONFOUNDED_VALUE);
        emojiValueList.add(EMOJI_THROWING_KISS_VALUE);
        emojiValueList.add(EMOJI_KISS_VALUE);
        emojiValueList.add(EMOJI_TONGUE_VALUE);
        emojiValueList.add(EMOJI_TONGUE_WITH_WINK_VALUE);
        emojiValueList.add(EMOJI_TONGUE_WITH_TIGHTLY_CLOSED_EYES_VALUE);
        emojiValueList.add(EMOJI_SURPRISED_VALUE);
        emojiValueList.add(EMOJI_HUSHED_VALUE);
        emojiValueList.add(EMOJI_SCREAMING_VALUE);
        emojiValueList.add(EMOJI_SLEEPING_VALUE);
        emojiValueList.add(EMOJI_SAD_WITH_SWEAT_VALUE);
        emojiValueList.add(EMOJI_WORRIED_VALUE);
        emojiValueList.add(EMOJI_SAD_VALUE);
        emojiValueList.add(EMOJI_ANGRY_VALUE);
        emojiValueList.add(EMOJI_CRYING_VALUE);
        emojiValueList.add(EMOJI_ANGUISHED_VALUE);
        emojiValueList.add(EMOJI_FEARFUL_VALUE);
        emojiValueList.add(EMOJI_TIRED_VALUE);
        emojiValueList.add(EMOJI_SMIRK_VALUE);
        return emojiValueList;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static int getExifRotation(InputStream in) {
        try {
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return ExifInterface.ORIENTATION_UNDEFINED;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
