package de.nulide.findmydevice.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMS extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "FMD_SMS_RECEIVER";

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String format = bundle.getString("format");
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus != null) {
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                Log.d(TAG, "onReceive: " + msgs[i].getOriginatingAddress());
                Log.d(TAG, "msg: " + msgs[i].getMessageBody());
                MessageHandler.handle(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody(), context);
            }
        }

    }

    public static void sendMessage(String destination, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage
                (destination, null, msg,
                        null, null);
    }

}
