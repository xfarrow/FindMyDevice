package de.nulide.findmydevice.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.receiver.SMSReceiver;
import de.nulide.findmydevice.receiver.ServiceStoppedReceiver;
import de.nulide.findmydevice.utils.Permission;

public class SMSService extends Service {

    private SMSReceiver rec;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IO.context = this.getApplicationContext();
        rec = new SMSReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(rec, new IntentFilter(SMSReceiver.SMS_RECEIVED));
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(rec);
        Intent broadcastIntent = new Intent(this, ServiceStoppedReceiver.class).setAction(ServiceStoppedReceiver.SERVICE_STOPPED);
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }
}
