package de.nulide.findmydevice.utils;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import de.nulide.findmydevice.R;

public class JSONResponseListener implements Response.ErrorListener, Response.Listener<JSONObject> {
    private final String sender;
    private final String url;
    private final Context context;

    public JSONResponseListener(Context context, String sender, String url) {
        this.sender = sender;
        this.url = url;
        this.context = context;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        SMS.sendMessage(sender, context.getString(R.string.JSON_RL_Error) + url);
    }

    @Override
    public void onResponse(JSONObject response) {
        if (response.has("lat") && response.has("lon")) {
            try {
                String lat = response.getString("lat");
                String lon = response.getString("lon");
                SMS.sendMessage(sender, context.getString(R.string.JSON_RL_OpenCellIdLocation) + lat + " " + lon+"\n\n" + Map.createMapLink(lat, lon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
