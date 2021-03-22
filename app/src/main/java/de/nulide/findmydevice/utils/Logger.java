package de.nulide.findmydevice.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import de.nulide.findmydevice.data.io.json.JSONLog;
import de.nulide.findmydevice.ui.CrashedActivity;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class Logger implements Thread.UncaughtExceptionHandler{

    private static boolean DEBUG;
    private static LogData log;
    private static Context context;
    private static StringBuilder logText;

    public static void init(Thread t, Context con){
        DEBUG = false;
        log = JSONFactory.convertJSONLog(IO.read(JSONLog.class, IO.logFileName));
        context = con;
        logText = new StringBuilder();
        Logger logger = new Logger();
        t.setUncaughtExceptionHandler(logger);
    }

    public static void setDebuggingMode(boolean debug){
        DEBUG = debug;
    }

    public static void log(String title, String msg){
        logText.append(title).append(" - ").append(msg);
        writeLog();
        if(DEBUG){
            Log.d(title, msg);
        }
    }

    public static void logSession(String title,String msg){
        if(!logText.toString().isEmpty()) {
            logText.append("\n");
        }
        logText.append(title).append(" - ").append(msg);
        if(DEBUG){
            Log.d(title, msg);
        }
    }

    public static void writeLog(){
        if(!logText.toString().isEmpty()) {
            log.add(Calendar.getInstance().getTimeInMillis(), logText.toString());
            logText = new StringBuilder();
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        log(t.getName(), createNiceCrashLog(e));
        Intent crash = new Intent(context, CrashedActivity.class);
        crash.putExtra(CrashedActivity.CRASH_LOG, createNiceCrashLog(e));
        context.startActivity(crash);
    }

    public String createNiceCrashLog(Throwable e){
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String newLine= "\n";
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
        }
        report.append(newLine);
        report.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(newLine);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append(arr[i].toString());
                report.append(newLine);
            }
        }
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(newLine);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(newLine);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(newLine);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(newLine);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(newLine);
        report.append(newLine);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append(newLine);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(newLine);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(newLine);

        return report.toString();
    }

}
