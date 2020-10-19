package de.nulide.findmydevice.utils;

import android.content.Context;

public class MessageHandler {

    public static void handle(String sender, String msg, Context context){
        msg = msg.toLowerCase();
        String reply = "";
        if(msg.startsWith("fmd where are you")){
            reply = "will be send as soon as possible.";
            GPS gps = new GPS(context, sender);
            gps.sendGSMCellLocation();
        }else if(msg.startsWith("fmd ring")){
            reply = "rings";
        }else if(msg.startsWith("fmd delete")){
            reply = "will be done.\nGoodbye";
        }else if(msg.startsWith("fmd")){
            reply = "FindMyDevice Commands:\n" +
                    "fmd where are you - sends gps data\n" +
                    "fmd ring - lets the phone ring\n" +
                    "fmd delete - resets the phone";
        }
            SMS.sendMessage(sender, reply);
    }
}
