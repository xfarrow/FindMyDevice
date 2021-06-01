package de.nulide.findmydevice.logic;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.Iterator;
import java.util.Map;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.ui.LockScreenMessage;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.logic.command.helper.GPS;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.logic.command.helper.Network;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.logic.command.helper.Ringer;
import de.nulide.findmydevice.utils.SecureSettings;

public class MessageHandler {

    private final String COM_LOCATE = "locate";
    private final String COM_RING = "ring";
    private final String COM_LOCK = "lock";
    private final String COM_DELETE = "delete";
    private final String COM_STATS = "stats";

    ComponentHandler ch;

    public MessageHandler(ComponentHandler ch) {
        this.ch = ch;
    }

    public void handle(Sender sender, String msg, Context context) {
        String originalMsg = msg;
        msg = msg.toLowerCase();
        StringBuilder replyBuilder = new StringBuilder();
        if(msg.startsWith((String) ch.getSettings().get(Settings.SET_FMD_COMMAND))) {
            int cutLength = ((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length();
            if(msg.length() > cutLength){
                cutLength+=1;
            }
            originalMsg = originalMsg.substring(cutLength, msg.length());
            msg = msg.substring(cutLength, msg.length());
            if (msg.startsWith(COM_LOCATE) && Permission.GPS) {
                if (!GPS.isGPSOn(context)) {
                    if (Permission.WRITE_SECURE_SETTINGS) {
                        ch.getSettings().set(Settings.SET_GPS_STATE_BEFORE, 0);
                        SecureSettings.turnGPS(context, true);
                    }else{
                        replyBuilder.append(context.getString(R.string.MH_No_GPS));
                    }
                }
                if(GPS.isGPSOn(context)){
                    replyBuilder.append(context.getString(R.string.MH_GPS_WILL_FOLLOW));
                    GPS gps = new GPS(ch);

                    //if option gps is set do not send gsm cell data
                    if(!msg.contains("gps")) {
                        gps.sendGSMCellLocation(ch.getSettings());
                    }
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
                if ((Boolean) ch.getSettings().get(Settings.SET_WIPE_ENABLED)) {
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    if (msg.length() > COM_DELETE.length()+1) {
                        String pin = originalMsg.substring(COM_DELETE.length()+1, msg.length());
                        if (CypherUtils.checkPasswordHash((String) ch.getSettings().get(Settings.SET_PIN), pin)) {
                            devicePolicyManager.wipeData(0);
                            replyBuilder.append(context.getString(R.string.MH_Delete));
                        } else {
                            replyBuilder.append(context.getString(R.string.MH_False_Pin));
                        }
                    } else {
                        replyBuilder.append(context.getString(R.string.MH_Syntax)).append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(" delete [pin]");
                    }
                }
            } else {
                replyBuilder.append(context.getString(R.string.MH_Title_Help)).append("\n");
                if (Permission.GPS) {
                    replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_where)).append("\n");
                }
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_ring)).append("\n");
                if (Permission.DEVICE_ADMIN) {
                    replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Lock)).append("\n");
                }
                replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Stats));
                if ((Boolean) ch.getSettings().get(Settings.SET_WIPE_ENABLED)) {
                    replyBuilder.append("\n").append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_delete));
                }
            }

            String reply = replyBuilder.toString();
            if (!reply.isEmpty()) {
                Logger.logSession("Command used", msg);
                int counter = (Integer) ch.getSettings().get(Settings.SET_FMDSMS_COUNTER);
                counter++;
                ch.getSettings().set(Settings.SET_FMDSMS_COUNTER, counter);
                sender.sendNow(reply.toString());
                Notifications.notify(context, "SMS-Receiver", "New Usage " + counter, Notifications.CHANNEL_USAGE);
            }
        }
    }

    public boolean checkForPin(String msg) {
        if (msg.length() > ((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length()) {
            String pin = msg.substring(((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length() + 1);
            return CypherUtils.checkPasswordHash((String) ch.getSettings().get(Settings.SET_PIN), pin);
        }
        return false;
    }

}
