package de.nulide.findmydevice.data.io.json;

public class JSONSettings {

    private boolean wipeEnabled;
    private String lockScreenMessage;
    private String pin;
    private String fmdCommand;
    private String openCellIDAPIkey;
    private int introductionVersionPassed;

    public JSONSettings() {
        lockScreenMessage = "";
        pin = "";
        fmdCommand = "";
        openCellIDAPIkey = "";
    }

    public boolean isWipeEnabled() {
        return wipeEnabled;
    }

    public void setWipeEnabled(boolean wipeEnabled) {
        this.wipeEnabled = wipeEnabled;
    }

    public String getLockScreenMessage() {
        return lockScreenMessage;
    }

    public void setLockScreenMessage(String lockScreenMessage) {
        this.lockScreenMessage = lockScreenMessage;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getFmdCommand() {
        return fmdCommand;
    }

    public void setFmdCommand(String fmdCommand) {
        this.fmdCommand = fmdCommand;
    }

    public String getOpenCellIDAPIkey() {
        return openCellIDAPIkey;
    }

    public void setOpenCellIDAPIkey(String openCellIDAPIkey) {
        this.openCellIDAPIkey = openCellIDAPIkey;
    }

    public int getIntroductionVersionPassed() {
        return introductionVersionPassed;
    }

    public void setIntroductionVersionPassed(int introductionVersionPassed) {
        this.introductionVersionPassed = introductionVersionPassed;
    }
}
