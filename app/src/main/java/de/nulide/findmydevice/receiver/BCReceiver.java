package de.nulide.findmydevice.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

import java.util.Calendar;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.logic.ComponentHandler;
import de.nulide.findmydevice.logic.LocationHandler;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.services.FMDSMSService;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.services.TempContactExpiredService;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.logic.MessageHandler;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.sender.SMS;

public class BCReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    private WhiteList whiteList;
    private ConfigSMSRec config;

    private ComponentHandler ch;

    public BCReceiver() {
    }

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Calendar time = Calendar.getInstance();
            time.add(Calendar.SECOND, -2);
            if (time.getTimeInMillis() > ((Long) config.get(ConfigSMSRec.CONF_LAST_USAGE))) {
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
                        FMDSMSService.scheduleJob(context, receiver, msgs[i].getMessageBody(), time.getTimeInMillis());
                    }
                }
                Calendar now = Calendar.getInstance();
                config.set(ConfigSMSRec.CONF_LAST_USAGE, now.getTime());
            }
        } else if (intent.getAction().equals(BOOT_COMPLETED)) {
            Notifications.notify(context, "AfterBootTest", "Receiver is working", Notifications.CHANNEL_LIFE);
            Logger.logSession("AfterBootTest", "passed");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            if((Boolean)ch.getSettings().get(Settings.SET_FMDSERVER)){
                FMDServerService.scheduleJob(context, 0);
            }
        }
        Logger.writeLog();
    }

    private void init(Context context) {
        IO.context = context;
        Logger.init(Thread.currentThread(), context);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(context, false);
        Permission.initValues(context);
        ch = new ComponentHandler(settings, context);
    }

}
