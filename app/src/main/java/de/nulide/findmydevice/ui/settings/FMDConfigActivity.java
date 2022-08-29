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
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.CypherUtils;

public class FMDConfigActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {

    private Settings settings;

    private CheckBox checkBoxDeviceWipe;
    private CheckBox checkBoxAccessViaPin;
    private CheckBox checkBoxPinOnly;
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

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        checkBoxDeviceWipe = findViewById(R.id.checkBoxWipeData);
        checkBoxDeviceWipe.setChecked((Boolean) settings.get(Settings.SET_WIPE_ENABLED));
        checkBoxDeviceWipe.setOnCheckedChangeListener(this);
        // if the Pin/Password is not set, then disable this checkbox
        if(settings.get(Settings.SET_PIN).equals("")){
            settings.set(Settings.SET_WIPE_ENABLED, false);
            checkBoxDeviceWipe.setChecked(false);
            checkBoxDeviceWipe.setEnabled(false);
        }

        checkBoxAccessViaPin = findViewById(R.id.checkBoxFMDviaPin);
        checkBoxAccessViaPin.setChecked((Boolean) settings.get(Settings.SET_ACCESS_VIA_PIN));
        checkBoxAccessViaPin.setOnCheckedChangeListener(this);
        // if the Pin/Password is not set, then disable this checkbox
        if(settings.get(Settings.SET_PIN).equals("")){
            settings.set(Settings.SET_ACCESS_VIA_PIN, false);
            checkBoxAccessViaPin.setChecked(false);
            checkBoxAccessViaPin.setEnabled(false);
        }

        checkBoxPinOnly = findViewById(R.id.checkBoxPinOnly);
        checkBoxPinOnly.setChecked((Boolean) settings.get(Settings.SET_PIN_ONLY));
        checkBoxPinOnly.setOnClickListener(this);
        // if the Pin/Password is not set, then disable this checkbox
        if(settings.get(Settings.SET_PIN).equals("")){
            settings.set(Settings.SET_PIN_ONLY, false);
            checkBoxPinOnly.setChecked(false);
            checkBoxPinOnly.setEnabled(false);
        }

        editTextLockScreenMessage = findViewById(R.id.editTextTextLockScreenMessage);
        editTextLockScreenMessage.setText((String) settings.get(Settings.SET_LOCKSCREEN_MESSAGE));
        editTextLockScreenMessage.addTextChangedListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        }else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        buttonEnterPin = findViewById(R.id.buttonEnterPin);
        // if the Pin/Password is not set, change the button colour
        if(settings.get(Settings.SET_PIN).equals("")){
            buttonEnterPin.setBackgroundColor(colorDisabled);
        }else{
            buttonEnterPin.setBackgroundColor(colorEnabled);
        }
        buttonEnterPin.setOnClickListener(this);

        buttonSelectRingtone = findViewById(R.id.buttonSelectRingTone);
        buttonSelectRingtone.setOnClickListener(this);

        editTextFmdCommand = findViewById(R.id.editTextFmdCommand);
        editTextFmdCommand.setText((String) settings.get(Settings.SET_FMD_COMMAND));
        editTextFmdCommand.addTextChangedListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxDeviceWipe) {
            settings.set(Settings.SET_WIPE_ENABLED, isChecked);
        } else if (buttonView == checkBoxAccessViaPin) {
            settings.set(Settings.SET_ACCESS_VIA_PIN, isChecked);
        } else if (buttonView == checkBoxPinOnly){
            settings.set(Settings.SET_PIN_ONLY, isChecked);
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
            settings.set(Settings.SET_LOCKSCREEN_MESSAGE, edited.toString());
        } else if (edited == editTextFmdCommand.getText()) {
            if (edited.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.Toast_Empty_FMDCommand), Toast.LENGTH_LONG).show();
                settings.set(Settings.SET_FMD_COMMAND, "fmd");
            } else {
                settings.set(Settings.SET_FMD_COMMAND, edited.toString().toLowerCase());
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
                        settings.set(Settings.SET_PIN, CypherUtils.hashPassword(text));
                        buttonEnterPin.setBackgroundColor(colorEnabled);
                        checkBoxAccessViaPin.setEnabled(true);
                        checkBoxDeviceWipe.setEnabled(true);
                    }
                    else{
                        Toast.makeText(FMDConfigActivity.this, "Cannot use a blank password. Aborted!", Toast.LENGTH_LONG).show();
                    }
                }
            });
            alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            final AlertDialog dialog = alert.create();
            dialog.show();

            // Disable button "OK" if the PIN contains a space or it's empty.
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!charSequence.toString().equals("") && !charSequence.toString().contains(" "));
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }else if(v == buttonSelectRingtone){
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.Settings_Select_Ringtone));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse((String) settings.get(Settings.SET_RINGER_TONE)));
            this.startActivityForResult(intent, REQUEST_CODE_RINGTONE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RINGTONE) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            settings.set(Settings.SET_RINGER_TONE, uri.toString());
        }
    }

}