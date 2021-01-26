package de.nulide.findmydevice;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.utils.Permission;

public class IntroductionActivity extends AppCompatActivity implements View.OnClickListener {

    public static String POS_KEY = "pos";
    private TextView textViewInfoText;
    private Button buttonNext;
    private Button buttonGivePermission;
    private int position = 0;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            position = bundle.getInt(POS_KEY);
        }
        IO.context = this;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONSettings.class, IO.settingsFileName));

        textViewInfoText = findViewById(R.id.textViewInfoText);
        buttonGivePermission = findViewById(R.id.buttonGivePermission);
        buttonGivePermission.setOnClickListener(this);
        buttonGivePermission.setVisibility(View.INVISIBLE);
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);
        updateViews();

    }

    public void updateViews() {
        switch (position) {
            case 0:
                if ((Integer) settings.get(Settings.SET_INTRODUCTION_VERSION) > 0) {
                    textViewInfoText.setText(getString(R.string.UpdatePermission));
                } else {
                    textViewInfoText.setText(getString(R.string.Introduction));
                }
                break;
            case 1:
                buttonGivePermission.setVisibility(View.VISIBLE);
                textViewInfoText.setText(getString(R.string.Permission_SMS));
                if (Permission.checkSMSPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 2:
                textViewInfoText.setText(getString(R.string.Permission_CONTACTS));
                if (Permission.checkContactsPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 3:
                textViewInfoText.setText(getString(R.string.Permission_GPS));
                if (Permission.checkGPSPermission(this)) {
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textViewInfoText.setText(getString(R.string.Permission_DND));
                    if (Permission.checkDNDPermission(this)) {
                        buttonGivePermission.setTextColor(Color.GREEN);
                    } else {
                        buttonGivePermission.setTextColor(Color.RED);
                    }
                } else {
                    position++;
                    updateViews();
                }
                break;
            case 5:
                textViewInfoText.setText(getString(R.string.Permission_DEVICE_ADMIN));
                if (Permission.checkDeviceAdminPermission(this)) {
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 6:
                textViewInfoText.setText(getString(R.string.Permission_OVERLAY));
                if (Permission.checkOverlayPermission(this)) {
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 7:
                textViewInfoText.setText(getString(R.string.Permission_WRITE_SECURE_SETTINGS));
                if (Permission.checkWriteSecurePermission(this)) {
                    buttonGivePermission.setTextColor(Color.GREEN);
                } else {
                    buttonGivePermission.setTextColor(Color.RED);
                }
                break;
            case 8:
                settings.setIntroductionPassed();
                Intent myIntent = new Intent(this, MainActivity.class);
                finish();
                startActivity(myIntent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonGivePermission) {
            switch (position) {
                case 0:

                    break;
                case 1:
                    Permission.requestSMSPermission(this);
                    break;
                case 2:
                    Permission.requestContactPermission(this);
                    break;
                case 3:
                    Permission.requestGPSPermission(this);
                    break;
                case 4:
                    Permission.requestDNDPermission(this);
                    break;
                case 5:
                    Permission.requestDeviceAdminPermission(this);
                    break;
                case 6:
                    Permission.requestOverlayPermission(this);
                    break;
                case 7:
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/PERMISSION-WRITE_SECURE_SETTINGS"));
                    startActivity(intent);
                    updateViews();
                    break;
            }
        } else if (v == buttonNext) {
            position++;
            updateViews();
        }
    }
}