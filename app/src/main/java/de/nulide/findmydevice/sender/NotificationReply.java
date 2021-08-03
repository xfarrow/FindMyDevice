package de.nulide.findmydevice.sender;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.robj.notificationhelperlibrary.utils.NotificationUtils;

import models.Action;

public class NotificationReply extends Sender {

    public final static String TYPE = "NOTIFICATION";
    private StatusBarNotification sbn;
    private final Context context;

    public NotificationReply(Context context, StatusBarNotification sbn) {
        super("notification", TYPE);
        this.sbn = sbn;
        this.context = context;
    }

    @Override
    protected void sendMessage(String destination, String msg) {
        Action action = NotificationUtils.getQuickReplyAction(sbn.getNotification(), context.getPackageName());
        if (action != null) {
            try {
                action.sendReply(context, msg);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canSend() {
        return NotificationUtils.getQuickReplyAction(sbn.getNotification(), context.getPackageName()) != null;
    }

}