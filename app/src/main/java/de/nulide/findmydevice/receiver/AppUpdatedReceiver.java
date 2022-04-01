package de.nulide.findmydevice.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import org.unifiedpush.android.connector.UnifiedPush;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.services.FMDServerCommandService;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;

public class AppUpdatedReceiver extends SuperReceiver {

    public static final String APP_UPDATED = "android.intent.action.MY_PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
    init(context);
    if (intent.getAction().equals(APP_UPDATED)){
            Notifications.notify(context, "App Update", "Receiver is working", Notifications.CHANNEL_LIFE);
            Logger.logSession("AppUpdate", "restarted");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            settings.updateSettings();

            if((Boolean)ch.getSettings().get(Settings.SET_FMDSERVER_UPLOAD_SERVICE)){
                FMDServerService.scheduleJob(context, 0);

            }
        }
    }

}
