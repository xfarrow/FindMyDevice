package de.nulide.findmydevice.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.locks.Lock;

import de.nulide.findmydevice.LockScreenMessage;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;

public class MessageHandler {

    public static void handle(String sender, String msg, Context context) {
        IO.context = context;
        Settings settings = IO.read(Settings.class, IO.settingsFileName);
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder("");
        if (msg.startsWith("fmd where are you")) {
            replyBuilder.append("will be send as soon as possible.");
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation();
        } else if (msg.startsWith("fmd ring")) {
            replyBuilder.append("rings");
            if(msg.contains("long")){
                Ringer.ring(context, 180);
            }else{
                Ringer.ring(context, 15);
            }
        } else if (msg.startsWith("fmd lock")) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = lockScreenMessage.getExtras();
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender);
            context.startActivity(lockScreenMessage);
            replyBuilder.append("locked");
        }else if(msg.startsWith("fmd stats")){
            replyBuilder.append("WiFi-Stats:\nIP: ").append(WiFi.getWifiIP(context)).append("\nAvailable Wifi-Networks:\n");
            for(ScanResult sr : WiFi.getWifiNetworks(context)){
                replyBuilder.append(sr.SSID).append("\n");
            }
        } else if (msg.startsWith("fmd delete")) {
            if(settings.isWipeEnabled()) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if(msg.length() > 11) {
                    String pin = msg.substring(11, msg.length());
                    if (pin.equals(settings.getPin().toLowerCase())) {
                        devicePolicyManager.wipeData(0);
                        replyBuilder.append("Goodbye...");
                    } else {
                        replyBuilder.append("False Pin!");
                    }
                }else{
                    replyBuilder.append("Syntax: fmd delete [pin]");
                }
            }
        } else if (msg.startsWith("fmd")) {
            replyBuilder.append("FindMyDevice Commands:\n" +
                    "fmd where are you - sends the current location\n" +
                    "fmd ring - lets the phone ring\n" +
                    "fmd lock - locks the phone\n" +
                    "fmd stats - sends device informations");
            if(settings.isWipeEnabled()){
                replyBuilder.append("\nfmd delete - resets the phone");
            }
        }
        String reply = replyBuilder.toString();
        if (!reply.isEmpty()) {
            SMS.sendMessage(sender, reply);
        }
    }
}
