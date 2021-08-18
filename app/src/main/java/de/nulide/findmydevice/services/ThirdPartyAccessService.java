package de.nulide.findmydevice.services;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Calendar;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.logic.ComponentHandler;
import de.nulide.findmydevice.sender.NotificationReply;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;

public class ThirdPartyAccessService extends NotificationListenerService {

    protected WhiteList whiteList;
    protected ConfigSMSRec config;

    protected ComponentHandler ch;

    protected void init(Context context) {
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
        ch = new ComponentHandler(settings, context, null, null);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        init(this);
        NotificationReply sender = new NotificationReply(this, sbn);
        if(sender.canSend()){
            ch.setSender(sender);
            String msg = sbn.getNotification().extras.getCharSequence("android.text").toString();
            String msgLower = msg.toLowerCase();
            String fmdcommand = (String)ch.getSettings().get(Settings.SET_FMD_COMMAND);
            if(msgLower.contains(fmdcommand)){
                msg = ch.getMessageHandler().checkAndRemovePin(msg);
                if(msg != null) {
                    ch.getMessageHandler().handle(msg, this);
                    cancelNotification(sbn.getKey());
                }
            }
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }



}
