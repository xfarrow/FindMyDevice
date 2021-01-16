package de.nulide.findmydevice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.utils.ServiceHandler;

public class ServiceStoppedReceiver extends BroadcastReceiver {

    public static final String SERVICE_STOPPED = "de.nulide.findmydevice.service.stopped";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SERVICE_STOPPED)) {
            ServiceHandler.startServiceSomehow(context);
        }
    }
}
