package de.nulide.findmydevice.logic;

import java.util.Calendar;
import java.util.Date;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.services.FMDServerService;

public class LocationHandler {

    private ComponentHandler ch;


    public LocationHandler(ComponentHandler ch){
        this.ch = ch;
    }

    public void newLocation(String provider, String lat, String lon){
        StringBuilder sb = new StringBuilder(provider);
        sb.append(": Lat: ").append(lat).append(" Lon: ").append(lon).append("\n\n").append(createMapLink(lat, lon));
        ch.getSender().sendNow(sb.toString());

        ch.getSettings().set(Settings.SET_LAST_KNOWN_LOCATION_LAT, lat);
        ch.getSettings().set(Settings.SET_LAST_KNOWN_LOCATION_LON, lon);
        ch.getSettings().set(Settings.SET_LAST_KNOWN_LOCATION_TIME, Calendar.getInstance().getTimeInMillis());

        if((Boolean) ch.getSettings().get(Settings.SET_FMDSERVER)){
            String id =  (String) ch.getSettings().get(Settings.SET_FMDSERVER_ID);
            if(!id.isEmpty()) {
                FMDServerService.sendNewLocation(ch.getContext(), provider, lat, lon, (String) ch.getSettings().get(Settings.SET_FMDSERVER_URL), (String) ch.getSettings().get(Settings.SET_FMDSERVER_ID), KeyIO.readHashedPW());
            }
        }
    }

    public void sendLastKnownLocation(){
        String lat = (String) ch.getSettings().get(Settings.SET_LAST_KNOWN_LOCATION_LAT);
        String lon = (String) ch.getSettings().get(Settings.SET_LAST_KNOWN_LOCATION_LON);
        Long time = (Long) ch.getSettings().get(Settings.SET_LAST_KNOWN_LOCATION_TIME);
        Calendar.getInstance().getTime();
        Date date = new Date(time);
        StringBuilder sb = new StringBuilder(ch.getContext().getString(R.string.MH_LAST_KNOWN_LOCATION));
        sb.append(": Lat: ").append(lat).append(" Lon: ").append(lon).append("\n\n").append("Time: ").append(date.toString()).append("\n\n").append(createMapLink(lat.toString(), lon.toString()));
        ch.getSender().sendNow(sb.toString());
    }

    private String createMapLink(String lat, String lon){
        StringBuilder link = new StringBuilder("https://www.openstreetmap.org/?mlat=");
        link.append(lat).append("&mlon=").append(lon).append("#map=14/").append(lat).append("/").append(lon);
        return link.toString();
    }
}
