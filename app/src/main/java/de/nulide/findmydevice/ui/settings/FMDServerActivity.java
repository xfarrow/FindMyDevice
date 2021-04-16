package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class FMDServerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher {

    private Settings settings;

    private CheckBox checkBoxFMDServer;
    private EditText editTextFMDServerURL;
    private EditText editTextFMDServerUpdateTime;
    private EditText editTextFMDServerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_m_d_server);

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        checkBoxFMDServer = findViewById(R.id.checkBoxFMDServer);
        checkBoxFMDServer.setChecked((Boolean) settings.get(Settings.SET_FMDSERVER));
        checkBoxFMDServer.setOnCheckedChangeListener(this);

        editTextFMDServerURL = findViewById(R.id.editTextFMDServerUrl);
        editTextFMDServerURL.setText((String) settings.get(Settings.SET_FMDSERVER_URL));
        editTextFMDServerURL.addTextChangedListener(this);

        editTextFMDServerUpdateTime = findViewById(R.id.editTextFMDServerUpdateTime);
        editTextFMDServerUpdateTime.setText(((Integer) settings.get(Settings.SET_FMDSERVER_UPDATE_TIME)).toString());
        editTextFMDServerUpdateTime.addTextChangedListener(this);

        editTextFMDServerID = findViewById(R.id.editTextFMDServerUsername);
        editTextFMDServerID.setText((String) settings.get(Settings.SET_FMDSERVER_ID));
        editTextFMDServerID.addTextChangedListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxFMDServer) {
            settings.set(Settings.SET_FMDSERVER, isChecked);
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
        if (edited == editTextFMDServerURL.getText()) {
            settings.set(Settings.SET_FMDSERVER_URL, edited.toString());
        } else if (edited == editTextFMDServerUpdateTime.getText()) {
            if (edited.toString().isEmpty()) {
                settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, 60);
            } else {
                settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, Integer.parseInt(editTextFMDServerUpdateTime.getText().toString()));
            }
        }else if(edited == editTextFMDServerID.getText()){
            settings.set(Settings.SET_FMDSERVER_ID, edited.toString());
        }
    }
}