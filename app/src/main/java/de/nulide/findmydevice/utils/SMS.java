package de.nulide.findmydevice.utils;

import android.telephony.SmsManager;

public class SMS {

    public static void sendMessage(String destination, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage
                (destination, null, msg,
                        null, null);
    }
}
