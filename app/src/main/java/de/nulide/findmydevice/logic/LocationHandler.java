package de.nulide.findmydevice.logic;

import android.content.Context;

import de.nulide.findmydevice.data.FMDSettings;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.OpenStreetMap;

public class LocationHandler {

    private static FMDSettings FMDSettings;
    private static Sender sender;
    private static Context context;


    public static void init(Context c, FMDSettings set, Sender send){
        FMDSettings = set;
        context = c;
        sender = send;
    }

    public static void newlocation(String provider, String lat, String lon){
        StringBuilder sb = new StringBuilder(provider);
        sb.append(": Lat: ").append(lat).append(" Lon: ").append(lat).append("\n\n").append(OpenStreetMap.createMapLink(lat, lon));
        sender.sendNow(sb.toString());

        if((Boolean) FMDSettings.get(FMDSettings.SET_FMDSERVER)){
            String id =  (String) FMDSettings.get(FMDSettings.SET_FMDSERVER_ID);
            if(!id.isEmpty()) {
                FMDServerService.sendNewLocation(context, provider, lat, lon, (String) FMDSettings.get(FMDSettings.SET_FMDSERVER_URL), (String) FMDSettings.get(FMDSettings.SET_FMDSERVER_ID));
            }
        }
    }
}
