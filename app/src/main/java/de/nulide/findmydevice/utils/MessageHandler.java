package de.nulide.findmydevice.utils;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

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
