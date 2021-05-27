package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.FMDSettings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.CypherUtils;

public class FMDSMSActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {

    private FMDSettings FMDSettings;

    private CheckBox checkBoxDeviceWipe;
    private CheckBox checkBoxAccessViaPin;
    private Button buttonEnterPin;
    private Button buttonSelectRingtone;
    private EditText editTextLockScreenMessage;
    private EditText editTextFmdCommand;

    int colorEnabled;
    int colorDisabled;

    private int REQUEST_CODE_RINGTONE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_m_d_config);

        FMDSettings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        checkBoxDeviceWipe = findViewById(R.id.checkBoxWipeData);
        checkBoxDeviceWipe.setChecked((Boolean) FMDSettings.get(FMDSettings.SET_WIPE_ENABLED));
        checkBoxDeviceWipe.setOnCheckedChangeListener(this);

        checkBoxAccessViaPin = findViewById(R.id.checkBoxFMDviaPin);
        checkBoxAccessViaPin.setChecked((Boolean) FMDSettings.get(FMDSettings.SET_ACCESS_VIA_PIN));
        checkBoxAccessViaPin.setOnCheckedChangeListener(this);

        editTextLockScreenMessage = findViewById(R.id.editTextTextLockScreenMessage);
        editTextLockScreenMessage.setText((String) FMDSettings.get(FMDSettings.SET_LOCKSCREEN_MESSAGE));
        editTextLockScreenMessage.addTextChangedListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        }else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        buttonEnterPin = findViewById(R.id.buttonEnterPin);
        if(FMDSettings.get(FMDSettings.SET_PIN).equals("")){
            buttonEnterPin.setBackgroundColor(colorDisabled);
        }else{
            buttonEnterPin.setBackgroundColor(colorEnabled);
        }
        buttonEnterPin.setOnClickListener(this);

        buttonSelectRingtone = findViewById(R.id.buttonSelectRingTone);
        buttonSelectRingtone.setOnClickListener(this);

        editTextFmdCommand = findViewById(R.id.editTextFmdCommand);
        editTextFmdCommand.setText((String) FMDSettings.get(FMDSettings.SET_FMD_COMMAND));
        editTextFmdCommand.addTextChangedListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxDeviceWipe) {
            FMDSettings.set(FMDSettings.SET_WIPE_ENABLED, isChecked);
        } else if (buttonView == checkBoxAccessViaPin) {
            FMDSettings.set(FMDSettings.SET_ACCESS_VIA_PIN, isChecked);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable edited) {
        if (edited == editTextLockScreenMessage.getText()) {
            FMDSettings.set(FMDSettings.SET_LOCKSCREEN_MESSAGE, edited.toString());
        } else if (edited == editTextFmdCommand.getText()) {
            if (edited.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.Toast_Empty_FMDCommand), Toast.LENGTH_LONG).show();
                FMDSettings.set(FMDSettings.SET_FMD_COMMAND, "fmd");
            } else {
                FMDSettings.set(FMDSettings.SET_FMD_COMMAND, edited.toString().toLowerCase());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonEnterPin) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.FMDConfig_Alert_Pin));
            alert.setMessage(getString(R.string.Settings_Enter_Pin));
            final EditText input = new EditText(this);
            input.setTransformationMethod(new PasswordTransformationMethod());
            alert.setView(input);
            alert.setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        FMDSettings.set(FMDSettings.SET_PIN, CypherUtils.hashPassword(text));
                        buttonEnterPin.setBackgroundColor(colorEnabled);
                    }
                }
            });
            alert.show();
        }else if(v == buttonSelectRingtone){
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.Settings_Select_Ringtone));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse((String) FMDSettings.get(FMDSettings.SET_RINGER_TONE)));
            this.startActivityForResult(intent, REQUEST_CODE_RINGTONE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RINGTONE) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            FMDSettings.set(FMDSettings.SET_RINGER_TONE, uri.toString());
        }
    }

}