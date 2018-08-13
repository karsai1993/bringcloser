package karsai.laszlo.bringcloser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Laci on 06/06/2018.
 * Util to handle image requests
 */
public class ImageUtils {

    private static final String PHOTO_PICKER_TITLE = "Complete action using";
    private static final String PHOTO_TYPE = "image/*";
    public static final int RC_PHOTO_PICKER = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String CAMERA_DATA_KEY = "data";
    private static final String CAMERA_IMAGE_EXTENSION = ".jpg";
    private static final String FROM_FILE_ID = "file";
    private static final String FROM_CAMERA_ID = "camera";

    private static String sRequestPermissionSource;

    public static void setPhoto(
            final Context context,
            String photoUrl,
            ImageView imageView,
            boolean shouldBeCircle) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.baseline_scatter_plot_black_48);
        requestOptions.error(R.drawable.baseline_error_outline_black_48);
        requestOptions.fitCenter();
        if (shouldBeCircle) {
            requestOptions.circleCrop();
        }
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
            int requestCode,
            int resultCode,
            Intent data,
            StorageReference storageReference,
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        if ((requestCode == RC_PHOTO_PICKER || requestCode == REQUEST_IMAGE_CAPTURE)
                && resultCode == RESULT_OK) {
            Uri imageUri;
            switch (requestCode) {
                case RC_PHOTO_PICKER:
                    imageUri = data.getData();
                    storeFileInStorage(
                            context,
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
                        if (image == null) {
                            Timber.wtf("image null imageutils");
                            break;
                        }
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        File destination = new File(
                                android.os.Environment.getExternalStorageDirectory()
                                        + File.separator
                                        + context.getResources().getString(R.string.app_name),
                                System.currentTimeMillis() + CAMERA_IMAGE_EXTENSION);
                        FileOutputStream fo;
                        try {
                            if (!destination.getParentFile().exists())
                                destination.getParentFile().mkdirs();
                            if (!destination.exists())
                                destination.createNewFile();
                            fo = new FileOutputStream(destination);
                            fo.write(bytes.toByteArray());
                            fo.close();
                            imageUri = Uri.fromFile(destination);
                            if (imageUri != null)
                                storeFileInStorage(
                                        context,
                                        imageUri,
                                        storageReference,
                                        onSuccessListener
                                );
                        } catch (IOException e) {
                            Timber.wtf("creating image ioexception");
                            return;
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
            Uri imageUri,
            StorageReference photoRef,
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(onSuccessListener);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double actualProgressValue = (double) taskSnapshot.getBytesTransferred() /
                        taskSnapshot.getTotalByteCount() * 100;
                NotificationUtils.addUploadNotification(
                        context,
                        false,
                        actualProgressValue == 0.0D,
                        (int)actualProgressValue);
                if (actualProgressValue == 100D) {
                    NotificationUtils.addUploadNotification(
                            context,
                            true,
                            false,
                            0);
                }
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.getMessage();
                Timber.wtf("image upload: " + message);
            }
        });
    }
}
