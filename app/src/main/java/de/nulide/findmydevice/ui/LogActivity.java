package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;

public class LogActivity extends AppCompatActivity {

    private ListView listLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        LogData logData = JSONFactory.convertJSONLog(IO.read(JSONMap.class, IO.logFileName));
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                logData.getList());

        listLog = findViewById(R.id.listLog);
        listLog.setAdapter(adapter);
    }
}