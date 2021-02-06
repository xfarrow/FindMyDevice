package de.nulide.findmydevice.ui;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.SMS;

public class LockScreenMessage extends AppCompatActivity {

    public static final String SENDER = "sender";
    public static final String CUSTOM_TEXT = "ctext";
    private String sender;

    private TextView tvLockScreenMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_message);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        Bundle bundle = getIntent().getExtras();
        sender = bundle.getString(SENDER);
        Settings settings;
        IO.context = this;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        tvLockScreenMessage = findViewById(R.id.textViewLockScreenMessage);
        if (bundle.containsKey(CUSTOM_TEXT)) {
            tvLockScreenMessage.setText(bundle.getString(CUSTOM_TEXT));
        } else {
            tvLockScreenMessage.setText((String) settings.get(Settings.SET_LOCKSCREEN_MESSAGE));
        }
    }

    @Override
    protected void onPause() {
        SMS.sendMessage(sender, "LockScreenMessage: Usage detected.");
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        SMS.sendMessage(sender, "LockScreenMessage: Back-Button pressed.");
        finish();
    }


}