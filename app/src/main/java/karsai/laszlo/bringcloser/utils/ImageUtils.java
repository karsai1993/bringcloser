package karsai.laszlo.bringcloser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.security.auth.callback.Callback;

import karsai.laszlo.bringcloser.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Laci on 06/06/2018.
 */

public class ImageUtils {

    private static final String PHOTO_PICKER_TITLE = "Complete action using";
    private static final String PHOTO_TYPE = "image/*";
    private static final int RC_PHOTO_PICKER = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String CAMERA_DATA_KEY = "data";
    private static final String CAMERA_IMAGE_EXTENSION = ".jpg";
    private static final String FROM_FILE_ID = "file";
    private static final String FROM_CAMERA_ID = "camera";

    public static String sRequestPermissionSource;

    public static void setUserPhoto(final Context context, String photoUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_icons8_load_96_1);
        requestOptions.error(R.drawable.baseline_error_outline_black_48);
        requestOptions.fitCenter();
        requestOptions.circleCrop();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context)
                    .load(photoUrl)
                    .apply(requestOptions)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.baseline_face_black_48)
                    .apply(requestOptions)
                    .into(imageView);
        }
    }

    public static void displayMessagePhoto(final Context context, String photoUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_icons8_load_96_1);
        requestOptions.error(R.drawable.baseline_error_outline_black_48);
        requestOptions.fitCenter();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context)
                    .load(photoUrl)
                    .apply(requestOptions)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.baseline_face_black_48)
                    .apply(requestOptions)
                    .into(imageView);
        }
    }

    public static void onClickFromFile(Context context) {
        sRequestPermissionSource = FROM_FILE_ID;
        if (PermissionUtils.isPermissionChecked(context)) {
            applyIntentForFilePicker(context);
        }
    }

    public static void onClickFromCamera(Context context) {
        sRequestPermissionSource = FROM_CAMERA_ID;
        if (PermissionUtils.isPermissionChecked(context)) {
            applyIntentForCameraCapture(context);
        }
    }

    public static void onRequestPermissionResult(
            Context context,
            int requestCode,
            int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (sRequestPermissionSource.equals(FROM_FILE_ID)) {
                    applyIntentForFilePicker(context);
                } else if (sRequestPermissionSource.equals(FROM_CAMERA_ID)) {
                    applyIntentForCameraCapture(context);
                }
            } else {
                Toast.makeText(
                        context,
                        context.getResources().getString(R.string.denied_permission_message),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private static void applyIntentForFilePicker(Context context) {
        Activity activity = (Activity) context;
        Intent chooseImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseImageIntent.setType(PHOTO_TYPE);
        chooseImageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (chooseImageIntent.resolveActivity(context.getPackageManager()) != null) {
            activity.startActivityForResult(
                    Intent.createChooser(
                            chooseImageIntent,
                            PHOTO_PICKER_TITLE
                    ),
                    RC_PHOTO_PICKER
            );
        } else {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.no_suitable_app_message),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private static void applyIntentForCameraCapture(Context context) {
        Activity activity = (Activity) context;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.no_suitable_app_message),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public static void onActivityResult(
            Context context,
            View snackbarView,
            int requestCode,
            int resultCode,
            Intent data,
            StorageReference storageReference,
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        if ((requestCode == RC_PHOTO_PICKER || requestCode == REQUEST_IMAGE_CAPTURE)
                && resultCode == RESULT_OK) {
            final Uri imageUri;
            switch (requestCode) {
                case RC_PHOTO_PICKER:
                    imageUri = data.getData();
                    storeFileInStorage(
                            context,
                            snackbarView,
                            imageUri,
                            storageReference,
                            onSuccessListener
                    );
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap image = (Bitmap) extras.get(CAMERA_DATA_KEY);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        File destination = new File(
                                Environment.getExternalStorageDirectory(),
                                System.currentTimeMillis() + CAMERA_IMAGE_EXTENSION);
                        FileOutputStream fo;
                        try {
                            destination.createNewFile();
                            fo = new FileOutputStream(destination);
                            fo.write(bytes.toByteArray());
                            fo.close();
                            imageUri = Uri.fromFile(destination);
                            if (imageUri != null)
                                storeFileInStorage(
                                        context,
                                        snackbarView,
                                        imageUri,
                                        storageReference,
                                        onSuccessListener
                                );
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void storeFileInStorage(
            final Context context,
            View snackbarView,
            final Uri imageUri,
            final StorageReference photoRef,
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        final Snackbar snackbar = Snackbar.make(
                snackbarView,
                "",
                Snackbar.LENGTH_INDEFINITE);
        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(onSuccessListener);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double actualProgressValue = (double) taskSnapshot.getBytesTransferred() /
                        taskSnapshot.getTotalByteCount() * 100;
                snackbar.setText(new StringBuilder()
                        .append(context.getResources().getString(R.string.uploading_image))
                        .append(" (")
                        .append(String.format(
                                Locale.getDefault(),
                                "%.2f",
                                actualProgressValue)
                        ).append("%)").toString());
                snackbar.show();
                if (actualProgressValue == 100D) {
                    snackbar.dismiss();
                }
            }
        });
    }
}
