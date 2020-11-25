package de.nulide.findmydevice.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SMSWorkaroundService extends Service {

    private final int ID = 71;

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        String NOTIFICATION_CHANNEL_ID = "de.nulide.findmydevice";
        String channelName = "SMS Service";
        NotificationChannel chan = null;
        chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        nBuilder.setContentTitle("Foreground Workaround")
                .setContentText("Service Workaround")
                .setAutoCancel(true);
        startForeground(ID, nBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent backgroundService = new Intent(this, SMSService.class);
        startService(backgroundService);
        stopSelf();
        return Service.START_NOT_STICKY;
    }
}
