package de.nulide.findmydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Ringtone;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Timer;

import de.nulide.findmydevice.utils.Ringer;
import de.nulide.findmydevice.utils.RingerTimerTask;

public class RingerActivity extends AppCompatActivity implements View.OnClickListener {

    public static String RING_DURATION = "rduration";

    private RingerTimerTask ringerTask;
    private Button buttonStopRinging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();

        Ringtone ringtone = Ringer.getRingtone(this);

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