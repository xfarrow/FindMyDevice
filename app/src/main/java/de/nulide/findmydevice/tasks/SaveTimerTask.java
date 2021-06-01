package de.nulide.findmydevice.tasks;

import java.util.TimerTask;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class SaveTimerTask extends TimerTask {

    private Settings Settings;
    private WhiteList whiteList;

    public SaveTimerTask(Settings Settings) {
        this.Settings = Settings;
    }

    public SaveTimerTask(WhiteList whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public void run() {
        write();
    }

    public void write(){
        if (Settings != null) {
            IO.write(JSONFactory.convertSettings(Settings), IO.settingsFileName);
        } else if (Settings != null) {
            IO.write(JSONFactory.convertWhiteList(whiteList), IO.whiteListFileName);
        }
    }
}
