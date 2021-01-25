package de.nulide.findmydevice.data;


import java.util.HashMap;
import java.util.Timer;

import de.nulide.findmydevice.utils.SaveTimerTask;

public class Settings extends HashMap<String, Object> {

    public static final int newestIntroductionVersion = 2;

    public static final String SET_WIPE_ENABLED = "SET_WIPE_ENABLED";
    public static final String SET_ACCESS_VIA_PIN = "SET_ACCESS_VIA_PIN";
    public static final String SET_LOCKSCREEN_MESSAGE = "SET_LOCKSCREEN_MESSAGE";
    public static final String SET_PIN = "SET_PIN";
    public static final String SET_FMD_COMMAND = "SET_FMD_COMMAND";
    public static final String SET_OPENCELLID_API_KEY = "SET_OPENCELLID_API_KEY";
    public static final String SET_INTRODUCTION_VERSION = "SET_INTRODUCTIONVERSION_PASSED";

    private Timer afterChangeTimer;

    public Settings() {
    }

    public <T> void set(String key, T value){
        super.put(key, value);
        write();
    }

    public Object get(String key){
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
