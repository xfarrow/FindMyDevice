package de.nulide.findmydevice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.nulide.findmydevice.utils.ServiceHandler;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceHandler.startServiceSomehow(context);
    }
}
