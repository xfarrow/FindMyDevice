package de.nulide.findmydevice.logic;

import android.content.Context;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.FMDServer;
import de.nulide.findmydevice.utils.OpenStreetMap;

public class LocationHandler {

    private static Settings settings;
    private static Sender sender;
    private static Context context;


    public static void init(Context c, Settings set, Sender send){
        settings = set;
        context = c;
        sender = send;
    }

    public static void newlocation(String provider, String lat, String lon){
        StringBuilder sb = new StringBuilder(provider);
        sb.append(": Lat: ").append(lat).append(" Lon: ").append(lat).append("\n\n").append(OpenStreetMap.createMapLink(lat, lon));
        sender.sendNow(sb.toString());

        if((Boolean)settings.get(Settings.SET_FMDSERVER)){
            FMDServer.sendNewLocation(context, lat, lon, (String)settings.get(Settings.SET_FMDSERVER_URL));
        }
    }
}
