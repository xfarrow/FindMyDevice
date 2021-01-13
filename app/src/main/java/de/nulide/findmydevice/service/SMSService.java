package de.nulide.findmydevice.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.receiver.SMSReceiver;

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
        registerReceiver(rec, new IntentFilter(SMSReceiver.SMS_RECEIVED));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(rec);
        super.onDestroy();
    }
}
