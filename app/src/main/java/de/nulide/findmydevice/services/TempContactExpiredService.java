package de.nulide.findmydevice.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.TimerTask;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.sender.SMS;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.Logger;

public class TempContactExpiredService extends JobService {

    public final static String SENDER_TYPE = "sender";
    public static final String DESTINATION = "dest";

    @Override
    public boolean onStartJob(JobParameters params) {
        Sender sender = null;
        ConfigSMSRec config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        String destination = (String) config.get(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT);
        if(destination != null && !destination.isEmpty()) {
            sender = new SMS(destination);
        }

        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        if(sender != null) {
            sender.sendNow("FindMyDevive: Pin expired!");
            Logger.logSession("Session expired", sender.getDestination());
        }
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context, Sender sender) {
        ComponentName serviceComponent = new ComponentName(context, TempContactExpiredService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(10 * 1000 * 60);
        builder.setOverrideDeadline(15 * 1000 * 60);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }




}
