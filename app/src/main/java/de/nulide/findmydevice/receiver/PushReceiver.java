package de.nulide.findmydevice.receiver;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.unifiedpush.android.connector.MessagingReceiver;
import org.unifiedpush.android.connector.MessagingReceiverHandler;
import org.unifiedpush.android.connector.Registration;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.services.FMDServerCommandService;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.PatchedVolley;


public class PushReceiver extends MessagingReceiver {

    public PushReceiver() {
        super(new handler());
    }

}

class handler implements MessagingReceiverHandler {


    @Override
    public void onMessage(@Nullable Context context, @NotNull String s, @NotNull String s1) {
        FMDServerCommandService.scheduleJobNow(
                context);
    }

    @Override
    public void onNewEndpoint(@Nullable Context context, @NotNull String s, @NotNull String s1) {
        IO.context = context;
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        String url = (String)settings.get(Settings.SET_FMDSERVER_URL);
        RequestQueue queue = PatchedVolley.newRequestQueue(context);


        final JSONObject requestAccessObject = new JSONObject();
        try {
            requestAccessObject.put("IDT", (String)settings.get(Settings.SET_FMDSERVER_ID));
            requestAccessObject.put("Data", KeyIO.readHashedPW());
        } catch (JSONException e) {

        }

        JsonObjectRequest accessRequest = new JsonObjectRequest(Request.Method.PUT, url + "/requestAccess", requestAccessObject, new AccesssTokenListener(context, settings, url, s),
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {

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
                return requestAccessObject.toString().getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(accessRequest);
    }

    @Override
    public void onRegistrationFailed(@Nullable Context context, @NotNull String s) {

    }

    @Override
    public void onRegistrationRefused(@Nullable Context context, @NotNull String s) {

    }

    @Override
    public void onUnregistered(@Nullable Context context, @NotNull String s) {

    }

    private class AccesssTokenListener implements Response.Listener<JSONObject> {

        private Context context;
        private Settings settings;
        private String url;
        private String endpoint;

        public AccesssTokenListener(Context context, Settings settings, String url, String endpoint) {
            this.context = context;
            this.settings = settings;
            this.url = url;
            this.endpoint = endpoint;
        }

        @Override
        public void onResponse(JSONObject response) {
            RequestQueue queue = PatchedVolley.newRequestQueue(context);


            final JSONObject dataPackage = new JSONObject();
            try {
                dataPackage.put("IDT", response.get("Data"));
                dataPackage.put("Data", endpoint);
            } catch (JSONException e) {

            }

            JsonObjectRequest accessRequest = new JsonObjectRequest(Request.Method.PUT, url + "/push", dataPackage, null, null);
            queue.add(accessRequest);
        }
    }
}
