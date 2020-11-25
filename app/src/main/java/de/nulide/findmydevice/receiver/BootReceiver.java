package de.nulide.findmydevice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.nulide.findmydevice.service.SMSForegroundService;
import de.nulide.findmydevice.service.SMSService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SMSService.isRunning(context)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent foregroundService = new Intent(context, SMSForegroundService.class);
                context.startForegroundService(foregroundService);
            }else {
                Intent backgroundService = new Intent(context, SMSService.class);
                context.startService(backgroundService);
            }
        }
    }
}
