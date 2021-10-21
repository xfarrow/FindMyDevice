package de.nulide.findmydevice.services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.BatteryManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.logic.ComponentHandler;
import de.nulide.findmydevice.sender.FooSender;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.PatchedVolley;
import de.nulide.findmydevice.utils.Permission;

@SuppressLint("NewApi")
public class FMDServerService extends JobService {

    private static final int JOB_ID = 108;

    public static void sendNewLocation(Context context, String provider, String lat, String lon, String url, String id, String hashedpw) {
        PublicKey publicKey = KeyIO.readKeys().getPublicKey();
        RequestQueue queue = PatchedVolley.newRequestQueue(context);
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        String batLevel = new Integer(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).toString();

        final JSONObject requestAccessObject = new JSONObject();
        try {
            requestAccessObject.put("DeviceId", id);
            requestAccessObject.put("HashedPassword", hashedpw);
        } catch (JSONException e) {

        }

        final JSONObject locationDataObject = new JSONObject();
        try {
            locationDataObject.put("provider", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, provider)));
            locationDataObject.put("date", Calendar.getInstance().getTimeInMillis());
            locationDataObject.put("bat", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, batLevel)));
            locationDataObject.put("lon", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, lon)));
            locationDataObject.put("lat", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, lat)));
        } catch (JSONException e) {

        }

        JsonObjectRequest accessRequest = new JsonObjectRequest(Request.Method.PUT, url + "/requestAccess", requestAccessObject, new AccesssTokenListener(context, locationDataObject, url),
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

    public static void registerOnServer(Context context, String url, String privKey, String pubKey, String hashedPW) {
        IO.context = context;
        Settings Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        RequestQueue queue = PatchedVolley.newRequestQueue(context);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("hashedPassword", hashedPW);
            jsonObject.put("pubkey", pubKey);
            jsonObject.put("privkey", privKey);
        }catch (JSONException e){

        }

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url+"/device", jsonObject, new IDResponseListener(Settings),
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
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

    public static void scheduleJob(Context context, int time) {
        ComponentName serviceComponent = new ComponentName(context, FMDServerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(time * 1000 * 60);
        builder.setOverrideDeadline(time * 1000 * 60 * 2);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
        Logger.logSession("FMDServerService", "scheduled new job");

    }

    public static void cancleAll(Context context) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancelAll();
    }


    @Override
    public boolean onStartJob(JobParameters params) {

        Sender sender = new FooSender();
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        Logger.logSession("FMDServerService", "started");
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        if ((Boolean) settings.get(settings.SET_FMDSERVER_UPLOAD_SERVICE)) {
            ComponentHandler ch = new ComponentHandler(settings, this, this, params);
            ch.setSender(sender);
            Boolean registered = !((String) ch.getSettings().get(Settings.SET_FMDSERVER_ID)).isEmpty();
            if (registered) {
                Notifications.init(this, true);
                Permission.initValues(this);
                ch.getMessageHandler().setSilent(true);
                String locateCommand = " locate";
                switch ((Integer) ch.getSettings().get(Settings.SET_FMDSERVER_LOCATION_TYPE)) {
                    case 0:
                        locateCommand += " gps";
                        break;
                    case 1:
                        locateCommand += " cell";
                        break;
                }
                ch.getMessageHandler().handle(((String) ch.getSettings().get(Settings.SET_FMD_COMMAND)) + locateCommand, this);
            }
            Logger.logSession("FMDServerService", "finished job, waiting for location");
            Logger.writeLog();
            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logger.logSession("FMDServerService", "job stopped by system");
        return false;
    }

    public static class IDResponseListener implements Response.Listener<JSONObject> {

        private Settings Settings;

        public IDResponseListener(Settings Settings){
            this.Settings = Settings;
        }

        @Override
        public void onResponse(JSONObject response) {
            try {
                Settings.set(Settings.SET_FMDSERVER_ID, response.get("DeviceId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static class AccesssTokenListener implements Response.Listener<JSONObject> {

        private final Context context;
        private final JSONObject locationDataObject;
        private final String url;

        public AccesssTokenListener(Context context, JSONObject locationDataObject, String url) {
            this.context = context;
            this.locationDataObject = locationDataObject;
            this.url = url;
        }

        @Override
        public void onResponse(JSONObject response) {
            if (response.has("AccessToken")) {
                try {
                    locationDataObject.put("AccessToken", response.get("AccessToken"));
                    RequestQueue queue = PatchedVolley.newRequestQueue(context);
                    JsonObjectRequest locationPutRequest = new JsonObjectRequest(Request.Method.POST, url + "/location", locationDataObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            }) {

                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Accept", "application/json");
                            return headers;
                        }

                        @Override
                        public byte[] getBody() {
                            return locationDataObject.toString().getBytes(StandardCharsets.UTF_8);
                        }
                    };
                    queue.add(locationPutRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
