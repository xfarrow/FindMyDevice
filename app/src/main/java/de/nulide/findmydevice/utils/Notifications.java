package de.nulide.findmydevice.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.ui.MainActivity;

public class Notifications {

    public static final int CHANNEL_USAGE = 42;
    public static final int CHANNEL_LIFE = 43;
    public static final int CHANNEL_PIN = 44;
    public static final int CHANNEL_SERVER = 45;
    public static final int CHANNEL_FOREGROUND_SERVICE = 99;

    private static boolean silent;


    public static void notify(Context context, String title, String text, int channelID) {
        if (!silent) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, new Integer(channelID).toString())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(channelID, builder.build());
        }
    }

    public static Notification.Builder getForegroundNotification(Context context){
        return new Notification.Builder(context,new Integer(CHANNEL_FOREGROUND_SERVICE).toString())
                .setSmallIcon(R.mipmap.ic_launcher);
    }

    public static void init(Context context, boolean silentWish) {
        silent = silentWish;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(new Integer(CHANNEL_USAGE).toString(), context.getString(R.string.Notification_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription(context.getString(R.string.Notification_Usage_Description));
            NotificationChannel channel2 = new NotificationChannel(new Integer(CHANNEL_LIFE).toString(), context.getString(R.string.Notification_Lifecycle), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(context.getString(R.string.Notification_Lifecycle_Description));
            NotificationChannel channel3 = new NotificationChannel(new Integer(CHANNEL_PIN).toString(), context.getString(R.string.Pin_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(context.getString(R.string.Notification_Pin_Usage_Description));
            NotificationChannel channel4 = new NotificationChannel(new Integer(CHANNEL_SERVER).toString(), context.getString(R.string.Notification_Server), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(context.getString(R.string.NotificationServer_Description));
            NotificationChannel channel99 = new NotificationChannel(new Integer(CHANNEL_FOREGROUND_SERVICE).toString(), "ForegroundTask",  NotificationManager.IMPORTANCE_HIGH);
            channel99.setDescription(context.getString(R.string.Notification_ForegroundService));

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
            notificationManager.createNotificationChannel(channel3);
            notificationManager.createNotificationChannel(channel4);
            notificationManager.createNotificationChannel(channel99);
        }
    }

}
