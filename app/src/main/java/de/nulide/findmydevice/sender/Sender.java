package de.nulide.findmydevice.sender;

import android.content.Context;
import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;

public class Sender {

    private Context context;
    private String destination;
    public final String SENDER_TYPE;

    public Sender(Context context, String destination, String senderType){
        this.context = context;
        this.destination = destination;
        this.SENDER_TYPE = senderType;
    }

    public void sendNow(String msg){
        sendMessage(destination, msg);
    }

    protected void sendMessage(String destination, String msg){

    }

    public String getDestination(){
        return destination;
    }
}
