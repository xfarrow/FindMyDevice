package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.services.CameraService;

public class DummyCameraActivity extends AppCompatActivity {

    public static String CAMERA = "camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int camera = 0;
        Bundle bundle = getIntent().getExtras();
        if(!bundle.isEmpty()){
            camera = bundle.getInt(CAMERA);
        }
        CameraService.startService(this, camera);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 100);

    }
}