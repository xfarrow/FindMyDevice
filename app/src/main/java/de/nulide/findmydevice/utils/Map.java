package de.nulide.findmydevice.utils;

public class Map {

    public static String createMapLink(String lat, String lon){
        StringBuilder link = new StringBuilder("https://www.openstreetmap.org/?mlat=");
        link.append(lat).append("&mlon=").append(lon).append("#map=14/").append(lat).append("/").append(lon);
        return link.toString();
    }
}
