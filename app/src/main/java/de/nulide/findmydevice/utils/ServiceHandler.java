package de.nulide.findmydevice.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.nulide.findmydevice.service.SMSService;
import de.nulide.findmydevice.service.SMSWorkaroundService;

public class ServiceHandler {


    public static void restartService(Context context) {
        if(isRunning(context)) {
            Intent backgroundService = new Intent(context, SMSService.class);
            context.stopService(backgroundService);
        }
        startServiceSomehow(context);
    }

    public static void startServiceSomehow(Context context) {
        if (!isRunning(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent foregroundService = new Intent(context, SMSWorkaroundService.class);
                context.startForegroundService(foregroundService);
            } else {
                Intent backgroundService = new Intent(context, SMSService.class);
                context.startService(backgroundService);
            }
        }
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMSService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
