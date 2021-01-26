package de.nulide.findmydevice.data;


import java.util.HashMap;
import java.util.Timer;

import de.nulide.findmydevice.utils.SaveTimerTask;

public class Settings extends HashMap<Integer, Object> {

    public static final int newestIntroductionVersion = 2;

    public static final int SET_WIPE_ENABLED = 0;
    public static final int SET_ACCESS_VIA_PIN = 1;
    public static final int SET_LOCKSCREEN_MESSAGE = 2;
    public static final int SET_PIN = 3;
    public static final int SET_FMD_COMMAND = 4;
    public static final int SET_OPENCELLID_API_KEY = 5;
    public static final int SET_INTRODUCTION_VERSION = 6;

    private Timer afterChangeTimer;

    public Settings() {
    }

    public <T> void set(int key, T value){
        super.put(key, value);
        write();
    }

    public Object get(int key){
        if(super.containsKey(key)){
            return super.get(key);
        }else{
            switch (key){
                case SET_WIPE_ENABLED:
                case SET_ACCESS_VIA_PIN:
                    return false;
                case SET_FMD_COMMAND:
                    return "fmd";
                case SET_INTRODUCTION_VERSION:
                    return 0;
            }
        }
        return "";
    }




    public boolean isIntroductionPassed() {
        if(newestIntroductionVersion == (Integer)get(SET_INTRODUCTION_VERSION)){
            return true;
        }
        return false;
    }

    public void setIntroductionPassed(){
        set(SET_INTRODUCTION_VERSION, newestIntroductionVersion);
    }

    private void write(){
        if (afterChangeTimer != null) {
            afterChangeTimer.cancel();
        }
        afterChangeTimer = new Timer();
        afterChangeTimer.schedule(new SaveTimerTask(this), 300);
    }
}
