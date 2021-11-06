package de.nulide.findmydevice.receiver;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unifiedpush.android.connector.MessagingReceiver;
import org.unifiedpush.android.connector.MessagingReceiverHandler;
import org.unifiedpush.android.connector.Registration;

import de.nulide.findmydevice.utils.Logger;


public class PushReceiver extends MessagingReceiver {

    public PushReceiver() {
        super(new handler());
    }

}

class handler implements MessagingReceiverHandler {


    @Override
    public void onMessage(@Nullable Context context, @NotNull String s, @NotNull String s1) {
        Logger.log("Message", s);
    }

    @Override
    public void onNewEndpoint(@Nullable Context context, @NotNull String s, @NotNull String s1) {
        Logger.log("Endpoint", s);
    }

    @Override
    public void onRegistrationFailed(@Nullable Context context, @NotNull String s) {

    }

    @Override
    public void onRegistrationRefused(@Nullable Context context, @NotNull String s) {

    }

    @Override
    public void onUnregistered(@Nullable Context context, @NotNull String s) {

    }
}
