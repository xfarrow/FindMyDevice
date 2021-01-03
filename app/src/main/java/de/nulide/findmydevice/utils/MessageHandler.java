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
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder("");
        if (msg.startsWith(settings.getFmdCommand() + " where are you")) {
            replyBuilder.append("will be send as soon as possible.");
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation();
        } else if (msg.startsWith(settings.getFmdCommand() + " ring")) {
            replyBuilder.append("rings");
            if(msg.contains("long")){
                Ringer.ring(context, 180);
            }else{
                Ringer.ring(context, 15);
            }
        } else if (msg.startsWith(settings.getFmdCommand() + " lock")) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = lockScreenMessage.getExtras();
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender);
            context.startActivity(lockScreenMessage);
            replyBuilder.append("locked");
        }else if(msg.startsWith(settings.getFmdCommand() + " stats")){
            replyBuilder.append("WiFi-Stats:\nIP: ").append(WiFi.getWifiIP(context)).append("\nAvailable Wifi-Networks:\n");
            for(ScanResult sr : WiFi.getWifiNetworks(context)){
                replyBuilder.append(sr.SSID).append("\n");
            }
        } else if (msg.startsWith(settings.getFmdCommand() + " delete")) {
            if(settings.isWipeEnabled()) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if(msg.length() > settings.getFmdCommand().length() + 8) {
                    String pin = originalMsg.substring(settings.getFmdCommand().length() + 8, msg.length());
                    if (pin.equals(settings.getPin())) {
                        devicePolicyManager.wipeData(0);
                        replyBuilder.append("Goodbye...");
                    } else {
                        replyBuilder.append("False Pin!");
                    }
                }else{
                    replyBuilder.append("Syntax: ").append(settings.getFmdCommand()).append(" delete [pin]");
                }
            }
        } else if (msg.equals(settings.getFmdCommand())) {
            replyBuilder.append("FindMyDevice Commands:\n")
                    .append(settings.getFmdCommand()).append(" where are you - sends the current location\n")
                    .append(settings.getFmdCommand()).append(" ring - lets the phone ring\n")
                    .append(settings.getFmdCommand()).append(" lock - locks the phone\n")
                    .append(settings.getFmdCommand()).append(" stats - sends device informations");
            if(settings.isWipeEnabled()){
                replyBuilder.append("\n").append(settings.getFmdCommand()).append(" delete - resets the phone");
            }
        }
        String reply = replyBuilder.toString();
        if (!reply.isEmpty()) {
            SMS.sendMessage(sender, reply);
        }
    }
}
