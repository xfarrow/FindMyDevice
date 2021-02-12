package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import de.nulide.findmydevice.R;

public class AboutActivity extends AppCompatActivity {

    private TextView textViewFMDVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        textViewFMDVersion = findViewById(R.id.textViewFMDVersion);

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(
                    getPackageName(), 0);
            textViewFMDVersion.setText(info.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            textViewFMDVersion.setText(getString(R.string.Error));
        }
    }
}