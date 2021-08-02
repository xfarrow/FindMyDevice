package de.nulide.findmydevice.receiver;

import android.content.Context;
import android.content.Intent;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.services.FMDServerCommandService;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;

public class BootReceiver extends SuperReceiver{

    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equals(BOOT_COMPLETED)) {
            Notifications.notify(context, "AfterBootTest", "Receiver is working", Notifications.CHANNEL_LIFE);
            Logger.logSession("AfterBootTest", "passed");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            ch.getSettings().set(Settings.SET_GPS_STATE, 1);
            if((Boolean)ch.getSettings().get(Settings.SET_FMDSERVER)){
                FMDServerService.scheduleJob(context, 0);
                FMDServerCommandService.scheduleJob(context);
            }
        }
    }

}
