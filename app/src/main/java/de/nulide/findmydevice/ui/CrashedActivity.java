package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONLog;
import de.nulide.findmydevice.data.io.json.JSONMap;

public class CrashedActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String CRASH_LOG = "log";

    private TextView textViewCrashLog;
    private Button buttonSendLog;

    private String crashLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashed);
        IO.context = this;
        Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        LogData log = JSONFactory.convertJSONLog(IO.read(JSONLog.class, IO.logFileName));
        crashLog = log.get((Integer)settings.get(Settings.SET_APP_CRASHED_LOG_ENTRY)).getText();
        settings.set(Settings.SET_APP_CRASHED_LOG_ENTRY, -1);

        textViewCrashLog = findViewById(R.id.textViewCrash);
        textViewCrashLog.setText(crashLog);

        buttonSendLog = findViewById(R.id.buttonSendLog);
        buttonSendLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType ("plain/text");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"Nulide@tutanota.de"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "CrashLog");
        intent.putExtra (Intent.EXTRA_TEXT, crashLog);
        startActivity (intent);
    }
}