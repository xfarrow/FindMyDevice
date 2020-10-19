package de.nulide.findmydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;

import de.nulide.findmydevice.receiver.SMSReceiver;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SMSReceiver rec = new SMSReceiver();
        registerReceiver(rec, new IntentFilter(SMSReceiver.SMS_RECEIVED));
    }
}
