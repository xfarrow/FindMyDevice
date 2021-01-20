package de.nulide.findmydevice.utils;

import android.content.Context;
import android.content.Intent;

import de.nulide.findmydevice.receiver.SMSReceiver;

public class ReceiverHandler {


    public static void reloadData(Context context) {
        Intent broadcastIntent = new Intent(context, SMSReceiver.class).setAction(SMSReceiver.RELOAD_DATA);
        context.sendBroadcast(broadcastIntent);
    }

}
