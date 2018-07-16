package karsai.laszlo.bringcloser.service;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import karsai.laszlo.bringcloser.ApplicationHelper;

public class UpdateStateService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        new UpdateStateAsyncTask(this, job).execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    static class UpdateStateAsyncTask extends AsyncTask<Void, Void, Integer> {

        private final static Integer RESULT_OK = 1;
        private final static Integer RESULT_FAILURE = 0;

        private UpdateStateService mUpdateStateService;
        private JobParameters mJob;

        public UpdateStateAsyncTask(UpdateStateService updateStateService, JobParameters job) {
            this.mUpdateStateService = updateStateService;
            this.mJob = job;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference connectionsDataBaseRef = firebaseDatabase.getReference()
                    .child(ApplicationHelper.CONNECTIONS_NODE);
            if (mJob != null) {
                Bundle extras = mJob.getExtras();
                if (extras != null) {
                    String type = extras.getString(ApplicationHelper.SERVICE_TYPE_IDENTIFIER);
                    String fromUid
                            = extras.getString(ApplicationHelper.SERVICE_CONTENT_FROM_IDENTIFIER);
                    String toUid
                            = extras.getString(ApplicationHelper.SERVICE_CONTENT_TO_IDENTIFIER);
                    String key
                            = extras.getString(ApplicationHelper.SERVICE_CONTENT_KEY_IDENTIFIER);
                    if (type != null) {
                        updateDatabase(type, fromUid, toUid, key, connectionsDataBaseRef);
                        return RESULT_OK;
                    } else {
                        return RESULT_FAILURE;
                    }
                } else {
                    return RESULT_FAILURE;
                }
            } else {
                return RESULT_FAILURE;
            }
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            mUpdateStateService.jobFinished(mJob, false);
        }

        private void updateDatabase(
                final String type,
                String fromUid,
                final String toUid,
                final String objectKey,
                final DatabaseReference connectionsDatabaseRef) {
            connectionsDatabaseRef.orderByChild(ApplicationHelper.CONNECTION_FROM_UID_IDENTIFIER)
                    .equalTo(fromUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot connectionSnapshot : dataSnapshot.getChildren()) {
                                String key = connectionSnapshot.getKey();
                                if (dataSnapshot
                                        .child(key)
                                        .child(ApplicationHelper.CONNECTION_TO_UID_IDENTIFIER)
                                        .getValue(String.class)
                                        .equals(toUid)) {
                                    DatabaseReference databaseReference;
                                    if (type.equals(ApplicationHelper.SERVICE_TYPE_WISH)) {
                                        databaseReference = connectionsDatabaseRef
                                                .child(key)
                                                .child(ApplicationHelper.WISHES_NODE).child(objectKey);
                                    } else if (type.equals(ApplicationHelper.SERVICE_TYPE_EVENT)) {
                                        databaseReference = connectionsDatabaseRef
                                                .child(key)
                                                .child(ApplicationHelper.EVENTS_NODE).child(objectKey);
                                    } else {
                                        return;
                                    }
                                    Map<String, Object> updateValueMap = new HashMap<>();
                                    updateValueMap.put(
                                            "/" + ApplicationHelper.OBJECT_HAS_ARRIVED_IDENTIFIER,
                                            true
                                    );
                                    databaseReference.updateChildren(updateValueMap);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }
}
