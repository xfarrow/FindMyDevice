package de.nulide.findmydevice.logic;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.services.CameraService;
import de.nulide.findmydevice.ui.DummyCameraActivity;
import de.nulide.findmydevice.ui.LockScreenMessage;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.ui.RingerActivity;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.logic.command.helper.GPS;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.logic.command.helper.Network;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.logic.command.helper.Ringer;
import de.nulide.findmydevice.utils.SecureSettings;

public class MessageHandler {

    public static final String COM_LOCATE = "locate";
    public static final String COM_RING = "ring";
    public static final String COM_LOCK = "lock";
    public static final String COM_DELETE = "delete";
    public static final String COM_STATS = "stats";

    public static final String COM_EXPERT = "expert";
    public static final String COM_EXPERT_GPS = "gps";
    public static final String COM_EXPERT_SOUND = "sound";
    public static final String COM_EXPERT_CAMERA = "camera";


    private ComponentHandler ch;

    private boolean silent = false;

    public MessageHandler(ComponentHandler ch) {
        this.ch = ch;
    }

    public String handle(String msg, Context context) {
        String executedCommand;
        StringBuilder replyBuilder = new StringBuilder();
        String originalMsg = msg;
        msg = msg.toLowerCase();

        // ^fmd\s(locate(\s(last|gps|cell))?|ring(\slock)?|lock|stats|delete)(\s\w*)?
        String regexToMatch = "^" + (String) ch.getSettings().get(Settings.SET_FMD_COMMAND) +
                "\\s(locate(\\s(last|gps|cell))?|" + COM_RING + "(\\slock)?|" + COM_LOCK + "|" + COM_STATS + "|" + COM_DELETE + ")(\\s\\w*)?";

        Pattern pattern = Pattern.compile(regexToMatch);
        Matcher matcher = pattern.matcher(msg);
        if (!matcher.find()) {
            return "";
        }

        String providedSubCommand = null;
        String providedPin = null;
        String[] splitMessage = msg.split(" ");
        /*
         * An SMS can be either
         * A) FMD_COMMAND + COMMAND (eg. fmd locate)
         * B) FMD COMMAND + COMMAND + SUBCOMMAND (eg fmd locate gps)
         * C) FMD COMMAND + COMMAND + PIN (eg fmd locate MyPin1234)
         * D) FMD COMMAND + COMMAND + SUBCOMMAND + PIN (eg fmd locate gps MyPin1234)
         */
        executedCommand = splitMessage[1];
        if(splitMessage.length == 3){
            providedSubCommand = splitMessage[2];
            providedPin = splitMessage[2];
        }
        else if(splitMessage.length == 4){
            providedSubCommand = splitMessage[2];
            providedPin = splitMessage[3];
        }

        // Check PIN if the devices accept only PIN-based commands
        if ((Boolean) ch.getSettings().get(Settings.SET_PIN_ONLY)) {
            if (providedPin == null || !CypherUtils.checkPasswordHash((String) ch.getSettings().get(Settings.SET_PIN), providedPin)) {
                return executedCommand;
            }
        }

        //LOCATE
        if (executedCommand.equals(COM_LOCATE) && Permission.GPS) {
            if (providedSubCommand != null && providedSubCommand.equals("last")) {
                if (!((String) ch.getSettings().get(Settings.SET_LAST_KNOWN_LOCATION_LAT)).isEmpty()) {
                    ch.getLocationHandler().sendLastKnownLocation();
                } else {
                    ch.getSender().sendNow(ch.getContext().getString(R.string.MH_LAST_KNOWN_LOCATION_NOT_AVAILABLE));
                }
            }
            if (!GPS.isGPSOn(context)) {
                if (Permission.WRITE_SECURE_SETTINGS) {
                    ch.getSettings().set(Settings.SET_GPS_STATE, 2);
                    SecureSettings.turnGPS(context, true);
                } else {
                    replyBuilder.append(context.getString(R.string.MH_No_GPS));
                }
            } else {
                if ((Integer) ch.getSettings().get(Settings.SET_GPS_STATE) != 2) {
                    ch.getSettings().set(Settings.SET_GPS_STATE, 1);
                }
            }
            if (GPS.isGPSOn(context)) {
                replyBuilder.append(context.getString(R.string.MH_GPS_WILL_FOLLOW));
                GPS gps = new GPS(ch);

                //if options cell is set do not send gps data
                if (providedSubCommand == null ||  !providedSubCommand.contains("cell")) {
                    gps.sendGPSLocation();
                }

                //if option gps is set do not send gsm cell data
                if (providedSubCommand == null || !providedSubCommand.contains("gps")) {
                    gps.sendGSMCellLocation();
                }
            }
        }

        //RING
        else if (executedCommand.equals(COM_RING)) {
            replyBuilder.append(context.getString(R.string.MH_rings));
            if (providedSubCommand != null  && providedSubCommand.equals("long")) {
                Ringer.ring(context, 180);
            } else {
                // NOT IN THE DOCUMENTATION!!!! TODO: ADD IT
                if (providedSubCommand != null) {
                    String time = providedSubCommand;
                    if (time.matches("[0-9]+")) {
                        Ringer.ring(context, Integer.parseInt(time));
                    }
                } else {
                    Ringer.ring(context, 30);
                }
            }
        }

        //LOCK
        else if (executedCommand.equals(COM_LOCK) && Permission.DEVICE_ADMIN) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.lockNow();
            Intent lockScreenMessage = new Intent(context, LockScreenMessage.class);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockScreenMessage.putExtra(LockScreenMessage.SENDER, ch.getSender().getDestination());
            lockScreenMessage.putExtra(LockScreenMessage.SENDER_TYPE, ch.getSender().SENDER_TYPE);
            if (providedSubCommand != null) {
                String customMessage = providedSubCommand;
                lockScreenMessage.putExtra(LockScreenMessage.CUSTOM_TEXT, customMessage);
            }
            context.startActivity(lockScreenMessage);
            replyBuilder.append(context.getString(R.string.MH_Locked));
        }

        //STATS
        else if (executedCommand.equals(COM_STATS)) {
            replyBuilder.append(context.getString(R.string.MH_Stats));
            Map<String, String> ips = Network.getAllIP();
            Iterator<String> it = ips.keySet().iterator();
            while (it.hasNext()) {
                String intf = it.next();
                replyBuilder.append(intf).append(": ").append(ips.get(intf)).append("\n");
            }
            replyBuilder.append("\n" + context.getString(R.string.MH_Networks) + "\n");
            for (ScanResult sr : Network.getWifiNetworks(context)) {
                replyBuilder.append("SSID: ");
                replyBuilder.append(sr.SSID).append("\nBSSID: ");
                replyBuilder.append(sr.BSSID).append("\n\n");
            }
        }

        // DELETE
        else if (executedCommand.equals(COM_DELETE) && Permission.DEVICE_ADMIN) {
            if ((Boolean) ch.getSettings().get(Settings.SET_WIPE_ENABLED)) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (providedPin != null) {
                    if (CypherUtils.checkPasswordHash((String) ch.getSettings().get(Settings.SET_PIN), providedPin)) {
                        devicePolicyManager.wipeData(0);
                        replyBuilder.append(context.getString(R.string.MH_Delete));
                    } else {
                        replyBuilder.append(context.getString(R.string.MH_False_Pin));
                    }
                } else {
                    replyBuilder.append(context.getString(R.string.MH_Syntax)).append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(" delete [pin]");
                }
            }
        }

        /*
        WHAT IS EXPERT MODE? NO DOCUMENTATION!!!
        TODO: Understand what this does.
         */

        //EXPERT
        else if (msg.startsWith(COM_EXPERT)) {
            executedCommand = COM_DELETE;
            replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_GPS)).append("\n");
            replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_Sound)).append("\n");
            replyBuilder.append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_Expert_Camera)).append("\n");

            //GPS
        } else if (msg.startsWith(COM_EXPERT_GPS)) {

            if (Permission.WRITE_SECURE_SETTINGS) {
                if (msg.contains("on")) {
                    SecureSettings.turnGPS(context, true);
                    ch.getSettings().set(Settings.SET_GPS_STATE, 1);
                } else if (msg.contains("off")) {
                    SecureSettings.turnGPS(context, false);
                    ch.getSettings().set(Settings.SET_GPS_STATE, 0);
                }
            } else {
                replyBuilder.append(context.getString(R.string.MH_NO_SECURE_SETTINGS));
            }

        } else if (msg.startsWith(COM_EXPERT_SOUND)) {
            if (Permission.DND) {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (msg.contains("on")) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                } else if (msg.contains("off")) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                }
            }
        } else if (msg.startsWith(COM_EXPERT_CAMERA)) {
            if (Permission.CAMERA) {
                Intent dummyCameraActivity = new Intent(context, DummyCameraActivity.class);
                dummyCameraActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (msg.contains("front")) {
                    dummyCameraActivity.putExtra(DummyCameraActivity.CAMERA, 1);
                } else {
                    dummyCameraActivity.putExtra(DummyCameraActivity.CAMERA, 0);
                }
                context.startActivity(dummyCameraActivity);
                replyBuilder.append(context.getString(R.string.MH_CAM_CAPTURE) + (String) ch.getSettings().get(Settings.SET_FMDSERVER_URL));
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
            replyBuilder.append("\n").append((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).append(context.getString(R.string.MH_Help_expert));

        }

        String reply = replyBuilder.toString();
        if (!reply.isEmpty() && !silent) {
            Logger.logSession("Command used", msg);
            int counter = (Integer) ch.getSettings().get(Settings.SET_FMDSMS_COUNTER);
            counter++;
            ch.getSettings().set(Settings.SET_FMDSMS_COUNTER, counter);
            ch.getSender().sendNow(reply.toString());
            Notifications.notify(context, "SMS-Receiver", "New Usage " + counter, Notifications.CHANNEL_USAGE);
        }

        return executedCommand;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    // TODO: change
    public boolean checkForPin(String msg) {
        if (msg.length() > ((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length() + 1) {
            String pin = msg.substring(((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)).length() + 1);
            return CypherUtils.checkPasswordHash((String) ch.getSettings().get(Settings.SET_PIN), pin);
        }
        return false;
    }

    // TODO: security vulnerability
    public String checkAndRemovePin(String msg) {
        String[] splited = msg.split(" ");
        String pin = (String) ch.getSettings().get(Settings.SET_PIN);
        boolean isPinValid = false;
        String newMsg = splited[0];
        for (int i = 1; i < splited.length; i++) {
            if (CypherUtils.checkPasswordHash(pin, splited[i])) {
                isPinValid = true;
            } else {
                newMsg += " " + splited[i];
            }
        }
        if (isPinValid) {
            return newMsg;
        }
        return null;
    }
}
