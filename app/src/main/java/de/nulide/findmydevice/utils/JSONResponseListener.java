package de.nulide.findmydevice.utils;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONResponseListener implements Response.ErrorListener, Response.Listener<JSONObject>{
    private String sender;
    private String url;

    public JSONResponseListener(String sender, String url){
        this.sender = sender;
        this.url = url;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        SMS.sendMessage(sender, "Error gaining location from OpenCellID.\nPlease try the following link. The parameter you want are lon and lat.\n"+url);
    }

    @Override
    public void onResponse(JSONObject response) {
        if(response.has("lat") && response.has("lon")){
            try {
                SMS.sendMessage(sender, "OpenCellID-Location: " + response.getString("lat") + " " + response.getString("lon"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
