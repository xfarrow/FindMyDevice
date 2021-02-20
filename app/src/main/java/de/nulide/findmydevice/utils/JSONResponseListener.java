package de.nulide.findmydevice.utils;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.sender.SMS;
import de.nulide.findmydevice.sender.Sender;

public class JSONResponseListener implements Response.ErrorListener, Response.Listener<JSONObject> {
    private final Sender sender;
    private final String url;
    private final Context context;

    public JSONResponseListener(Context context, Sender sender, String url) {
        this.sender = sender;
        this.url = url;
        this.context = context;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        sender.sendNow(context.getString(R.string.JSON_RL_Error) + url);
    }

    @Override
    public void onResponse(JSONObject response) {
        if (response.has("lat") && response.has("lon")) {
            try {
                String lat = response.getString("lat");
                String lon = response.getString("lon");
                sender.sendNow(context.getString(R.string.JSON_RL_OpenCellIdLocation) + lat + " " + lon+"\n\n" + Map.createMapLink(lat, lon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
