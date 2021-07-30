package de.nulide.findmydevice.sender;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

public class NotificationReply extends Sender{

    public final static String TYPE = "NOTIFICATION";
    private StatusBarNotification sbn;

    public NotificationReply(String destination, String senderType, StatusBarNotification sbn) {
        super(destination, senderType);
        this.sbn = sbn;
    }

    @Override
    protected void sendMessage(String destination, String msg) {
        Notification.Action actions[] = sbn.getNotification().actions;

        for (Notification.Action act : actions) {
            if (act != null && act.getRemoteInputs() != null) {
                if (act.title.toString().contains("Telegram")) {
                    if (act.getRemoteInputs() != null)
                        sendNotification();
                }
            }
        }
    }

    private void sendNotification(){

    }


    }
