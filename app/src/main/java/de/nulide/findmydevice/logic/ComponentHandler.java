package de.nulide.findmydevice.logic;

import android.content.Context;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.sender.Sender;

public class ComponentHandler {

    private Settings settings;
    private Sender sender;
    private Context context;

    private LocationHandler locationHandler;
    private MessageHandler messageHandler;

    public ComponentHandler(de.nulide.findmydevice.data.Settings settings, Context context) {
        this.settings = settings;
        this.context = context;
        messageHandler = new MessageHandler(this);
        locationHandler = new LocationHandler(this);
    }

    public Settings getSettings() {
        return settings;
    }

    public Sender getSender() {
        return sender;
    }

    public Context getContext() {
        return context;
    }

    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}
