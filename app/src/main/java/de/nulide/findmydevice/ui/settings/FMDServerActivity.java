package de.nulide.findmydevice.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.unifiedpush.android.connector.Registration;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Keys;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.KeyIO;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.receiver.PushReceiver;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.CypherUtils;

public class FMDServerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {

    private Settings settings;

    private CheckBox checkBoxFMDServer;
    private CheckBox checkBoxFMDServerAutoUpload;
    private EditText editTextFMDServerURL;
    private EditText editTextFMDServerUpdateTime;
    private TextView textViewFMDServerID;
    private Button registerButton;
    private Button deleteButton;
    private CheckBox checkBoxFMDServerGPS;
    private CheckBox checkBoxFMDServerCell;

    private int colorEnabled;
    private int colorDisabled;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_m_d_server);

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        this.context = this;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        } else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }


        checkBoxFMDServer = findViewById(R.id.checkBoxFMDServer);
        checkBoxFMDServer.setChecked((Boolean) settings.get(Settings.SET_FMDSERVER_UPLOAD_SERVICE));
        checkBoxFMDServer.setOnCheckedChangeListener(this);
        if(((String) settings.get(Settings.SET_FMDSERVER_ID)).isEmpty()){
           checkBoxFMDServer.setEnabled(false);
        }

        checkBoxFMDServerAutoUpload = findViewById(R.id.checkBoxFMDServerAutoUpload);
        checkBoxFMDServerAutoUpload.setChecked((Boolean) settings.get(Settings.SET_FMDSERVER_AUTO_UPLOAD));
        checkBoxFMDServerAutoUpload.setOnCheckedChangeListener(this);

        editTextFMDServerURL = findViewById(R.id.editTextFMDServerUrl);
        editTextFMDServerURL.setText((String) settings.get(Settings.SET_FMDSERVER_URL));
        editTextFMDServerURL.addTextChangedListener(this);

        editTextFMDServerUpdateTime = findViewById(R.id.editTextFMDServerUpdateTime);
        editTextFMDServerUpdateTime.setText(((Integer) settings.get(Settings.SET_FMDSERVER_UPDATE_TIME)).toString());
        editTextFMDServerUpdateTime.addTextChangedListener(this);

        textViewFMDServerID = findViewById(R.id.textViewID);

        deleteButton = findViewById(R.id.buttonDeleteData);
        deleteButton.setOnClickListener(this);

        if (!((String) settings.get(Settings.SET_FMDSERVER_ID)).isEmpty()) {
            textViewFMDServerID.setText((String) settings.get(Settings.SET_FMDSERVER_ID));
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setEnabled(true);
        }

        registerButton = findViewById(R.id.buttonRegisterOnServer);
        Boolean passwordSet = (Boolean) settings.get(Settings.SET_FMDSERVER_PASSWORD_SET);
        if (passwordSet) {
            registerButton.setBackgroundColor(colorEnabled);
        } else {
            registerButton.setBackgroundColor(colorDisabled);
        }
        if(((String) settings.get(Settings.SET_FMDSERVER_URL)).isEmpty()){
            registerButton.setEnabled(false);
        }
        registerButton.setOnClickListener(this);

        if(!(Boolean) settings.get(Settings.SET_FIRST_TIME_FMD_SERVER)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.Settings_FMDServer))
                    .setMessage(this.getString(R.string.Alert_First_time_fmdserver))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            settings.set(Settings.SET_FIRST_TIME_FMD_SERVER, true);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

        checkBoxFMDServerGPS = findViewById(R.id.checkBoxFMDServerGPS);
        checkBoxFMDServerCell = findViewById(R.id.checkBoxFMDServerCell);
        switch((Integer)settings.get(Settings.SET_FMDSERVER_LOCATION_TYPE)){
            case 0:
                checkBoxFMDServerGPS.setChecked(true);
                break;
            case 1:
                checkBoxFMDServerCell.setChecked(true);
                break;
            case 2:
                checkBoxFMDServerGPS.setChecked(true);
                checkBoxFMDServerCell.setChecked(true);
        }
        checkBoxFMDServerGPS.setOnCheckedChangeListener(this);
        checkBoxFMDServerCell.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxFMDServer) {
            settings.setNow(Settings.SET_FMDSERVER_UPLOAD_SERVICE, isChecked);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    FMDServerService.scheduleJob(this, 0);
                    Registration reg = new Registration();
                    reg.registerAppWithDialog(context);
                    new PushReceiver();
                }
            }else{
                FMDServerService.cancleAll(this);
            }
        }else if(buttonView == checkBoxFMDServerAutoUpload){
            settings.set(Settings.SET_FMDSERVER_AUTO_UPLOAD, isChecked);
        }else if(buttonView == checkBoxFMDServerCell || buttonView == checkBoxFMDServerGPS){
            if(checkBoxFMDServerGPS.isChecked() && checkBoxFMDServerCell.isChecked()){
                settings.set(Settings.SET_FMDSERVER_LOCATION_TYPE, 2);
            }else if(checkBoxFMDServerGPS.isChecked()){
                settings.set(Settings.SET_FMDSERVER_LOCATION_TYPE, 0);
            }else if(checkBoxFMDServerCell.isChecked()){
                settings.set(Settings.SET_FMDSERVER_LOCATION_TYPE, 1);
            }else{
                settings.set(Settings.SET_FMDSERVER_LOCATION_TYPE, 0);
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
            settings.set(Settings.SET_FMDSERVER_URL, edited.toString());
            if(edited.toString().isEmpty()){
                registerButton.setEnabled(false);
            }else{
                registerButton.setEnabled(true);
            }
        } else if (edited == editTextFMDServerUpdateTime.getText()) {
            if (edited.toString().isEmpty()) {
                settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, 60);
            } else {
                settings.set(Settings.SET_FMDSERVER_UPDATE_TIME, Integer.parseInt(editTextFMDServerUpdateTime.getText().toString()));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == registerButton) {
            WebView webView = new WebView(context);
            webView.loadUrl(editTextFMDServerURL.getText().toString()+"/ds.html");

            final AlertDialog.Builder pinAlert = new AlertDialog.Builder(this);
            pinAlert.setTitle(getString(R.string.FMDConfig_Alert_Password));
            pinAlert.setMessage(getString(R.string.Settings_Enter_Password));
            final EditText input = new EditText(this);
            input.setTransformationMethod(new PasswordTransformationMethod());
            pinAlert.setView(input);
            pinAlert.setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        Keys keys = CypherUtils.genKeys(text);
                        KeyIO.writeKeys(keys);
                        String hashedPW = CypherUtils.hashWithPKBDF2(text);
                        KeyIO.writeHashedPassword(hashedPW);
                        settings.setNow(Settings.SET_FMDSERVER_PASSWORD_SET, true);
                        FMDServerService.registerOnServer(context, (String) settings.get(Settings.SET_FMDSERVER_URL), keys.getEncryptedPrivateKey(), keys.getBase64PublicKey(), hashedPW);
                        finish();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                startActivity(getIntent());
                            }
                        }, 1500);
                    }
                }
            });

            AlertDialog.Builder privacyPolicy = new AlertDialog.Builder(context);
            privacyPolicy.setTitle(getString(R.string.Settings_FMDServer_Alert_PrivacyPolicy_Title))
                    .setView(webView)
                    .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pinAlert.show();

                        }
                    })
                    .setNegativeButton(getString(R.string.cancle), null)
                    .show();
        }else if(v == deleteButton){
            AlertDialog.Builder privacyPolicy = new AlertDialog.Builder(context);
            privacyPolicy.setTitle(getString(R.string.Settings_FMDServer_Alert_DeleteData))
                    .setMessage(R.string.Settings_FMDServer_Alert_DeleteData_Desc)
                    .setPositiveButton(getString(R.string.Ok), new DialogClickListenerForUnregistration(this))
                    .setNegativeButton(getString(R.string.cancle), null)
                    .show();
        }
    }

    private class DialogClickListenerForUnregistration implements DialogInterface.OnClickListener{

        private Context context;

        public DialogClickListenerForUnregistration(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            FMDServerService.unregisterOnServer(context);
            finish();
            startActivity(getIntent());
        }
    }

}