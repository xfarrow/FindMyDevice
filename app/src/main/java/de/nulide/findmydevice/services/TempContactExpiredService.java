package de.nulide.findmydevice.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.TimerTask;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.Logger;

public class TempContactExpiredService extends Service {

    private Sender sender;
    public final static String SENDER = "sender";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        sender = (Sender) intent.getSerializableExtra(SENDER);

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.init(Thread.currentThread(), this);
        if(intent.getExtras().getSerializable(SENDER) != null) {
            sender = (Sender) intent.getExtras().getSerializable(SENDER);
            sender.sendNow("FindMyDevive: Pin expired!");
            Logger.logSession("Session expired", sender.getDestination());
        }
        ConfigSMSRec config = JSONFactory.convertJSONConfig(IO.read(JSONMap.class, IO.SMSReceiverTempData));
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
        config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
        return super.onStartCommand(intent, flags, startId);
    }


}
