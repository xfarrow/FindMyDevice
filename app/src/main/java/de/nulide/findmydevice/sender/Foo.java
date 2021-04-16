package de.nulide.findmydevice.sender;

import android.content.Context;
import android.telephony.SmsManager;

import java.util.ArrayList;

import de.nulide.findmydevice.utils.Logger;

public class Foo extends Sender {

    public final static String TYPE = "FOO";

    public Foo(Context context) {
        super(context, "", TYPE);
    }

    @Override
    protected void sendMessage(String destination, String msg) {
    }
}
