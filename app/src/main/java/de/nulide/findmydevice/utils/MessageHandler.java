package de.nulide.findmydevice.utils;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.collection.ArrayMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.nulide.findmydevice.LockScreenMessage;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;

public class MessageHandler {

    private static int counter = 0;
    private static Settings settings;

    public static void init(Settings set){
        settings = set;
    }

    public static void handle(String sender, String msg, Context context) {
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder();
        if (msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND) + " where are you") && Permission.GPS) {
            if(Permission.WRITE_SECURE_SETTINGS){
                if(!GPS.isGPSOn(context)){
                    GPS.turnOnGPS(context);
                }
            }
            replyBuilder.append(context.getString(R.string.MH_GPS_WILL_FOLLOW));
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation(settings);
        } else if (msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND) + " ring")) {
            replyBuilder.append(context.getString(R.string.MH_rings));
            if (msg.contains("long")) {
                Ringer.ring(context, 180);
            } else {
                Ringer.ring(context, 15);
            }
        } else if (msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND) + " lock") && Permission.DEVICE_ADMIN) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender);
            if (msg.length() > ((String)settings.get(Settings.SET_FMD_COMMAND)).length() + 6) {
                String customMessage = originalMsg.substring(((String)settings.get(Settings.SET_FMD_COMMAND)).length() + 6, msg.length());
                lockScreenMessage.putExtra(LockScreenMessage.CUSTOM_TEXT, customMessage);
            }
            context.startActivity(lockScreenMessage);
            replyBuilder.append(context.getString(R.string.MH_Locked));
        } else if (msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND) + " stats")) {
            replyBuilder.append(context.getString(R.string.MH_Stats));
            Map<String, String> ips = Network.getAllIP();
            Iterator<String> it = ips.keySet().iterator();
            while(it.hasNext()){
                String intf = it.next();
                replyBuilder.append(intf).append(": ").append(ips.get(intf)).append("\n");
            }
            replyBuilder.append(context.getString(R.string.MH_Networks));
            for (ScanResult sr : Network.getWifiNetworks(context)) {
                replyBuilder.append(sr.SSID).append("\n");
            }
        } else if (msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND) + " delete") && Permission.DEVICE_ADMIN) {
            if ((Boolean)settings.get(Settings.SET_WIPE_ENABLED)) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (msg.length() > ((String)settings.get(Settings.SET_FMD_COMMAND)).length() + 8) {
                    String pin = originalMsg.substring(((String)settings.get(Settings.SET_FMD_COMMAND)).length() + 8, msg.length());
                    if (pin.equals((String)settings.get(Settings.SET_PIN))) {
                        devicePolicyManager.wipeData(0);
                        replyBuilder.append(context.getString(R.string.MH_Delete));
                    } else {
                        replyBuilder.append(context.getString(R.string.MH_False_Pin));
                    }
                } else {
                    replyBuilder.append(context.getString(R.string.MH_Syntax)).append((String)settings.get(Settings.SET_FMD_COMMAND)).append(" delete [pin]");
                }
            }
        } else if (msg.equals((String)settings.get(Settings.SET_FMD_COMMAND))) {
            replyBuilder.append(context.getString(R.string.MH_Title_Help)).append("\n");
            if (Permission.GPS) {
                replyBuilder.append((String)settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_where)).append("\n");
            }
            replyBuilder.append((String)settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_ring)).append("\n");
            if (Permission.DEVICE_ADMIN) {
                replyBuilder.append((String)settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Lock)).append("\n");
            }
            replyBuilder.append((String)settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Stats));
            if ((Boolean)settings.get(Settings.SET_WIPE_ENABLED)) {
                replyBuilder.append("\n").append((String)settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_delete));
            }
        }

        String reply = replyBuilder.toString();
        if (!reply.isEmpty()) {
            counter++;
            Notifications.notify(context, "SMS-Receiver", "New Usage " + counter, Notifications.CHANNEL_USAGE);
            SMS.sendMessage(sender, reply);
        }
    }
}
