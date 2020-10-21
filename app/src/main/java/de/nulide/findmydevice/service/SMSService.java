package de.nulide.findmydevice.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import de.nulide.findmydevice.data.WhiteList;
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
        super.onDestroy();
        unregisterReceiver(rec);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSService.class.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }


}
