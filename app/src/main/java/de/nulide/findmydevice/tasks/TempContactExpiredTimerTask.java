package de.nulide.findmydevice.tasks;

import android.content.Context;
import android.util.Log;

import java.util.TimerTask;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.Logger;

public class TempContactExpiredTimerTask extends TimerTask {

    private Sender sender;
    private Context context;

    public TempContactExpiredTimerTask(Context context, Sender sender){
        IO.context = context;
        this.context = context;
        this.sender = sender;
    }

    @Override
    public void run() {
        Logger.init(Thread.currentThread(), context);
        sender.sendNow("FindMyDevive: Pin expired!");
        Logger.logSession("Session expired", sender.getDestination());
        ConfigSMSRec config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
    }
}
