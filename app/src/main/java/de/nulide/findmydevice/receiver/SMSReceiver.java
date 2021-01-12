package de.nulide.findmydevice.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.Calendar;
import java.util.Date;

import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.utils.MessageHandler;

public class SMSReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private final WhiteList whitelist;
    private Date timeUntilNextUsage;

    public SMSReceiver() {
        whitelist = (WhiteList) IO.read(WhiteList.class, IO.whiteListFileName);
        timeUntilNextUsage = Calendar.getInstance().getTime();
    }

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        Date time = Calendar.getInstance().getTime();
        if (time.getTime() > timeUntilNextUsage.getTime()) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String format = bundle.getString("format");
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                boolean isVersionM =
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    if (isVersionM) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String receiver = msgs[i].getOriginatingAddress();
                    receiver.replace(" ", "");
                    for (int iwl = 0; iwl < whitelist.size(); iwl++) {
                        if (receiver.equals(whitelist.get(iwl).getNumber())) {
                            MessageHandler.handle(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody(), context);
                        }
                    }
                }
            }
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, 1);
            timeUntilNextUsage = now.getTime();
        }
    }

}
