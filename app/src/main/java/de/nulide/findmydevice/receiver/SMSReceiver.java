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
import de.nulide.findmydevice.utils.Notifications;

public class SMSReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String RELOAD_DATA = "de.nulide.reload.data";

    private WhiteList whitelist;
    private Date timeUntilNextUsage;

    public SMSReceiver() {
        timeUntilNextUsage = Calendar.getInstance().getTime();

    }

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(whitelist == null){
            init(context);
        }
        if (intent.getAction().equals(SMS_RECEIVED)) {
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
        }else if(intent.getAction().equals(BOOT_COMPLETED)){
            init(context);
            Notifications.notify(context, "AfterBootTest", "Service running", Notifications.CHANNEL_LIFE);
        }else if(intent.getAction().equals(RELOAD_DATA)){
            init(context);
        }
    }

    private void init(Context context){
        IO.context = context;
        whitelist = (WhiteList) IO.read(WhiteList.class, IO.whiteListFileName);
        MessageHandler.init(context);
    }

}
