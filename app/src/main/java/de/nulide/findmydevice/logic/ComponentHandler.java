package de.nulide.findmydevice.logic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Handler;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.services.GPSTimeOutService;

public class ComponentHandler {

    private Settings settings;
    private Sender sender;
    private Context context;
    private JobService FmdServerService;
    private JobParameters FmdServerServiceParams;

    private LocationHandler locationHandler;
    private MessageHandler messageHandler;

    public ComponentHandler(de.nulide.findmydevice.data.Settings settings, Context context, JobService service, JobParameters serviceParams) {
        this.settings = settings;
        this.context = context;
        messageHandler = new MessageHandler(this);
        locationHandler = new LocationHandler(this);
        this.FmdServerService = service;
        this.FmdServerServiceParams = serviceParams;
    }

    public Settings getSettings() {
        return settings;
    }

    public Sender getSender() {
        return sender;
    }

    public Context getContext() {
        return context;
    }

    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void finishJob(){
        if(FmdServerService != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FmdServerService.jobFinished(FmdServerServiceParams, false);
                    GPSTimeOutService.cancleJob(context);
                }
            }, 10000);
        }
    }
}
