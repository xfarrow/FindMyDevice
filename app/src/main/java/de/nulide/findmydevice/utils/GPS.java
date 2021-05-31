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

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.FMDSettings;
import de.nulide.findmydevice.logic.LocationHandler;
import de.nulide.findmydevice.sender.Sender;

public class GPS implements LocationListener {

    private final Context context;
    private final Sender sender;
    private final LocationManager locationManager;
    private FMDSettings settings;
    private int providerSize = 0;
    private int providerIndex = 0;

    @SuppressLint("MissingPermission")
    public GPS(Context context, Sender sender, FMDSettings settings) {
        this.context = context;
        this.sender = sender;
        this.settings = settings;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        for (String provider : locationManager.getAllProviders()) {
            providerSize++;
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
    }

    public static void turnGPS(Context context, boolean enable) {
            if(enable) {
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_MODE, new Integer(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY).toString());
            }else{
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_MODE, new Integer(Settings.Secure.LOCATION_MODE_OFF).toString());
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
        if(location != null) {
            String provider = location.getProvider();
            String lat = new Double(location.getLatitude()).toString();
            String lon = new Double(location.getLongitude()).toString();
            providerIndex++;
            LocationHandler.newlocation(provider, lat, lon);
            if (providerSize <= providerIndex && ((Integer) settings.get(FMDSettings.SET_GPS_STATE_BEFORE) == 0)) {
                turnGPS(context, false);
                settings.set(FMDSettings.SET_GPS_STATE_BEFORE, 1);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        locationManager.removeUpdates(this);
    }

    public GsmCellLocation sendGSMCellLocation(FMDSettings FMDSettings) {
        StringBuilder msg = new StringBuilder(context.getString(R.string.GPS_GSM_Data));
        msg.append("\n");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getNetworkOperator();
        @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
        if (location != null) {
            msg.append("cid: ").append(location.getCid()).append("\nlac: ").append(location.getLac()).append("\n");
            if (!TextUtils.isEmpty(operator)) {
                int mcc = Integer.parseInt(operator.substring(0, 3));
                int mnc = Integer.parseInt(operator.substring(3));
                msg.append("mcc: ").append(mcc).append("\nmnc: ").append(mnc);
                sender.sendNow(msg.toString());
                sendOpenCellIdLocation(FMDSettings, sender, mcc, mnc, location.getLac(), location.getCid());
            }
        }
        return location;
    }

    public void sendOpenCellIdLocation(FMDSettings FMDSettings, Sender sender, int mcc, int mnc, int lac, int cid) {
        if (((String) FMDSettings.get(FMDSettings.SET_OPENCELLID_API_KEY)).isEmpty()) {
            return;
        }
        StringBuilder urlBuilder = new StringBuilder("https://opencellid.org/cell/get?key=")
                .append((String) FMDSettings.get(FMDSettings.SET_OPENCELLID_API_KEY)).append("&mcc=").append(mcc).append("&mnc=").append(mnc)
                .append("&lac=").append(lac).append("&cellid=").append(cid).append("&format=json");

        final String url = urlBuilder.toString();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest ExampleRequest = new JsonObjectRequest(Request.Method.GET, url, null, new JSONResponseListener(context, sender, url), new JSONResponseListener(context, sender, url));
        requestQueue.add(ExampleRequest);
    }
}
