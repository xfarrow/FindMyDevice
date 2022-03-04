package de.nulide.findmydevice.logic.command.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.logic.ComponentHandler;
import de.nulide.findmydevice.services.GPSTimeOutService;
import de.nulide.findmydevice.utils.PatchedVolley;
import de.nulide.findmydevice.utils.SecureSettings;

public class GPS implements LocationListener {

    private ComponentHandler ch;
    private LocationManager locationManager;

    public GPS(ComponentHandler ch) {
        this.ch = ch;
        locationManager = (LocationManager) ch.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public static boolean isGPSOn(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            String provider = location.getProvider();
            String lat = new Double(location.getLatitude()).toString();
            String lon = new Double(location.getLongitude()).toString();
            ch.getLocationHandler().newLocation(provider, lat, lon);
            locationManager.removeUpdates(this);
            if ((Integer) ch.getSettings().get(Settings.SET_GPS_STATE) == 2) {
                SecureSettings.turnGPS(ch.getContext(), false);
                ch.getSettings().set(Settings.SET_GPS_STATE, 0);
            }
            ch.finishJob();
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
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    public void sendGPSLocation(){
        for (String provider : locationManager.getAllProviders()) {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
        GPSTimeOutService.scheduleJob(ch.getContext());
    }

    public GsmCellLocation sendGSMCellLocation() {
        StringBuilder msg = new StringBuilder(ch.getContext().getString(R.string.GPS_GSM_Data));
        msg.append("\n");
        TelephonyManager tm = (TelephonyManager) ch.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getNetworkOperator();
        @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
        if (location != null) {
            msg.append("cid: ").append(location.getCid()).append("\nlac: ").append(location.getLac()).append("\n");
            if (!TextUtils.isEmpty(operator)) {
                int mcc = Integer.parseInt(operator.substring(0, 3));
                int mnc = Integer.parseInt(operator.substring(3));
                msg.append("mcc: ").append(mcc).append("\nmnc: ").append(mnc);
                ch.getSender().sendNow(msg.toString());
                if (!((String) ch.getSettings().get(Settings.SET_OPENCELLID_API_KEY)).isEmpty()) {
                    StringBuilder urlBuilder = new StringBuilder("https://opencellid.org/cell/get?key=")
                            .append((String) ch.getSettings().get(Settings.SET_OPENCELLID_API_KEY)).append("&mcc=").append(mcc).append("&mnc=").append(mnc)
                            .append("&lac=").append(location.getLac()).append("&cellid=").append(location.getCid()).append("&format=json");

                    final String url = urlBuilder.toString();
                    RequestQueue requestQueue = PatchedVolley.newRequestQueue(ch.getContext());
                    JsonObjectRequest ExampleRequest = new JsonObjectRequest(Request.Method.GET, url, null, new LocationResponseListener(ch, url), new LocationResponseListener(ch, url));
                    requestQueue.add(ExampleRequest);
                }
            }
        }
        return location;
    }

    public class LocationResponseListener implements Response.ErrorListener, Response.Listener<JSONObject> {

        private ComponentHandler ch;
        private final String url;

        public LocationResponseListener(ComponentHandler ch, String url) {
            this.ch = ch;
            this.url = url;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            ch.getSender().sendNow(ch.getContext().getString(R.string.JSON_RL_Error) + url);
        }

        @Override
        public void onResponse(JSONObject response) {
            if (response.has("lat") && response.has("lon")) {
                try {
                    String lat = response.getString("lat");
                    String lon = response.getString("lon");
                    ch.getLocationHandler().newLocation(ch.getContext().getString(R.string.JSON_RL_OpenCellIdLocation), lat, lon);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
