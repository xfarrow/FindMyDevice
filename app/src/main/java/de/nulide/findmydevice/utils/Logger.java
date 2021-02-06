package de.nulide.findmydevice.utils;

import android.util.Log;

import java.util.Calendar;

import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;

public class Logger {

    private static boolean DEBUG;
    private static LogData log;

    public static void init(){
        DEBUG = false;
        log = JSONFactory.convertJSONLog(IO.read(JSONMap.class, IO.logFileName));
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


}
