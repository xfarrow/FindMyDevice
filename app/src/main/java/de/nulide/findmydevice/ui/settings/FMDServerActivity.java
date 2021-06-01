package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.Keys;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.CypherUtils;

public class FMDServerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {

    private Settings Settings;

    private CheckBox checkBoxFMDServer;
    private EditText editTextFMDServerURL;
    private EditText editTextFMDServerUpdateTime;
    private TextView textViewFMDServerID;
    private Button registerButton;

    private int colorEnabled;
    private int colorDisabled;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_m_d_server);

        Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        this.context = this;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        } else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }


        checkBoxFMDServer = findViewById(R.id.checkBoxFMDServer);
        checkBoxFMDServer.setChecked((Boolean) Settings.get(Settings.SET_FMDSERVER));
        checkBoxFMDServer.setOnCheckedChangeListener(this);
        if(!(Boolean) Settings.get(Settings.SET_FMDSERVER_PASSWORD_SET)){
           checkBoxFMDServer.setEnabled(false);
        }

        editTextFMDServerURL = findViewById(R.id.editTextFMDServerUrl);
        editTextFMDServerURL.setText((String) Settings.get(Settings.SET_FMDSERVER_URL));
        editTextFMDServerURL.addTextChangedListener(this);

        editTextFMDServerUpdateTime = findViewById(R.id.editTextFMDServerUpdateTime);
        editTextFMDServerUpdateTime.setText(((Integer) Settings.get(Settings.SET_FMDSERVER_UPDATE_TIME)).toString());
        editTextFMDServerUpdateTime.addTextChangedListener(this);

        textViewFMDServerID = findViewById(R.id.textViewID);
        if (!((String) Settings.get(Settings.SET_FMDSERVER_ID)).isEmpty()) {
            textViewFMDServerID.setText((String) Settings.get(Settings.SET_FMDSERVER_ID));
        }

        registerButton = findViewById(R.id.buttonRegisterOnServer);
        Boolean passwordSet = (Boolean) Settings.get(Settings.SET_FMDSERVER_PASSWORD_SET);
        if (passwordSet) {
            registerButton.setBackgroundColor(colorEnabled);
        } else {
            registerButton.setBackgroundColor(colorDisabled);
        }
        if(((String) Settings.get(Settings.SET_FMDSERVER_URL)).isEmpty()){
            registerButton.setEnabled(false);
        }
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxFMDServer) {
            Settings.setNow(Settings.SET_FMDSERVER, isChecked);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    FMDServerService.scheduleJob(this, 0);
                }
            }
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
            Settings.set(Settings.SET_FMDSERVER_URL, edited.toString());
            if(edited.toString().isEmpty()){
                registerButton.setEnabled(false);
            }else{
                registerButton.setEnabled(true);
            }
        } else if (edited == editTextFMDServerUpdateTime.getText()) {
            if (edited.toString().isEmpty()) {
                Settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, 60);
            } else {
                Settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, Integer.parseInt(editTextFMDServerUpdateTime.getText().toString()));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == registerButton) {
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
                        Keys keys = CypherUtils.genKeys(text);
                        KeyIO.writeKeys(keys);
                        Settings.set(Settings.SET_FMDSERVER_PASSWORD_SET, true);
                        registerButton.setBackgroundColor(colorEnabled);
                        FMDServerService.registerOnServer(context, (String) Settings.get(Settings.SET_FMDSERVER_URL), keys.getEncryptedPrivateKey());
                        checkBoxFMDServer.setEnabled(true);
                    }
                }
            });
            alert.show();
        }
    }
}