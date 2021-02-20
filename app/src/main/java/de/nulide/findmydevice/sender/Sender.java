package de.nulide.findmydevice.sender;

import android.content.Context;
import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;

public class Sender {

    private Context context;
    private StringBuilder queue;
    private String destination;
    public final String SENDER_TYPE;

    public Sender(Context context, String destination, String senderType){
        this.context = context;
        this.queue = new StringBuilder();
        this.destination = destination;
        this.SENDER_TYPE = senderType;
    }

    public void addToQueue(String msg){
        queue.append(msg).append("\n\n\n");
    }

    public void send(){
        queue.append(context.getString(R.string.Sender_End_Of_Msg));
        sendMessage(destination, queue.toString());
        queue = new StringBuilder();
    }

    public void sendNow(String msg){
        sendMessage(destination, msg);
    }

    protected void sendMessage(String destination, String msg){

    }

    public boolean isQueueEmpty(){
        return queue.toString().length() == 0;
    }

    public String getDestination(){
        return destination;
    }
}
