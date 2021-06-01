package de.nulide.findmydevice.utils;

import android.content.Context;
import android.provider.Settings;

public class SecureSettings {

    public static void turnGPS(Context context, boolean enable) {
        if(enable) {
            Settings.Secure.putString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_MODE, new Integer(android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY).toString());
        }else{
            Settings.Secure.putString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_MODE, new Integer(android.provider.Settings.Secure.LOCATION_MODE_OFF).toString());
        }
    }

}
