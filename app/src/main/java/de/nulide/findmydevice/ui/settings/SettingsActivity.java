package de.nulide.findmydevice.ui.settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.ui.IntroductionActivity;
import de.nulide.findmydevice.ui.LogActivity;
import de.nulide.findmydevice.ui.helper.SettingsViewAdapter;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listSettings;

    private List<String> settingsEntries;

    private final int EXPORT_REQ_CODE = 30;

    private final int IMPORT_REQ_CODE = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsEntries = new ArrayList<>();
        settingsEntries.add(getString(R.string.Settings_FMDConfig));
        settingsEntries.add(getString(R.string.Settings_FMDServer));
        settingsEntries.add(getString(R.string.Settings_WhiteList));
        settingsEntries.add(getString(R.string.Settings_OpenCellId));
        settingsEntries.add(getString(R.string.Settings_Permissions));
        settingsEntries.add(getString(R.string.Settings_Export));
        settingsEntries.add(getString(R.string.Settings_Import));
        settingsEntries.add(getString(R.string.Settings_Logs));
        settingsEntries.add(getString(R.string.Settings_About));


        listSettings = findViewById(R.id.listSettings);
        listSettings.setAdapter(new SettingsViewAdapter(this, settingsEntries));
        listSettings.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent settingIntent = null;
        switch(position){
            case 0:
                settingIntent = new Intent(this, FMDConfigActivity.class);
                break;
            case 1:
                settingIntent = new Intent(this, FMDServerActivity.class);
                break;
            case 2:
                settingIntent = new Intent(this, WhiteListActivity.class);
                break;

            case 3:
                settingIntent = new Intent(this, OpenCellIdActivity.class);
                break;

            case 4:
                settingIntent = new Intent(this, IntroductionActivity.class);
                settingIntent.putExtra(IntroductionActivity.POS_KEY, 1);
                break;

            case 5:
                settingIntent = null;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.putExtra(Intent.EXTRA_TITLE, IO.settingsFileName);
                intent.setType("*/*");
                startActivityForResult(intent, EXPORT_REQ_CODE);
                break;

            case 6:
                settingIntent = null;
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, IMPORT_REQ_CODE);

                break;

            case 7:
                settingIntent = new Intent(this, LogActivity.class);
                break;

            case 8:
                settingIntent = new Intent(this, AboutActivity.class);
                break;
        }
        if(settingIntent != null){
            startActivity(settingIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);


                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder json = new StringBuilder();
                    try {
                        String line;

                        while ((line = br.readLine()) != null) {
                            json.append(line);
                            json.append('\n');
                        }
                        br.close();
                        String text = json.toString();
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        if (!text.isEmpty()) {
                            Settings settings = mapper.readValue(text, Settings.class);
                            settings.setNow(Settings.SET_INTRODUCTION_VERSION, settings.get(Settings.SET_INTRODUCTION_VERSION));
                            finish();
                            startActivity(getIntent());

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == EXPORT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    ParcelFileDescriptor sco = this.getContentResolver().openFileDescriptor(uri, "w");
                    PrintWriter out = new PrintWriter(new FileOutputStream(sco.getFileDescriptor()));
                    Settings settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(settings);
                    out.write(json);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}