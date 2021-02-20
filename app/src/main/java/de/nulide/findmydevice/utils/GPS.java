package de.nulide.findmydevice.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.sender.Sender;

public class GPS implements LocationListener {

    static final int WAIT_TIME = 1000 * 5;
    private final Context context;
    private final LocationManager locationManager;
    private final Sender sender;
    private Location currentBestLocation = null;

    @SuppressLint("MissingPermission")
    public GPS(Context context, Sender sender) {
        this.context = context;
        this.sender = sender;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Looper m = Looper.myLooper();
        for (String providor : locationManager.getAllProviders()) {
            locationManager.requestSingleUpdate(providor, this, m);
        }
    }

    public static void turnOnGPS(Context context) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_MODE, new Integer(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY).toString());
        }else{
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            context.sendBroadcast(intent);
        }
    }

    public static boolean isGPSOn(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }
        for(String provider : lm.getAllProviders()){
            if(lm.isProviderEnabled(provider)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, currentBestLocation)) {
            currentBestLocation = location;
        }
        if (currentBestLocation == null) {
            currentBestLocation = location;
        }
        String provider = getLastBestLocation().getProvider();
        String lat = new Double(getLastBestLocation().getLatitude()).toString();
        String lon = new Double(getLastBestLocation().getLongitude()).toString();
        sender.sendNow(provider + ": " + lat + " " + lon + "\n\n" + Map.createMapLink(lat, lon));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLastBestLocation() {
        @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > WAIT_TIME;
        boolean isSignificantlyOlder = timeDelta < -WAIT_TIME;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        String provider1 = location.getProvider();
        String provider2 = currentBestLocation.getProvider();
        if (provider1 == null) {
            return provider2 == null;
        }
        boolean isFromSameProvider = provider1.equals(provider2);

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    public GsmCellLocation sendGSMCellLocation(de.nulide.findmydevice.data.Settings settings) {
        StringBuilder msg = new StringBuilder(context.getString(R.string.GPS_GSM_Data));
        msg.append("\n");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getNetworkOperator();
        @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
        if (location != null) {
            msg.append("cid: ").append(location.getCid()).append("\nlac: ").append(location.getLac()).append("\n");
        }
        if (!TextUtils.isEmpty(operator)) {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            msg.append("mcc: ").append(mcc).append("\nmnc: ").append(mnc);
            sender.sendNow(msg.toString());
            sendOpenCellIdLocation(settings, sender, mcc, mnc, location.getLac(), location.getCid());
        }
        return location;
    }

    public void sendOpenCellIdLocation(de.nulide.findmydevice.data.Settings settings, Sender sender, int mcc, int mnc, int lac, int cid) {
        if (((String) settings.get(de.nulide.findmydevice.data.Settings.SET_OPENCELLID_API_KEY)).isEmpty()) {
            return;
        }
        StringBuilder urlBuilder = new StringBuilder("https://opencellid.org/cell/get?key=")
                .append((String) settings.get(de.nulide.findmydevice.data.Settings.SET_OPENCELLID_API_KEY)).append("&mcc=").append(mcc).append("&mnc=").append(mnc)
                .append("&lac=").append(lac).append("&cellid=").append(cid).append("&format=json");

        final String url = urlBuilder.toString();
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest ExampleRequest = new JsonObjectRequest(Request.Method.GET, url, null, new JSONResponseListener(context, sender, url), new JSONResponseListener(context, sender, url));
        ExampleRequestQueue.add(ExampleRequest);
    }
}
