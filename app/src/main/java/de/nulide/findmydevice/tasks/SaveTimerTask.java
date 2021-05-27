package de.nulide.findmydevice.tasks;

import java.util.TimerTask;

import de.nulide.findmydevice.data.FMDSettings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class SaveTimerTask extends TimerTask {

    private FMDSettings FMDSettings;
    private WhiteList whiteList;

    public SaveTimerTask(FMDSettings FMDSettings) {
        this.FMDSettings = FMDSettings;
    }

    public SaveTimerTask(WhiteList whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public void run() {
        write();
    }

    public void write(){
        if (FMDSettings != null) {
            IO.write(JSONFactory.convertSettings(FMDSettings), IO.settingsFileName);
        } else if (FMDSettings != null) {
            IO.write(JSONFactory.convertWhiteList(whiteList), IO.whiteListFileName);
        }
    }
}
