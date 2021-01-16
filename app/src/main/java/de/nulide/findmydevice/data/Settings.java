package de.nulide.findmydevice.data;


import de.nulide.findmydevice.data.io.IO;

public class Settings {

    public static final int newestIntroductionVersion = 2;

    private boolean wipeEnabled;
    private String lockScreenMessage;
    private String pin;
    private String fmdCommand;
    private int introductionVersionPassed;

    public Settings() {
        lockScreenMessage = new String();
        pin = new String();
        fmdCommand = new String("fmd");
        introductionVersionPassed = 0;
    }

    public boolean isWipeEnabled() {
        return wipeEnabled;
    }

    public void setWipeEnabled(boolean wipeEnabled) {
        this.wipeEnabled = wipeEnabled;
        IO.write(this, IO.settingsFileName);
    }

    public String getLockScreenMessage() {
        return lockScreenMessage;
    }

    public void setLockScreenMessage(String lockScreenMessage) {
        this.lockScreenMessage = lockScreenMessage;
        IO.write(this, IO.settingsFileName);
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
        IO.write(this, IO.settingsFileName);
    }

    public String getFmdCommand() {
        return fmdCommand;
    }

    public void setFmdCommand(String fmdCommand) {
        this.fmdCommand = fmdCommand.toLowerCase();
        IO.write(this, IO.settingsFileName);
    }

    public int getIntroductionVersionPassed() {
        return introductionVersionPassed;
    }

    public void setIntroductionVersionPassed(int introductionVersionPassed) {
        this.introductionVersionPassed = introductionVersionPassed;
        IO.write(this, IO.settingsFileName);
    }

    public boolean isIntroductionPassed() {
        if(newestIntroductionVersion == introductionVersionPassed){
            return true;
        }
        return false;
    }

    public void setIntroductionPassed() {
        this.introductionVersionPassed = newestIntroductionVersion;
        IO.write(this, IO.settingsFileName);
    }
}
