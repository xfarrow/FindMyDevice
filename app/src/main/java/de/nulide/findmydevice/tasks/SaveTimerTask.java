package de.nulide.findmydevice.tasks;

import java.util.TimerTask;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class SaveTimerTask extends TimerTask {

    Settings settings;
    WhiteList whiteList;

    public SaveTimerTask(Settings settings) {
        this.settings = settings;
    }

    public SaveTimerTask(WhiteList whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public void run() {
        write();
    }

    public void write(){
        if (settings != null) {
            IO.write(JSONFactory.convertSettings(settings), IO.settingsFileName);
        } else if (settings != null) {
            IO.write(JSONFactory.convertWhiteList(whiteList), IO.whiteListFileName);
        }
    }
}
