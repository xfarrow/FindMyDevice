package de.nulide.findmydevice.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import java.io.IOException;

import de.nulide.findmydevice.data.Keys;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.ui.MainActivity;
import de.nulide.findmydevice.ui.settings.OpenCellIdActivity;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;

public class CameraService extends Service implements Camera.PictureCallback {

    private Settings settings;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        PendingIntent action = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class),
                0);

        Notification notification = Notifications.getForegroundNotification(this).setContentTitle("FMD Camera").setContentIntent(action).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(45, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
        }else{
            startForeground(45, notification);
        }

        Camera mCamera = Camera.open();
        SurfaceTexture st = new SurfaceTexture(10);


        try {
            mCamera.setPreviewTexture(st);
            Camera.Parameters parameters = mCamera.getParameters();

            int height = 0;
            int width = 0;
            for( Camera.Size dim: parameters.getSupportedPictureSizes()){
                if (height < dim.height){
                    height = dim.height;
                    width = dim.width;
                }
            }
            parameters.setPictureSize(width, height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();

            mCamera.takePicture(null, null, this);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, CameraService.class);
        context.startForegroundService(intent);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String picture = CypherUtils.encodeBase64(data);
        FMDServerService.sendPicture(this, picture, (String) settings.get(Settings.SET_FMDSERVER_URL), (String) settings.get(Settings.SET_FMDSERVER_ID));
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


