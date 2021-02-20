package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Ringtone;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Timer;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.Ringer;
import de.nulide.findmydevice.tasks.RingerTimerTask;

public class RingerActivity extends AppCompatActivity implements View.OnClickListener {

    public static String RING_DURATION = "rduration";

    private RingerTimerTask ringerTask;
    private Button buttonStopRinging;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();

        IO.context = this;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        Ringtone ringtone = Ringer.getRingtone(this, (String)settings.get(Settings.SET_RINGER_TONE));

        Timer t = new Timer();
        ringerTask = new RingerTimerTask(t, ringtone, this);
        t.schedule(ringerTask, 0, bundle.getInt(RING_DURATION) * 100);
        ringtone.play();

        buttonStopRinging = findViewById(R.id.buttonStopRinging);
        buttonStopRinging.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v == buttonStopRinging){
            ringerTask.stop();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ringerTask.stop();
        finish();
    }
}