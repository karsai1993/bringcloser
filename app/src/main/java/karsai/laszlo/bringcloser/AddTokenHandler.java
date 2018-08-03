package karsai.laszlo.bringcloser;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Entity;

import karsai.laszlo.bringcloser.ApplicationHelper;
import timber.log.Timber;

/**
 * Class to add token to the database
 */
public class AddTokenHandler {

    public static void addTokenIfNeeded(final String token, String uid) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child(ApplicationHelper.USERS_NODE)
                .child(uid)
                .child(ApplicationHelper.USER_TOKENS_IDENTIFIER);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isContained = false;
                for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                    String key = tokenSnapshot.getKey();
                    if (key == null) {
                        Timber.wtf("key null add token");
                        continue;
                    }
                    if (key.equals(token)) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained) {
                    databaseReference.child(token).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
