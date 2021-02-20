package de.nulide.findmydevice.data;


import android.media.RingtoneManager;
import android.net.Uri;

import java.util.HashMap;
import java.util.Timer;

import de.nulide.findmydevice.tasks.SaveTimerTask;
import de.nulide.findmydevice.utils.Ringer;

public class Settings extends HashMap<Integer, Object> {

    public static final int newestIntroductionVersion = 2;

    public static final int SET_WIPE_ENABLED = 0;
    public static final int SET_ACCESS_VIA_PIN = 1;
    public static final int SET_LOCKSCREEN_MESSAGE = 2;
    public static final int SET_PIN = 3;
    public static final int SET_FMD_COMMAND = 4;
    public static final int SET_OPENCELLID_API_KEY = 5;
    public static final int SET_INTRODUCTION_VERSION = 6;
    public static final int SET_RINGER_TONE = 7;


    public static final int SET_FIRST_TIME_WHITELIST = 301;
    public static final int SET_FIRST_TIME_CONTACT_ADDED = 302;


    private Timer afterChangeTimer;

    public Settings() {
    }

    public <T> void set(int key, T value) {
        super.put(key, value);
        write(false);
    }

    public Object get(int key) {
        if (super.containsKey(key)) {
            return super.get(key);
        } else {
            switch (key) {
                case SET_WIPE_ENABLED:
                case SET_ACCESS_VIA_PIN:
                case SET_FIRST_TIME_WHITELIST:
                case SET_FIRST_TIME_CONTACT_ADDED:
                    return false;
                case SET_FMD_COMMAND:
                    return "fmd";
                case SET_INTRODUCTION_VERSION:
                    return 0;
                case SET_RINGER_TONE:
                    return Ringer.getDefaultRingtoneAsString();

            }
        }
        return "";
    }


    public boolean isIntroductionPassed() {
        return newestIntroductionVersion == (Integer) get(SET_INTRODUCTION_VERSION);
    }

    public void setIntroductionPassed() {
        set(SET_INTRODUCTION_VERSION, newestIntroductionVersion);
        write(true);
    }

    private void write(boolean write_now) {
        SaveTimerTask saverTask = new SaveTimerTask(this);
        if (write_now) {
            saverTask.write();
        } else {
            if (afterChangeTimer != null) {
                afterChangeTimer.cancel();
            }
        }
        afterChangeTimer = new Timer();
        afterChangeTimer.schedule(saverTask, 300);
    }
}
