package de.nulide.findmydevice.data;

import de.nulide.findmydevice.data.io.IO;

public class Settings {

    private boolean wipeEnabled;
    private String lockScreenMessage;
    private String pin;

    public Settings() {
        lockScreenMessage = new String();
        pin = new String();
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
}
