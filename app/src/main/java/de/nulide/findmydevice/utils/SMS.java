package de.nulide.findmydevice.utils;

import android.telephony.SmsManager;

import java.util.ArrayList;

public class SMS {

    public static void sendMessage(String destination, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        if (msg.length() <= 160) {
            smsManager.sendTextMessage
                    (destination, null, msg,
                            null, null);
        } else {
            ArrayList<String> parts = smsManager.divideMessage(msg);
            smsManager.sendMultipartTextMessage(destination, null, parts, null, null);
        }
    }
}
