package de.nulide.findmydevice.services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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

public class FMDServerCommandService extends JobService {

    private static final int JOB_ID = 109;

    @SuppressLint("NewApi")
    @Override
    public boolean onStartJob(JobParameters params) {
            IO.context = this;
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        String url = (String)settings.get(Settings.SET_FMDSERVER_URL);
        RequestQueue queue = PatchedVolley.newRequestQueue(this);


        final JSONObject requestAccessObject = new JSONObject();
        try {
            requestAccessObject.put("DeviceId", (String)settings.get(Settings.SET_FMDSERVER_ID));
            requestAccessObject.put("HashedPassword", KeyIO.readHashedPW());
        } catch (JSONException e) {

        }

        final JSONObject locationDataObject = new JSONObject();
        try {
            locationDataObject.put("id", -1);
        } catch (JSONException e) {

        }

        JsonObjectRequest accessRequest = new JsonObjectRequest(Request.Method.PUT, url + "/requestAccess", requestAccessObject, new AccesssTokenAndCommandListener(this, this, settings, requestAccessObject, url, params, ""),
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


        scheduleJob(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @SuppressLint("NewApi")
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, FMDServerCommandService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(15 * 1000 * 60);
        builder.setOverrideDeadline(30 * 1000 * 60);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

    }

    private class AccesssTokenAndCommandListener implements Response.Listener<JSONObject> {

        private Settings settings;
        private Context context;
        private JobService service;
        private JSONObject jsonObject;
        private String url;
        private JobParameters params;

        private String accessToken = "";

        public AccesssTokenAndCommandListener(Context context, JobService service, Settings settings, JSONObject jsonObject, String url, JobParameters params, String accessToken) {
            this.context = context;
            this.jsonObject = jsonObject;
            this.url = url;
            this.service = service;
            this.settings = settings;
            this.params = params;
            this.accessToken = accessToken;
        }

        @SuppressLint("NewApi")
        @Override
        public void onResponse(JSONObject response) {
            if(accessToken.equals("")){
                try {
                    accessToken = response.getString("AccessToken");
                    if(!accessToken.equals("")){
                        RequestQueue queue = PatchedVolley.newRequestQueue(context);
                        final JSONObject requestDataObject = new JSONObject();
                        try {
                            requestDataObject.put("id", -1);
                            requestDataObject.put("AccessToken", accessToken);
                        } catch (JSONException e) {

                        }

                        JsonObjectRequest accessRequest = new JsonObjectRequest(Request.Method.PUT, url + "/command", requestDataObject, new AccesssTokenAndCommandListener(context, service, settings, requestDataObject, url, params, accessToken),
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
                                return requestDataObject.toString().getBytes(StandardCharsets.UTF_8);
                            }
                        };
                        queue.add(accessRequest);



                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {

                try {
                    String command = response.getString("Command");
                    if (!command.equals("")) {
                        Sender sender = new FooSender();
                        Logger.init(Thread.currentThread(), context);
                        ComponentHandler ch = new ComponentHandler(settings, context, service, params);
                        ch.setSender(sender);
                        ch.getMessageHandler().setSilent(true);
                        String fmdCommand = (String)settings.get(Settings.SET_FMD_COMMAND);
                        ch.getMessageHandler().handle(sender, fmdCommand + " " + command, context);
                        scheduleJob(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
