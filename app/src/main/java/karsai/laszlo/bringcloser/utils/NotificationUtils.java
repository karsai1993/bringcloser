package karsai.laszlo.bringcloser.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.net.URL;

import karsai.laszlo.bringcloser.activity.UnusedDataActivity;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.activity.ConnectionActivity;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.activity.ReceivedDetailsActivity;
import timber.log.Timber;
import karsai.laszlo.bringcloser.R;

/**
 * Created by Laci on 10/06/2018.
 * Util to handle notification requests
 */
public class NotificationUtils {

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String HIGH_IMP_NOTIFICATION_CHANNEL_ID = "10003";
    private static final String LOW_IMP_NOTIFICATION_CHANNEL_ID = "10004";
    private static final int PDF_NOTIFICATION_ID = 9;
    private static final int UPLOAD_NOTIFICATION_ID = 10;
    private static final String NOTIFICATION_CHANNEL_NAME = "bring_closer_notifications";

    private static Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            int rotDegree = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rotDegree = ApplicationUtils.getExifRotation(url.openStream());
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(url.openStream(), null, options);
            options.inSampleSize = ApplicationUtils.calculateInSampleSize(options, 100, 100);
            options.inJustDecodeBounds = false;
            Bitmap image = BitmapFactory.decodeStream(url.openStream(), null, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotDegree);
            return Bitmap.createBitmap(
                    image,
                    0,
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    matrix,
                    true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addFunctionsNotification(
            Context context,
            String title,
            String message,
            String imageUrl,
            int notificationId,
            String action,
            ConnectionDetail connectionDetail) {
        Intent targetIntent;
        Intent openMainActivityForUpNavigationIntent = null;
        if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_CONNECTION)
                || action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_PAGE_REQUEST)) {
            targetIntent = new Intent(context, MainActivity.class);
        } else if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_WISH)
                || action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_EVENT)
                || action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_THOUGHT)) {
            openMainActivityForUpNavigationIntent = new Intent(context, MainActivity.class);
            openMainActivityForUpNavigationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            targetIntent = new Intent(context, ReceivedDetailsActivity.class);
        } else if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_ACTION_MESSAGE)) {
            openMainActivityForUpNavigationIntent = new Intent(context, MainActivity.class);
            openMainActivityForUpNavigationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            targetIntent = new Intent(context, ConnectionActivity.class);
            targetIntent.putExtra(ApplicationUtils.CONNECTION_KEY, connectionDetail);
        } else if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_PRIVACY)) {
            String url = context.getResources().getString(R.string.privacy_policy);
            Uri webPage = Uri.parse(url);
            targetIntent = new Intent(Intent.ACTION_VIEW, webPage);
        } else if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_TERMS)) {
            String url = context.getResources().getString(R.string.terms_of_use);
            Uri webPage = Uri.parse(url);
            targetIntent = new Intent(Intent.ACTION_VIEW, webPage);
        } else if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_UNUSED)) {
            openMainActivityForUpNavigationIntent = new Intent(context, MainActivity.class);
            openMainActivityForUpNavigationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            targetIntent = new Intent(context, UnusedDataActivity.class);
        } else {
            return;
        }
        PendingIntent contentIntent;
        if (action.equals(ApplicationUtils.NOTIFICATION_INTENT_PRIVACY)
                || action.equals(ApplicationUtils.NOTIFICATION_INTENT_TERMS)) {
            contentIntent = PendingIntent.getActivity(
                    context,
                    0,
                    targetIntent,
                    0
            );
        } else if (openMainActivityForUpNavigationIntent == null) {
            targetIntent.setAction(action);
            contentIntent = PendingIntent.getActivity(
                    context,
                    0,
                    targetIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
        } else {
            targetIntent.setAction(action);
            contentIntent = PendingIntent.getActivities(
                    context,
                    0,
                    new Intent[]{openMainActivityForUpNavigationIntent, targetIntent},
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
        );

        builder.setSmallIcon(R.drawable.ic_stat_notification);
        if (!action.equals(ApplicationUtils.NOTIFICATION_INTENT_UNUSED)) {
            builder.setLargeIcon(
                    action.equals(ApplicationUtils.NOTIFICATION_INTENT_PRIVACY)
                            || action.equals(ApplicationUtils.NOTIFICATION_INTENT_TERMS) ?
                            BitmapFactory.decodeResource(
                                    context.getResources(),
                                    R.mipmap.ic_launcher
                            ) :
                            imageUrl != null ?
                                    getBitmapFromUrl(imageUrl) :
                                    BitmapFactory.decodeResource(
                                            context.getResources(),
                                            R.drawable.baseline_face_black_48
                                    )
            );
        }
        builder.setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.wtf("notificationmanager null");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    importance
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLACK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}
            );
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(notificationId, builder.build());
    }

    public static void addPdfNotification(
            Context context,
            File destination,
            boolean isDone,
            int progress) {
        boolean isHighImportanceRequired = isDone || progress == 1;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                        LOW_IMP_NOTIFICATION_CHANNEL_ID
        );

        builder.setSmallIcon(R.drawable.ic_stat_notification);
        builder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        if (isDone) {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    "android.support.v4.content.FileProvider",
                    destination);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent targetIntent = Intent.createChooser(
                    intent,
                    context.getResources().getString(R.string.pdf_notification_choose)
            );
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    0,
                    targetIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
            builder.setProgress(0, progress, false);
            builder.setContentTitle(context.getResources().getString(R.string.pdf_notification_title_done))
                    .setContentText(context.getResources().getString(R.string.pdf_notification_message))
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(contentIntent);
        } else {
            builder.setProgress(6, progress, false)
                    .setContentTitle(context.getResources().getString(R.string.pdf_notification_title_progress))
                    .setDefaults(0)
                    .setSound(null);
        }

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.wtf("notificationmanager null");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(
                    isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                            LOW_IMP_NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    isHighImportanceRequired ? NotificationManager.IMPORTANCE_HIGH :
                            NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLACK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}
            );
            builder.setChannelId(
                    isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                            LOW_IMP_NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(PDF_NOTIFICATION_ID, builder.build());
    }

    public static void addUploadNotification(
            Context context,
            boolean isDone,
            boolean isFirst,
            int progress) {
        boolean isHighImportanceRequired = isDone || isFirst;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                        LOW_IMP_NOTIFICATION_CHANNEL_ID
        );

        builder.setSmallIcon(R.drawable.ic_stat_notification);
        builder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        if (isDone) {
            builder.setProgress(0, progress, false);
            builder.setContentTitle(context.getResources().getString(R.string.uploaded_image))
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        } else {
            builder.setProgress(100, progress, false)
                    .setContentTitle(context.getResources().getString(R.string.uploading_image))
                    .setDefaults(0)
                    .setSound(null);
        }

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Timber.wtf("notificationmanager null");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(
                    isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                            LOW_IMP_NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    isHighImportanceRequired ? NotificationManager.IMPORTANCE_HIGH :
                            NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLACK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}
            );
            builder.setChannelId(
                    isHighImportanceRequired ? HIGH_IMP_NOTIFICATION_CHANNEL_ID :
                            LOW_IMP_NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
    }
}
