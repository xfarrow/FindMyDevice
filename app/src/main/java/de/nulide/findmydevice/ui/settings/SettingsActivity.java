package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.ui.IntroductionActivity;
import de.nulide.findmydevice.ui.LogActivity;
import de.nulide.findmydevice.ui.helper.SettingsViewAdapter;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listSettings;

    private List<String> settingsEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsEntries = new ArrayList<>();
        settingsEntries.add(getString(R.string.Settings_FMDConfig));
        settingsEntries.add(getString(R.string.Settings_WhiteList));
        settingsEntries.add(getString(R.string.Settings_OpenCellId));
        settingsEntries.add(getString(R.string.Settings_Permissions));
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
                settingIntent = new Intent(this, WhiteListActivity.class);
                break;

            case 2:
                settingIntent = new Intent(this, OpenCellIdActivity.class);
                break;

            case 3:
                settingIntent = new Intent(this, IntroductionActivity.class);
                settingIntent.putExtra(IntroductionActivity.POS_KEY, 1);
                break;

            case 4:
                settingIntent = new Intent(this, LogActivity.class);
                break;

            case 5:
                settingIntent = new Intent(this, AboutActivity.class);
                break;
        }
        if(settingIntent != null){
            startActivity(settingIntent);
        }
    }
}