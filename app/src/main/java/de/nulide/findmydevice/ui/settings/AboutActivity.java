package de.nulide.findmydevice.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.nulide.findmydevice.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewFMDVersion;
    private Button buttonHelp;

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

        buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/home"));
        startActivity(helpIntent);
    }
}