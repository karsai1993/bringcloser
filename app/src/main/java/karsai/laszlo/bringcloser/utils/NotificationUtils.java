package karsai.laszlo.bringcloser.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.ui.screens.main.MainActivity;

/**
 * Created by Laci on 10/06/2018.
 */

public class NotificationUtils {

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String NOTIFICATION_CHANNEL_NAME = "bring_closer_notifications";

    public static Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addNotification(
            Context context,
            String title,
            String message,
            String imageUrl,
            int notificationId,
            String action) {

        Intent targetIntent = new Intent(context, MainActivity.class);
        targetIntent.setAction(action);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                targetIntent,
                0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(
                        imageUrl != null ?
                                getBitmapFromUrl(imageUrl) :
                                BitmapFactory.decodeResource(
                                        context.getResources(),
                                        R.drawable.baseline_face_black_48
                                )
                ).setColor(context.getResources().getColor(R.color.colorAccent))
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    importance
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
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
