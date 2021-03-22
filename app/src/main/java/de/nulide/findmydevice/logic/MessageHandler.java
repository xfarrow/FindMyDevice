package de.nulide.findmydevice.logic;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.ui.LockScreenMessage;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.utils.BCryptUtils;
import de.nulide.findmydevice.utils.GPS;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Network;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.utils.Ringer;
import de.nulide.findmydevice.sender.SMS;

public class MessageHandler {

    private static String COM_LOCATE = "locate";
    private static String COM_RING = "ring";
    private static String COM_LOCK = "lock";
    private static String COM_DELETE = "delete";
    private static String COM_STATS = "stats";

    private static int counter = 0;
    private static Settings settings;

    public static void init(Settings set) {
        settings = set;
    }

    public static void handle(Sender sender, String msg, Context context) {
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder();
        if(msg.startsWith((String)settings.get(Settings.SET_FMD_COMMAND))) {
            int cutLength = ((String) settings.get(Settings.SET_FMD_COMMAND)).length();
            if(msg.length() > cutLength){
                cutLength+=1;
            }
            originalMsg = originalMsg.substring(cutLength, msg.length());
            msg = msg.substring(cutLength, msg.length());
            if (msg.startsWith(COM_LOCATE) && Permission.GPS) {
                if (!GPS.isGPSOn(context)) {
                    if (Permission.WRITE_SECURE_SETTINGS) {
                        GPS.turnOnGPS(context);
                    }else{
                        replyBuilder.append(context.getString(R.string.MH_No_GPS));
                    }
                }
                if(GPS.isGPSOn(context)){
                    replyBuilder.append(context.getString(R.string.MH_GPS_WILL_FOLLOW));
                    GPS gps = new GPS(context, sender);
                    gps.sendGSMCellLocation(settings);
                }
            } else if (msg.startsWith(COM_RING)) {
                replyBuilder.append(context.getString(R.string.MH_rings));
                if (msg.contains("long")) {
                    Ringer.ring(context, 180);
                } else {
                    Ringer.ring(context, 15);
                }
            } else if (msg.startsWith(COM_LOCK) && Permission.DEVICE_ADMIN) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.lockNow();
                Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
                lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                lockScreenMessage.putExtra(LockScreenMessage.SENDER, sender.getDestination());
                lockScreenMessage.putExtra(LockScreenMessage.SENDER_TYPE, sender.SENDER_TYPE);
                if (msg.length() > COM_LOCK.length() + 1 ) {
                    String customMessage = originalMsg.substring(COM_LOCK.length() + 1, msg.length());
                    lockScreenMessage.putExtra(LockScreenMessage.CUSTOM_TEXT, customMessage);
                }
                context.startActivity(lockScreenMessage);
                replyBuilder.append(context.getString(R.string.MH_Locked));
            } else if (msg.startsWith(COM_STATS)) {
                replyBuilder.append(context.getString(R.string.MH_Stats));
                Map<String, String> ips = Network.getAllIP();
                Iterator<String> it = ips.keySet().iterator();
                while (it.hasNext()) {
                    String intf = it.next();
                    replyBuilder.append(intf).append(": ").append(ips.get(intf)).append("\n");
                }
                replyBuilder.append(context.getString(R.string.MH_Networks));
                for (ScanResult sr : Network.getWifiNetworks(context)) {
                    replyBuilder.append(sr.SSID).append("\n");
                }
            } else if (msg.startsWith(COM_DELETE) && Permission.DEVICE_ADMIN) {
                if ((Boolean) settings.get(Settings.SET_WIPE_ENABLED)) {
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    if (msg.length() > COM_DELETE.length()+1) {
                        String pin = originalMsg.substring(COM_DELETE.length()+1, msg.length());
                        if (BCryptUtils.checkPasswordHash((String) settings.get(Settings.SET_PIN), pin)) {
                            devicePolicyManager.wipeData(0);
                            replyBuilder.append(context.getString(R.string.MH_Delete));
                        } else {
                            replyBuilder.append(context.getString(R.string.MH_False_Pin));
                        }
                    } else {
                        replyBuilder.append(context.getString(R.string.MH_Syntax)).append((String) settings.get(Settings.SET_FMD_COMMAND)).append(" delete [pin]");
                    }
                }
            } else {
                replyBuilder.append(context.getString(R.string.MH_Title_Help)).append("\n");
                if (Permission.GPS) {
                    replyBuilder.append((String) settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_where)).append("\n");
                }
                replyBuilder.append((String) settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_ring)).append("\n");
                if (Permission.DEVICE_ADMIN) {
                    replyBuilder.append((String) settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Lock)).append("\n");
                }
                replyBuilder.append((String) settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Stats));
                if ((Boolean) settings.get(Settings.SET_WIPE_ENABLED)) {
                    replyBuilder.append("\n").append((String) settings.get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_delete));
                }
            }

            String reply = replyBuilder.toString();
            if (!reply.isEmpty()) {
                Logger.logSession("Command used", msg);
                counter++;
                sender.sendNow(reply.toString());
                Notifications.notify(context, "SMS-Receiver", "New Usage " + counter, Notifications.CHANNEL_USAGE);
            }
        }
    }

    public static boolean checkForPin(String msg) {
        if (msg.length() > ((String) settings.get(Settings.SET_FMD_COMMAND)).length()) {
            String pin = msg.substring(((String) settings.get(Settings.SET_FMD_COMMAND)).length() + 1);
            return BCryptUtils.checkPasswordHash((String) settings.get(Settings.SET_PIN), pin);
        }
        return false;
    }

}
