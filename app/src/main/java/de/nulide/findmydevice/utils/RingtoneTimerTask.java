package de.nulide.findmydevice.utils;

import android.media.Ringtone;

import java.util.Timer;
import java.util.TimerTask;

public class RingtoneTimerTask extends TimerTask {

    private final Timer t;
    private final Ringtone r;
    private int I = 0;

    public RingtoneTimerTask(Timer t, Ringtone r) {
        this.t = t;
        this.r = r;
    }

    @Override
    public void run() {
        if (I == 15) {
            r.stop();
            t.cancel();
        } else {
            I++;
        }
    }
}
