package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;

public class OpenCellIdActivity extends AppCompatActivity implements TextWatcher {

    private Settings settings;

    private EditText editTextOpenCellIdKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cell_id);

        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        editTextOpenCellIdKey = findViewById(R.id.editTextOpenCellIDAPIKey);
        editTextOpenCellIdKey.setText((String) settings.get(Settings.SET_OPENCELLID_API_KEY));
        editTextOpenCellIdKey.addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable edited) {
        if (edited == editTextOpenCellIdKey.getText()) {
            settings.set(Settings.SET_OPENCELLID_API_KEY, edited.toString());
        }
    }
}