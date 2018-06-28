package karsai.laszlo.bringcloser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import karsai.laszlo.bringcloser.activity.AddNewConnectionActivity;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.adapter.ConnectedPeopleAdapter;
import karsai.laszlo.bringcloser.adapter.ConnectionFragmentPagerAdapter;
import karsai.laszlo.bringcloser.adapter.RequestFromUsersAdapter;
import karsai.laszlo.bringcloser.adapter.RequestToUsersAdapter;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.User;

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
    public static final String CONNECTION_CONNECTED_IDENTIFIER = "connectionBit";
    public static final String INTENT_CHOSEN_USER_KEY = "chosen_user";
    public static final String TOKEN_STORAGE_KEY = "firebase_instance_id_key";
    public static final String TOKEN_NEW_STORAGE_KEY = "new_firebase_instance_id_key";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_REQUEST = "action_request";
    public static final String NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION = "action_connection";
    public static final String VIEW_IMAGE = "view_image";
    public static final String SAVE_RECYCLERVIEW_POS_KEY = "save_recyclerview_pos";
    public static final int CONNECTION_BIT_POS = 1;
    public static final int CONNECTION_BIT_NEG = 0;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

    public static String getPersonalizedRelationshipType(
            Context context,
            String type,
            String toGender,
            String fromGender,
            boolean isPairItemShouldBeChanged) {
        Resources res = context.getResources();
        if (type.equals(res.getString(R.string.relationship_type_other)))
            return res.getString(R.string.relationship_type_acquaintanceship);
        else if (type.equals(res.getString(R.string.relationship_type_lover)) ||
                type.equals(res.getString(R.string.relationship_type_friend)) ||
                type.equals(res.getString(R.string.relationship_type_cousin)))
            return type;
        else if (type.equals(res.getString(R.string.relationship_type_uncle))) {
            if (!isPairItemShouldBeChanged) return type;
            else return res.getString(R.string.relationship_type_nephew);
        } else if (type.equals(res.getString(R.string.relationship_type_aunt))) {
            if (!isPairItemShouldBeChanged) return type;
            else return res.getString(R.string.relationship_type_niece);
        } else if (type.equals(res.getString(R.string.relationship_type_nephew))) {
            if (!isPairItemShouldBeChanged) return type;
            else return res.getString(R.string.relationship_type_uncle);
        } else if (type.equals(res.getString(R.string.relationship_type_niece))) {
            if (!isPairItemShouldBeChanged) return type;
            else return res.getString(R.string.relationship_type_aunt);
        } else if (type.equals(res.getString(R.string.relationship_type_sibling))) {
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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
            if (isPairItemShouldBeChanged) {
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

    /*public static void updateConnectionMember(
            String uid,
            final String updatedName,
            final String updatedBirthday,
            final String updatedGender,
            final String updatedPhotoUrl) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference connectionsRef = firebaseDatabase.getReference().child(CONNECTIONS_NODE);
        connectionsRef.orderByChild(CONNECTION_FROM_UID_IDENTIFIER).equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                            String key = connectionSnapshot.getKey();
                            DatabaseReference databaseReference = connectionsRef.child(key);
                            Map<String, Object> updateValues = new HashMap<>();
                            updateValues.put("/" + CONNECTION_FROM_NAME_IDENTIFIER, updatedName);
                            updateValues.put(
                                    "/" + CONNECTION_FROM_BIRTHDAY_IDENTIFIER, updatedBirthday);
                            updateValues.put(
                                    "/" + CONNECTION_FROM_GENDER_IDENTIFIER, updatedGender);
                            updateValues.put(
                                    "/" + CONNECTION_FROM_PHOTO_URL_IDENTIFIER, updatedPhotoUrl);
                            databaseReference.updateChildren(updateValues);
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
                            DatabaseReference databaseReference = connectionsRef.child(key);
                            Map<String, Object> updateValues = new HashMap<>();
                            updateValues.put("/" + CONNECTION_TO_NAME_IDENTIFIER, updatedName);
                            updateValues.put(
                                    "/" + CONNECTION_TO_BIRTHDAY_IDENTIFIER, updatedBirthday);
                            updateValues.put(
                                    "/" + CONNECTION_TO_GENDER_IDENTIFIER, updatedGender);
                            updateValues.put(
                                    "/" + CONNECTION_TO_PHOTO_URL_IDENTIFIER, updatedPhotoUrl);
                            databaseReference.updateChildren(updateValues);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }*/

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
