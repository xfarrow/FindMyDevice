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
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.utils.MessageHandler;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.utils.SMS;

public class SMSReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    private WhiteList whiteList;
    private Settings settings;
    private ConfigSMSRec config;

    public SMSReceiver() {
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
                        boolean inWhitelist = false;
                        for (int iwl = 0; iwl < whiteList.size(); iwl++) {
                            if (PhoneNumberUtils.compare(whiteList.get(iwl).getNumber(), receiver)) {
                                MessageHandler.handle(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody(), context);
                                inWhitelist = true;
                            }
                        }
                        if ((Boolean) settings.get(Settings.SET_ACCESS_VIA_PIN)) {
                            String tempContact = (String) config.get(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT);
                            Long tempContactActiveSince = (Long) config.get(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE);
                            if (tempContactActiveSince != null && tempContactActiveSince + (5 * 60 * 1000) < time.getTimeInMillis()) {
                                SMS.sendMessage(tempContact, "FindMyDevive: Pin expired!");
                                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
                                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
                                tempContact = null;
                            }
                            if (!inWhitelist && tempContact != null && PhoneNumberUtils.compare(tempContact, receiver)) {
                                MessageHandler.handle(receiver, msgs[i].getMessageBody(), context);
                                inWhitelist = true;
                            }
                            if (!inWhitelist && MessageHandler.checkForPin(msgs[i].getMessageBody())) {
                                SMS.sendMessage(receiver, context.getString(R.string.PinAccepted));
                                Notifications.notify(context, "Pin", "The pin was used by the following number: "+receiver+"\nPlease change the Pin!", Notifications.CHANNEL_PIN);
                                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, receiver);
                                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, time.getTimeInMillis());
                                MessageHandler.handle(receiver, msgs[i].getMessageBody(), context);
                            }
                        }
                    }
                }

                Calendar now = Calendar.getInstance();
                config.set(ConfigSMSRec.CONF_LAST_USAGE, now.getTime());
            }
        } else if (intent.getAction().equals(BOOT_COMPLETED)) {
            Notifications.notify(context, "AfterBootTest", "Receiver is working", Notifications.CHANNEL_LIFE);
        }
    }

    private void init(Context context) {
        IO.context = context;
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        settings = JSONFactory.convertJSONSettings(IO.read(JSONSettings.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONSettings.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(context);
        Permission.initValues(context);
        MessageHandler.init(settings);
    }

}
