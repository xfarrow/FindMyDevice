package de.nulide.findmydevice.data;


import java.util.Timer;
import java.util.TimerTask;

import de.nulide.findmydevice.MainActivity;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.utils.SaveTimerTask;

public class Settings {

    public static final int newestIntroductionVersion = 2;

    private boolean wipeEnabled;
    private String lockScreenMessage;
    private String pin;
    private String fmdCommand;
    private String openCellIDAPIkey;
    private int introductionVersionPassed;

    private Timer afterChangeTimer;

    public Settings() {
        lockScreenMessage = new String();
        pin = new String();
        fmdCommand = new String("fmd");
        openCellIDAPIkey = new String();
        introductionVersionPassed = 0;
    }

    public Settings(boolean wipeEnabled, String lockScreenMessage, String pin, String fmdCommand, String openCellIDAPIkey, int introductionVersionPassed) {
        this.wipeEnabled = wipeEnabled;
        this.lockScreenMessage = lockScreenMessage;
        this.pin = pin;
        this.fmdCommand = fmdCommand;
        this.openCellIDAPIkey = openCellIDAPIkey;
        this.introductionVersionPassed = introductionVersionPassed;
    }

    public boolean isWipeEnabled() {
        return wipeEnabled;
    }

    public void setWipeEnabled(boolean wipeEnabled) {
        this.wipeEnabled = wipeEnabled;
        write();
    }

    public String getLockScreenMessage() {
        return lockScreenMessage;
    }

    public void setLockScreenMessage(String lockScreenMessage) {
        this.lockScreenMessage = lockScreenMessage;
        write();
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
        write();
    }

    public String getFmdCommand() {
        return fmdCommand;
    }

    public void setFmdCommand(String fmdCommand) {
        this.fmdCommand = fmdCommand.toLowerCase();
        write();
    }

    public int getIntroductionVersionPassed() {
        return introductionVersionPassed;
    }

    public void setIntroductionVersionPassed(int introductionVersionPassed) {
        this.introductionVersionPassed = introductionVersionPassed;
        write();
    }

    public boolean isIntroductionPassed() {
        if(newestIntroductionVersion == introductionVersionPassed){
            return true;
        }
        return false;
    }

    public void setIntroductionPassed() {
        this.introductionVersionPassed = newestIntroductionVersion;
        write();
    }

    public String getOpenCellIDAPIkey() {
        return openCellIDAPIkey;
    }

    public void setOpenCellIDAPIkey(String openCellIDAPIkey) {
        this.openCellIDAPIkey = openCellIDAPIkey;
        write();
    }

    private void write(){
        if (afterChangeTimer != null) {
            afterChangeTimer.cancel();
        }
        afterChangeTimer = new Timer();
        afterChangeTimer.schedule(new SaveTimerTask(this), 300);
    }
}
