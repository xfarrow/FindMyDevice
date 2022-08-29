package de.nulide.findmydevice.services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.RequiresApi;

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
import de.nulide.findmydevice.logic.MessageHandler;
import de.nulide.findmydevice.sender.SMS;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;

public class FMDSMSService extends JobService {

    private static final int JOB_ID = 107;

    private static final String DESTINATION = "dest";
    private static final String MESSAGE = "msg";
    private static final String TIME = "time";

    private WhiteList whiteList;
    private ComponentHandler ch;
    private ConfigSMSRec config;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context, String destination, String message, Long time) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DESTINATION, destination);
        bundle.putString(MESSAGE, message);
        bundle.putLong(TIME, time);

        ComponentName serviceComponent = new ComponentName(context, FMDSMSService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent)
                .setExtras(bundle);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(0);

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onStartJob(JobParameters params) {
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        if (config.get(ConfigSMSRec.CONF_LAST_USAGE) == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            config.set(ConfigSMSRec.CONF_LAST_USAGE, cal.getTimeInMillis());
        }
        Notifications.init(this, false);
        Permission.initValues(this);
        ch = new ComponentHandler(settings, this, this, params);
        String receiver = params.getExtras().getString(DESTINATION);
        String msg = params.getExtras().getString(MESSAGE);
        Long time = params.getExtras().getLong(TIME);
        ch.setSender(new SMS(receiver));
        boolean inWhitelist = false;
        String executedCommand = "";

        // check if number is in the whitelist. If true, execute the command.
        for (int iwl = 0; iwl < whiteList.size(); iwl++) {
            if (PhoneNumberUtils.compare(whiteList.get(iwl).getNumber(), receiver)) {
                Logger.logSession("Usage", receiver + " used FMD");
                executedCommand = ch.getMessageHandler().handle(msg, this);
                inWhitelist = true;
                // break; // can we break here?
            }
        }

        // check if set access via pin (tmp whitelist) is enabled and PIN is not blank
        if ((Boolean) ch.getSettings().get(Settings.SET_ACCESS_VIA_PIN) && !((String)ch.getSettings().get(Settings.SET_PIN)).isEmpty()) {
            String tempContact = (String) config.get(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT);
            if (!inWhitelist && tempContact != null && PhoneNumberUtils.compare(tempContact, receiver)) {
                Logger.logSession("Usage", receiver + " used FMD");
                executedCommand = ch.getMessageHandler().handle(msg, this);
                inWhitelist = true;
            }
            if (!inWhitelist && ch.getMessageHandler().checkForPin(msg)) {
                Logger.logSession("Usage", receiver + " used the Pin");
                ch.getSender().sendNow(getString(R.string.MH_Pin_Accepted));
                Notifications.notify(this, "Pin", "The pin was used by the following number: "+receiver+"\nPlease change the Pin!", Notifications.CHANNEL_PIN);
                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, receiver);
                config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, time);
                TempContactExpiredService.scheduleJob(this, ch.getSender());
            }
        }

        if(executedCommand == MessageHandler.COM_LOCATE) {
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
