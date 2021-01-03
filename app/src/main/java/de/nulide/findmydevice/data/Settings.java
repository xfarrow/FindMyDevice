package de.nulide.findmydevice.data;

import de.nulide.findmydevice.data.io.IO;

public class Settings {

    private boolean wipeEnabled;
    private String lockScreenMessage;
    private String pin;
    private String fmdCommand;

    public Settings() {
        lockScreenMessage = new String();
        pin = new String();
        fmdCommand = new String("fmd");
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
}
