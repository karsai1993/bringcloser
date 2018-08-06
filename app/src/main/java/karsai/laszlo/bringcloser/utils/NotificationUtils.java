package karsai.laszlo.bringcloser.utils;

import android.annotation.TargetApi;
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
import android.support.media.ExifInterface;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.ConnectionDetail;
import karsai.laszlo.bringcloser.activity.ConnectionActivity;
import karsai.laszlo.bringcloser.activity.MainActivity;
import karsai.laszlo.bringcloser.activity.ReceivedDetailsActivity;
import timber.log.Timber;

/**
 * Created by Laci on 10/06/2018.
 * Util to handle notification requests
 */
public class NotificationUtils {

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String NOTIFICATION_CHANNEL_NAME = "bring_closer_notifications";

    public static Bitmap getBitmapFromUrl(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            int rotDegree = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rotDegree = getExifRotation(connection.getInputStream());
            }
            connection.disconnect();
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
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
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
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


    public static void addNotification(
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
        builder.setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(
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
                )
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
}
