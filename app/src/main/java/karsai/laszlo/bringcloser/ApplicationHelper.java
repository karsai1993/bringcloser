package karsai.laszlo.bringcloser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import karsai.laszlo.bringcloser.model.ChatDetail;
import karsai.laszlo.bringcloser.model.Message;
import karsai.laszlo.bringcloser.model.MessageDetail;
import karsai.laszlo.bringcloser.model.Wish;

/**
 * Created by Laci on 28/05/2018.
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
    public static final String CONNECTION_FROM_UID_IDENTIFIER = "fromUid";
    public static final String CONNECTION_FROM_NAME_IDENTIFIER = "fromName";
    public static final String CONNECTION_FROM_BIRTHDAY_IDENTIFIER = "fromBirthday";
    public static final String CONNECTION_FROM_GENDER_IDENTIFIER = "fromGender";
    public static final String CONNECTION_FROM_PHOTO_URL_IDENTIFIER = "fromPhotoUrl";
    public static final String CONNECTION_TO_UID_IDENTIFIER = "toUid";
    public static final String CONNECTION_TO_NAME_IDENTIFIER = "toName";
    public static final String CONNECTION_TO_BIRTHDAY_IDENTIFIER = "toBirthday";
    public static final String CONNECTION_TO_GENDER_IDENTIFIER = "toGender";
    public static final String CONNECTION_TO_PHOTO_URL_IDENTIFIER = "toPhotoUrl";
    public static final String OBJECT_TEXT_IDENTIFIER = "text";
    public static final String OBJECT_HAS_ARRIVED_IDENTIFIER = "hasArrived";
    public static final String CONNECTION_CONNECTED_IDENTIFIER = "connectionBit";
    public static final String CONNECTION_TIMESTAMP_IDENTIFIER = "timestamp";
    public static final String INTENT_CHOSEN_USER_KEY = "chosen_user";
    public static final String TOKEN_STORAGE_KEY = "firebase_instance_id_key";
    public static final String TOKEN_NEW_STORAGE_KEY = "new_firebase_instance_id_key";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_REQUEST = "action_request";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION = "action_connection";
    public static final String NEW_SENT_REQUEST_INTENT_ACTION_PAGE_CONNECTION = "action_sent_rq";
    public static final String WISH_ACTION_PAGE_CONNECTION_DETAIL = "action_wish_conn_detail";
    public static final String EVENT_ACTION_PAGE_CONNECTION_DETAIL = "action_event_conn_detail";
    public static final String THOUGHT_ACTION_PAGE_CONNECTION_DETAIL = "action_thought_conn_detail";
    public static final String VIEW_IMAGE = "view_image";
    public static final String SAVE_RECYCLERVIEW_POS_KEY = "save_recyclerview_pos";
    public static final String EXTRA_X_COORD = "x_coord";
    public static final String EXTRA_Y_COORD = "y_coord";
    public static final String DATE_PATTERN_FULL = "yyyy MMM dd_HH:mm";
    public static final String DATE_PATTERN_FOR_SERVICE = "yyyy MMM dd_HH:mm:ss";
    public static final String DATE_PATTERN_COMPOSITION = "yyyy-MM-dd_HH:mm";
    public static final String DATE_PATTERN_DISPLAY = "yyyy MMM dd";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm";
    public static final String DATE_PATTERN_FULL_STORAGE = "yyyyMMddHHmmss";
    public static final String DATE_SPLITTER = "_";
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

    public static final int CONNECTION_BIT_POS = 1;
    public static final int CONNECTION_BIT_NEG = 0;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static String getServiceUniqueTag(String fromUid, String toUid, String key) {
        return new StringBuilder()
                .append(fromUid)
                .append("_")
                .append(toUid)
                .append("_")
                .append(key)
                .toString();
    }

    public static String getLocalDateAndTime(Context context, String dateAndTime) {
        Locale[] locales = Locale.getAvailableLocales();
        String result = convertDateAndTimeToLocal(dateAndTime, Locale.getDefault());
        if (!result.isEmpty()) {
            return result;
        } else {
            for (Locale locale : locales) {
                result = convertDateAndTimeToLocal(dateAndTime, locale);
                if (!result.isEmpty()) {
                    return result;
                }
            }
            return context.getResources().getString(R.string.data_not_available);
        }
    }

    public static String convertDateAndTimeToLocal(
            String dateAndTime,
            Locale locale) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN_FULL, locale);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = df.parse(dateAndTime);
        } catch (ParseException e) {
            return "";
        }
        df.setTimeZone(TimeZone.getDefault());
        String result = df.format(date);
        if (!result.isEmpty()) {
            SimpleDateFormat defLocaleSdf = new SimpleDateFormat(
                    DATE_PATTERN_FULL, Locale.getDefault());
            defLocaleSdf.setTimeZone(TimeZone.getDefault());
            return defLocaleSdf.format(date);
        }
        return df.format(date);
    }

    public static String getCurrentUTCDateAndTime(String pattern) {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(currentDate);
    }

    public static String getDisplayDateWithRespectToCurrentDate(Context context, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_DISPLAY, Locale.getDefault());
        SimpleDateFormat simpleDateFormatDisplay
                = new SimpleDateFormat(DATE_PATTERN_DISPLAY, Locale.getDefault());
        try {
            Date actualDate = sdf.parse(date);
            Date todayDate = new Date();
            sdf.setTimeZone(TimeZone.getDefault());
            simpleDateFormatDisplay.setTimeZone(TimeZone.getDefault());
            String todayDateAsText = sdf.format(todayDate);
            String actualDateAsText = sdf.format(actualDate);
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.add(Calendar.DATE, -1);
            Date yesterdayDate = calendar.getTime();
            String yesterdayDateAsText = sdf.format(yesterdayDate);
            if (actualDateAsText.equals(todayDateAsText)) {
                return context.getString(R.string.today);
            } else if (actualDateAsText.equals(yesterdayDateAsText)) {
                return context.getString(R.string.yesterday);
            } else {
                return simpleDateFormatDisplay.format(actualDate);
            }
        } catch (ParseException e) {
            return "";
        }
    }

    public static void showFoundResultsNumber(String title, int size, TextView textView) {
        textView.setText(
                new StringBuilder()
                        .append(title)
                        .append(" (")
                        .append(size)
                        .append(")")
                        .toString()
        );
    }

    public static void deletePairMemoryElements(
            final Context context,
            final String fromUid,
            final String toUid,
            final String name) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef
                = firebaseDatabase.getReference().child(CONNECTIONS_NODE);
        Query connectionsQuery = connectionsRef
                .orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(fromUid);
        connectionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    if (dataSnapshot
                            .child(key)
                            .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                            .getValue(String.class)
                            .equals(toUid)) {
                        DatabaseReference messagesDatabaseRef = connectionsRef
                                .child(key)
                                .child(ApplicationHelper.MESSAGES_NODE);
                        messagesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                    Message message = messageSnapshot.getValue(Message.class);
                                    deleteImageFromStorage(context, message.getPhotoUrl());
                                }
                                deletePairConnection(fromUid, toUid, context, name);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public static void deleteSingleMemoryElements(
            final Context context,
            final String uid) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef
                = firebaseDatabase.getReference().child(CONNECTIONS_NODE);
        connectionsRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                .equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    DatabaseReference messagesDatabaseRef = connectionsRef
                            .child(key)
                            .child(ApplicationHelper.MESSAGES_NODE);
                    messagesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                Message message = messageSnapshot.getValue(Message.class);
                                deleteImageFromStorage(context, message.getPhotoUrl());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        connectionsRef.orderByChild(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                .equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                    String key = connectionSnapshot.getKey();
                    DatabaseReference messagesDatabaseRef = connectionsRef
                            .child(key)
                            .child(ApplicationHelper.MESSAGES_NODE);
                    messagesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                Message message = messageSnapshot.getValue(Message.class);
                                deleteImageFromStorage(context, message.getPhotoUrl());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            String currDateAndTime = messageDetail.getTimestamp();
            String currDateAndTimeLocale = ApplicationHelper.getLocalDateAndTime(
                    context,
                    currDateAndTime
            );
            String [] parts = currDateAndTimeLocale.split(ApplicationHelper.DATE_SPLITTER);
            String currDateAsText = parts[0];
            if (!dateList.contains(currDateAsText)) {
                dateList.add(currDateAsText);
                ChatDetail currDate = new ChatDetail(currDateAsText);
                chatDetailList.add(currDate);
            }
            ChatDetail currMessage = new ChatDetail(messageDetail, currDateAndTimeLocale);
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
            else return res.getString(R.string.relationship_type_nephew);
        } else if (type.equals(res.getString(R.string.relationship_type_aunt))) {
            if (!isPairItemShouldBeReversed) return type;
            else return res.getString(R.string.relationship_type_niece);
        } else if (type.equals(res.getString(R.string.relationship_type_nephew))) {
            if (!isPairItemShouldBeReversed) return type;
            else return res.getString(R.string.relationship_type_uncle);
        } else if (type.equals(res.getString(R.string.relationship_type_niece))) {
            if (!isPairItemShouldBeReversed) return type;
            else return res.getString(R.string.relationship_type_aunt);
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
                            if (dataSnapshot
                                    .child(key)
                                    .child(CONNECTION_TO_UID_IDENTIFIER)
                                    .getValue(String.class).equals(toUid)) {
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

    public static void deleteSingleConnection(String uid) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef = firebaseDatabase.getReference().child(CONNECTIONS_NODE);
        connectionsRef.orderByChild(CONNECTION_FROM_UID_IDENTIFIER).equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            connectionsRef.child(key).setValue(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        connectionsRef.orderByChild(CONNECTION_TO_UID_IDENTIFIER).equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            connectionsRef.child(key).setValue(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static void saveTokenToPrefs(Context context, String key, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, token);
        editor.apply();
    }

    public static String getTokenFromPrefs(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static boolean checkPlayServices(
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

    public static List<String> getGenderOptionsList(Context context) {
        List<String> genderOptionsList = new ArrayList<>();
        genderOptionsList.add(context.getResources().getString(R.string.gender_none));
        genderOptionsList.add(context.getResources().getString(R.string.gender_male));
        genderOptionsList.add(context.getResources().getString(R.string.gender_female));
        return genderOptionsList;
    }

    public static List<String> getRelationshipOptionsList(Context context) {
        List<String> relationshipOptionsList = new ArrayList<>();
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_none));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_lover));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_friend));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_sibling));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_cousin));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_parent));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_child));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_godparent));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_godchild));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_grandparent));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_grandchild));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_uncle));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_aunt));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_nephew));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_niece));
        relationshipOptionsList.add(
                context.getResources().getString(R.string.relationship_type_other));
        return relationshipOptionsList;
    }
}
