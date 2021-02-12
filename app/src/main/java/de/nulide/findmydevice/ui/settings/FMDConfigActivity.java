package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import de.nulide.findmydevice.utils.BCryptUtils;

public class FMDConfigActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {

    private Settings settings;

    private CheckBox checkBoxDeviceWipe;
    private CheckBox checkBoxAccessViaPin;
    private Button buttonEnterPin;
    private EditText editTextLockScreenMessage;
    private EditText editTextFmdCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_m_d_config);

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        checkBoxDeviceWipe = findViewById(R.id.checkBoxWipeData);
        checkBoxDeviceWipe.setChecked((Boolean) settings.get(Settings.SET_WIPE_ENABLED));
        checkBoxDeviceWipe.setOnCheckedChangeListener(this);

        checkBoxAccessViaPin = findViewById(R.id.checkBoxFMDviaPin);
        checkBoxAccessViaPin.setChecked((Boolean) settings.get(Settings.SET_ACCESS_VIA_PIN));
        checkBoxAccessViaPin.setOnCheckedChangeListener(this);

        editTextLockScreenMessage = findViewById(R.id.editTextTextLockScreenMessage);
        editTextLockScreenMessage.setText((String) settings.get(Settings.SET_LOCKSCREEN_MESSAGE));
        editTextLockScreenMessage.addTextChangedListener(this);

        buttonEnterPin = findViewById(R.id.buttonEnterPin);
        buttonEnterPin.setOnClickListener(this);

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
                Toast.makeText(this, "Empty Command not allowed\n Returning to default.[fmd]", Toast.LENGTH_LONG).show();
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
            alert.setTitle("PIN");
            alert.setMessage(getString(R.string.EnterPin));
            final EditText input = new EditText(this);
            input.setTransformationMethod(new PasswordTransformationMethod());
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        settings.set(Settings.SET_PIN, BCryptUtils.hashPassword(text));
                    }
                }
            });
            alert.show();
        }
    }
}