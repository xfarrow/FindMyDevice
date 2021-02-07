package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.nulide.findmydevice.R;

public class CrashedActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String CRASH_LOG = "log";

    private TextView textViewCrashLog;
    private Button buttonSendLog;

    private String crashLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashed);
        crashLog = getIntent().getExtras().getString(CRASH_LOG);

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