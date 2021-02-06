package de.nulide.findmydevice.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;

import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;

public class Logger implements Thread.UncaughtExceptionHandler {

    private static boolean DEBUG;
    private static LogData log;

    public static void init(Thread t){
        DEBUG = false;
        log = JSONFactory.convertJSONLog(IO.read(JSONMap.class, IO.logFileName));
        Logger logger = new Logger();
        t.setUncaughtExceptionHandler(logger);
    }

    public static void setDebuggingMode(boolean debug){
        DEBUG = debug;
    }

    public static void log(String title, String msg){
        StringBuilder logText = new StringBuilder();
        logText.append(Calendar.getInstance().getTime().toString()).append(": ").append(title)
                .append(" - ").append(msg);
        log.add(logText.toString());
        if(DEBUG){
            Log.d(title, msg);
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        log(t.getName(), e.getMessage());
    }
}
