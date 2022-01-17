package de.nulide.findmydevice.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
import de.nulide.findmydevice.ui.settings.OpenCellIdActivity;
import de.nulide.findmydevice.utils.CypherUtils;
import de.nulide.findmydevice.utils.Logger;

public class CameraService extends JobService implements Camera.PictureCallback {

    private Settings settings;

    @Override
    public boolean onStartJob(JobParameters params) {
        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

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
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CameraService.class);
        JobInfo.Builder builder = new JobInfo.Builder(2848, serviceComponent);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(0);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String picture = CypherUtils.encodeBase64(data);
        FMDServerService.sendPicture(this, picture, (String) settings.get(Settings.SET_FMDSERVER_URL), (String) settings.get(Settings.SET_FMDSERVER_ID));
    }
}


