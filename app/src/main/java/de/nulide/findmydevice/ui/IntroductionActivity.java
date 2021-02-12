package de.nulide.findmydevice.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.nulide.findmydevice.MainActivity;
import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.Permission;

public class IntroductionActivity extends AppCompatActivity implements View.OnClickListener {

    public static String POS_KEY = "pos";

    private TextView textViewInfoText;
    private Button buttonNext;
    private Button buttonGivePermission;
    private int position = 0;
    private Settings settings;

    private int colorEnabled;
    private int colorDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            position = bundle.getInt(POS_KEY);
        }
        IO.context = this;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        textViewInfoText = findViewById(R.id.textViewInfoText);
        buttonGivePermission = findViewById(R.id.buttonGivePermission);
        buttonGivePermission.setOnClickListener(this);
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        }else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        updateViews();

    }

    public void updateViews() {
        switch (position) {
            case 0:
                buttonGivePermission.setText(getString(R.string.About_Help));
                if ((Integer) settings.get(Settings.SET_INTRODUCTION_VERSION) > 0) {
                    textViewInfoText.setText(getString(R.string.Introduction_UpdatePermission));
                } else {
                    textViewInfoText.setText(getString(R.string.Introduction_Introduction));
                }
                break;
            case 1:
                buttonGivePermission.setText(getString(R.string.Introduction_Give_Permission));
                textViewInfoText.setText(getString(R.string.Permission_SMS));
                if (Permission.checkSMSPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 2:
                textViewInfoText.setText(getString(R.string.Permission_CONTACTS));
                if (Permission.checkContactsPermission(this)) {
                    buttonNext.setEnabled(true);
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonNext.setEnabled(false);
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 3:
                textViewInfoText.setText(getString(R.string.Permission_GPS));
                if (Permission.checkGPSPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textViewInfoText.setText(getString(R.string.Permission_DND));
                    if (Permission.checkDNDPermission(this)) {
                        buttonGivePermission.setBackgroundColor(colorEnabled);
                    } else {
                        buttonGivePermission.setBackgroundColor(colorDisabled);
                    }
                } else {
                    position++;
                    updateViews();
                }
                break;
            case 5:
                textViewInfoText.setText(getString(R.string.Permission_DEVICE_ADMIN));
                if (Permission.checkDeviceAdminPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 6:
                textViewInfoText.setText(getString(R.string.Permission_OVERLAY));
                if (Permission.checkOverlayPermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
                }
                break;
            case 7:
                textViewInfoText.setText(getString(R.string.Permission_WRITE_SECURE_SETTINGS));
                if (Permission.checkWriteSecurePermission(this)) {
                    buttonGivePermission.setBackgroundColor(colorEnabled);
                } else {
                    buttonGivePermission.setBackgroundColor(colorDisabled);
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
                    Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/home"));
                    startActivity(helpIntent);
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