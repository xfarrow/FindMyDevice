package de.nulide.findmydevice.services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.logic.LocationHandler;
import de.nulide.findmydevice.logic.MessageHandler;
import de.nulide.findmydevice.sender.FooSender;
import de.nulide.findmydevice.sender.Sender;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.utils.JSONResponseListener;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;

@SuppressLint("NewApi")
public class FMDServerService extends JobService {

    private static final String TAG = "FMDServerService";

    public static void sendNewLocation(Context context, String provider, String lat, String lon, String url, String id) {
        PublicKey publicKey = KeyIO.readKeys().getPublicKey();
        RequestQueue queue = Volley.newRequestQueue(context);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("provider", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey,provider)));
            jsonObject.put("date", Calendar.getInstance().getTimeInMillis());
            jsonObject.put("lon", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, lon)));
            jsonObject.put("lat", CypherUtils.encodeBase64(CypherUtils.encryptWithKey(publicKey, lat)));
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

    public static void registerOnServer(Context context, String url, String key) {
        IO.context = context;
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        RequestQueue queue = Volley.newRequestQueue(context);

        final String requestString = key;

        StringRequest putRequest = new StringRequest(Request.Method.PUT, url+"/newDevice", new IDResponseListener(settings),
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
                headers.put("Content-Type", "application/text");
                headers.put("Accept", "application/text");
                return headers;
            }

            @Override
            public byte[] getBody() {

                try {
                    return requestString.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        queue.add(putRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context, int time) {
        ComponentName serviceComponent = new ComponentName(context, FMDServerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(time * 1000 * 60);
        builder.setOverrideDeadline(time + 5 * 1000 * 60);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Sender sender = new FooSender(this);
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        Boolean passwordSet = (Boolean) settings.get(Settings.SET_FMDSERVER_PASSWORD_SET);
        if(passwordSet) {
            Notifications.init(this);
            Permission.initValues(this);
            MessageHandler.init(settings);
            LocationHandler.init(this, settings, sender);
            MessageHandler.handle(sender, ((String) settings.get(Settings.SET_FMD_COMMAND)) + " locate", this);
            if ((Boolean) settings.get(Settings.SET_FMDSERVER)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scheduleJob(this, (Integer) settings.get(Settings.SET_FMDSERVER_UPDATE_TIME));
                }
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static class IDResponseListener implements Response.Listener<String> {

        private Settings settings;

        public IDResponseListener(Settings settings){
            this.settings = settings;
        }

        @Override
        public void onResponse(String response) {
            settings.set(Settings.SET_FMDSERVER_ID, response);
        }

    }
}
