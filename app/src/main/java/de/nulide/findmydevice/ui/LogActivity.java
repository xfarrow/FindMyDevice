package de.nulide.findmydevice.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONLog;

public class LogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listLog;
    private LogData logData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logData = JSONFactory.convertJSONLog(IO.read(JSONLog.class, IO.logFileName));
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                logData.getDates());

        listLog = findViewById(R.id.listLog);
        listLog.setAdapter(adapter);
        listLog.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Log_Alert_LogData))
                .setMessage(logData.get(position).getText())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}