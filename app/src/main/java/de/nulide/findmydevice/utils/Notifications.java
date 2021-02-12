package de.nulide.findmydevice.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.nulide.findmydevice.R;

public class Notifications {

    public static final int CHANNEL_USAGE = 42;
    public static final int CHANNEL_LIFE = 43;
    public static final int CHANNEL_PIN = 44;


    public static void notify(Context context, String title, String text, int channelID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, new Integer(channelID).toString())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(channelID, builder.build());
    }

    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(new Integer(CHANNEL_USAGE).toString(), context.getString(R.string.Notification_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription(context.getString(R.string.Notification_Usage_Description));
            NotificationChannel channel2 = new NotificationChannel(new Integer(CHANNEL_LIFE).toString(), context.getString(R.string.Notification_Lifecycle), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(context.getString(R.string.Notification_Lifecycle_Description));
            NotificationChannel channel3 = new NotificationChannel(new Integer(CHANNEL_PIN).toString(), context.getString(R.string.Pin_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription(context.getString(R.string.Notification_Pin_Usage_Description));
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
            notificationManager.createNotificationChannel(channel3);
        }
    }

}
