package de.nulide.findmydevice.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.locks.Lock;

import de.nulide.findmydevice.LockScreenMessage;
import de.nulide.findmydevice.R;

public class MessageHandler {

    public static void handle(String sender, String msg, Context context) {
        msg = msg.toLowerCase();
        String reply = "";
        if (msg.startsWith("fmd where are you")) {
            reply = "will be send as soon as possible.";
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation();
        } else if (msg.startsWith("fmd ring")) {
            reply = "rings";
            if(msg.contains("long")){
                Ringer.ring(context, 180);
            }else{
                Ringer.ring(context, 15);
            }
        } else if (msg.startsWith("fmd lock")){
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = lockScreenMessage.getExtras();
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender);
            context.startActivity(lockScreenMessage);
            reply = "locked";
        } else if (msg.startsWith("fmd delete")) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            //devicePolicyManager.wipeData(0);
            reply = "Goodbye...";
        } else if (msg.startsWith("fmd")) {
            reply = "FindMyDevice Commands:\n" +
                    "fmd where are you - sends gps data\n" +
                    "fmd ring - lets the phone ring\n" +
                    "fmd lock - locks the phone\n" +
                    "fmd delete - resets the phone";
        }
        if (!reply.isEmpty()) {
            SMS.sendMessage(sender, reply);
        }
    }
}
