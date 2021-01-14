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
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;

public class MessageHandler {

    private static int counter = 0;

    public static void handle(String sender, String msg, Context context) {
        IO.context = context;
        Settings settings = IO.read(Settings.class, IO.settingsFileName);
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder();
        if (msg.startsWith(settings.getFmdCommand() + " where are you") && Permission.GPS) {
            if(Permission.WRITE_SECURE_SETTINGS){
                if(!GPS.isGPSOn(context)){
                    GPS.turnOnGPS(context);
                }
            }
            replyBuilder.append("will be send as soon as possible.");
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation();
        } else if (msg.startsWith(settings.getFmdCommand() + " ring")) {
            replyBuilder.append("rings");
            if (msg.contains("long")) {
                Ringer.ring(context, 180);
            } else {
                Ringer.ring(context, 15);
            }
        } else if (msg.startsWith(settings.getFmdCommand() + " lock") && Permission.DEVICE_ADMIN) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = lockScreenMessage.getExtras();
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender);
            context.startActivity(lockScreenMessage);
            replyBuilder.append("locked");
        } else if (msg.startsWith(settings.getFmdCommand() + " stats")) {
            replyBuilder.append("Network-Stats:\nIPs: \n");
            Map<String, String> ips = Network.getAllIP();
            Iterator<String> it = ips.keySet().iterator();
            while(it.hasNext()){
                String intf = it.next();
                replyBuilder.append(intf).append(": ").append(ips.get(intf)).append("\n");
            }
            replyBuilder.append("\nAvailable Wifi-Networks:\n");
            for (ScanResult sr : Network.getWifiNetworks(context)) {
                replyBuilder.append(sr.SSID).append("\n");
            }
        } else if (msg.startsWith(settings.getFmdCommand() + " delete") && Permission.DEVICE_ADMIN) {
            if (settings.isWipeEnabled()) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (msg.length() > settings.getFmdCommand().length() + 8) {
                    String pin = originalMsg.substring(settings.getFmdCommand().length() + 8, msg.length());
                    if (pin.equals(settings.getPin())) {
                        devicePolicyManager.wipeData(0);
                        replyBuilder.append("Goodbye...");
                    } else {
                        replyBuilder.append("False Pin!");
                    }
                } else {
                    replyBuilder.append("Syntax: ").append(settings.getFmdCommand()).append(" delete [pin]");
                }
            }
        } else if (msg.equals(settings.getFmdCommand())) {
            replyBuilder.append("FindMyDevice Commands:\n");
            if (Permission.GPS) {
                replyBuilder.append(settings.getFmdCommand()).append(" where are you - sends the current location\n");
            }
            replyBuilder.append(settings.getFmdCommand()).append(" ring - lets the phone ring\n");
            if (Permission.DEVICE_ADMIN) {
                replyBuilder.append(settings.getFmdCommand()).append(" lock - locks the phone\n");
            }
            replyBuilder.append(settings.getFmdCommand()).append(" stats - sends device informations");
            if (settings.isWipeEnabled()) {
                replyBuilder.append("\n").append(settings.getFmdCommand()).append(" delete - resets the phone");
            }
        }

        String reply = replyBuilder.toString();
        if (!reply.isEmpty()) {
            counter++;
            Notifications.notify(context, "SMS-Receiver", "New Usage " + counter);
            SMS.sendMessage(sender, reply);
        }
    }
}
