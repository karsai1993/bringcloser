package karsai.laszlo.bringcloser.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 05/06/2018.
 */

public class PermissionUtils {
    public static final int REQUEST_EXTERNAL_STORAGE = 3;

    public static boolean isPermissionChecked(Context context) {
        final Activity activity = (Activity) context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    builder.setTitle(context.getResources().getString(R.string.permission_needed));
                    builder.setMessage(context.getResources()
                            .getString(R.string.permission_needed_message));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(activity);
                        }
                    });
                    builder.create().show();
                } else {
                    requestPermissions(activity);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_EXTERNAL_STORAGE
        );
    }
}
