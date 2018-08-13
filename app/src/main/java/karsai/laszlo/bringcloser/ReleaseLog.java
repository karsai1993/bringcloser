package karsai.laszlo.bringcloser;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import timber.log.Timber;

/**
 * Timber is under the following license:
 * Copyright 2013 Jake Wharton

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 * I created this class to handle Timber logs when the app is in release mode
 */
public class ReleaseLog extends Timber.DebugTree {

    @Override
    protected boolean isLoggable(@Nullable String tag, int priority) {
        return priority != Log.VERBOSE && priority != Log.DEBUG && priority != Log.INFO;
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        if (priority == Log.ASSERT) {//wtf in Timber
            registerProblem(message, tag);
        }
    }

    private void registerProblem(@NotNull String message, @Nullable String tag) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child("problems")
                .push();
        databaseReference.child("tag").setValue(tag);
        databaseReference.child("message").setValue(message);
        databaseReference.child("utc_time").setValue(ApplicationUtils.getCurrentUTCDateAndTime());
        databaseReference.child("from").setValue(FirebaseAuth.getInstance().getUid());
    }
}
