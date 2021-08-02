package de.nulide.findmydevice.sender;

import android.content.Context;
import android.telephony.SmsManager;

import java.util.ArrayList;

import de.nulide.findmydevice.utils.Logger;

public class FooSender extends Sender {

    public final static String TYPE = "FOO";

    public FooSender() {
        super("", TYPE);
    }

    @Override
    protected void sendMessage(String destination, String msg) {
    }
}
