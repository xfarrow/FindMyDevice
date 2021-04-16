package de.nulide.findmydevice.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FMDServer {

    public static void sendNewLocation(Context context, String lat, String lon, String url) {
        RequestQueue queue = Volley.newRequestQueue(context);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", createID());
            jsonObject.put("date", Calendar.getInstance().getTimeInMillis());
            jsonObject.put("lon", lon);
            jsonObject.put("lat", lat);
        } catch (JSONException e) {

        }


        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url+"/newlocation", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("error", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", error.getMessage());
                    }
                }){

            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {

                try {
                    return jsonObject.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        queue.add(putRequest);
    }

    private static String createID(){
        return "test";
    }
}
