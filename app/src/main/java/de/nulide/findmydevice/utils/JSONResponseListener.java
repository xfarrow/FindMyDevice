package de.nulide.findmydevice.utils;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONResponseListener implements Response.ErrorListener, Response.Listener<JSONObject> {
    private final String sender;
    private final String url;

    public JSONResponseListener(String sender, String url) {
        this.sender = sender;
        this.url = url;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        SMS.sendMessage(sender, "Error gaining location from OpenCellID.\nPlease try the following link. The parameter you want are lon and lat.\n" + url);
    }

    @Override
    public void onResponse(JSONObject response) {
        if (response.has("lat") && response.has("lon")) {
            try {
                String lat = response.getString("lat");
                String lon = response.getString("lon");
                SMS.sendMessage(sender, "OpenCellID-Location: " + lat + " " + lon+"\n\n" + Map.createMapLink(lat, lon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
