package de.nulide.findmydevice.sender;

import android.content.Context;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;

public class Sender implements Serializable {

    private String destination;
    public final String SENDER_TYPE;

    public Sender(String destination, String senderType){
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
