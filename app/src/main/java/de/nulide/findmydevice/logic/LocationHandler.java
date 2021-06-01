package de.nulide.findmydevice.logic;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.services.FMDServerService;

public class LocationHandler {

    ComponentHandler ch;


    public LocationHandler(ComponentHandler ch){
        this.ch = ch;
    }

    public void newlocation(String provider, String lat, String lon){
        StringBuilder sb = new StringBuilder(provider);
        sb.append(": Lat: ").append(lat).append(" Lon: ").append(lat).append("\n\n").append(createMapLink(lat, lon));
        ch.getSender().sendNow(sb.toString());

        if((Boolean) ch.getSettings().get(Settings.SET_FMDSERVER)){
            String id =  (String) ch.getSettings().get(Settings.SET_FMDSERVER_ID);
            if(!id.isEmpty()) {
                FMDServerService.sendNewLocation(ch.getContext(), provider, lat, lon, (String) ch.getSettings().get(Settings.SET_FMDSERVER_URL), (String) ch.getSettings().get(Settings.SET_FMDSERVER_ID));
            }
        }
    }

    private String createMapLink(String lat, String lon){
        StringBuilder link = new StringBuilder("https://www.openstreetmap.org/?mlat=");
        link.append(lat).append("&mlon=").append(lon).append("#map=14/").append(lat).append("/").append(lon);
        return link.toString();
    }
}
