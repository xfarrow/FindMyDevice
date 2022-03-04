package de.nulide.findmydevice.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;

import androidx.annotation.RequiresApi;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.SecureSettings;

public class GPSTimeOutService extends JobService {

    private static final int JOB_ID = 409;

    @Override
    public boolean onStartJob(JobParameters params) {
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        Logger.logSession("GPS", "GPS timed out.");
        if(((Integer)settings.get(Settings.SET_GPS_STATE)) == 2){
            settings.set(Settings.SET_GPS_STATE, 0);
            SecureSettings.turnGPS(this, false);
            Logger.logSession("GPS", "turned off");
        }
        Logger.writeLog();
        FMDServerService.scheduleJob(this, (Integer)settings.get(Settings.SET_FMDSERVER_UPDATE_TIME));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, GPSTimeOutService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(7 * 1000 * 60);
        builder.setOverrideDeadline(10 * 1000 * 60);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    public static void cancleJob(Context context){
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if(jobScheduler.getPendingJob(JOB_ID) != null) {
            jobScheduler.cancel(JOB_ID);
        }
    }
}
