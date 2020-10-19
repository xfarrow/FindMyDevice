package de.nulide.findmydevice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.nulide.findmydevice.service.SMSService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent backgroundService = new Intent(context, SMSService.class);
        context.startService(backgroundService);
    }
}
