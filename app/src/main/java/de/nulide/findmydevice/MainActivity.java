package de.nulide.findmydevice;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.ui.IntroductionActivity;
import de.nulide.findmydevice.ui.LogActivity;
import de.nulide.findmydevice.ui.settings.SettingsActivity;
import de.nulide.findmydevice.ui.helper.MainPageViewAdapter;
import de.nulide.findmydevice.ui.helper.WhiteListViewAdapter;
import de.nulide.findmydevice.utils.BCryptUtils;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher, ViewPager.OnPageChangeListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TextView textViewFMDCommandName;
    private TextView textViewWhiteListCount;
    private TextView textViewCORE;
    private TextView textViewGPS;
    private TextView textViewDND;
    private TextView textViewDeviceAdmin;
    private TextView textViewWriteSecureSettings;
    private TextView textViewOverlay;

    private TextView textViewFMDVersion;
    private Button buttonHelp;

    private ListView listWhiteList;
    private WhiteListViewAdapter listWhiteListAdapter;
    private Button buttonAddContact;

    private Button buttonPermission;
    private Button buttonLog;


    private WhiteList whiteList;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IO.context = this;
        Logger.init(Thread.currentThread(), this);
        Notifications.init(this);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        Permission.initValues(this);
        if (!settings.isIntroductionPassed() || !Permission.CORE) {
            Intent introductionIntent = new Intent(this, IntroductionActivity.class);
            startActivity(introductionIntent);
        }
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewPager);
        MainPageViewAdapter mPageViewAdapter = new MainPageViewAdapter(this);
        viewPager.setAdapter(mPageViewAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getString(R.string.Main_Title));
        tabLayout.getTabAt(1).setText(getString(R.string.TAB_Whitelist));
        tabLayout.getTabAt(2).setText(getString(R.string.TAB_Settings));
        tabLayout.getTabAt(3).setText(getString(R.string.Tab_About));

        reloadViews();
        updateViews();
    }

    public void reloadViews() {
        textViewFMDCommandName = findViewById(R.id.textViewFMDCommandName);
        textViewWhiteListCount = findViewById(R.id.textViewWhiteListCount);

        listWhiteList = findViewById(R.id.listWhiteList);
        buttonAddContact = findViewById(R.id.buttonAddContact);
        buttonAddContact.setOnClickListener(this);

        buttonPermission = findViewById(R.id.buttonPermissions);
        buttonLog = findViewById(R.id.buttonLog);

        textViewCORE = findViewById(R.id.textViewCORE);
        textViewGPS = findViewById(R.id.textViewGPS);
        textViewDND = findViewById(R.id.textViewDND);
        textViewDeviceAdmin = findViewById(R.id.textViewDeviceAdmin);
        textViewWriteSecureSettings = findViewById(R.id.textViewWriteSecureSettings);
        textViewOverlay = findViewById(R.id.textViewOverlay);

        textViewFMDVersion = findViewById(R.id.textViewFMDVersion);
        buttonHelp = findViewById(R.id.buttonHelp);
    }

    public void updateViews() {
        textViewFMDCommandName.setText((String) settings.get(Settings.SET_FMD_COMMAND));
        textViewWhiteListCount.setText("" + whiteList.size());
        listWhiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(listWhiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);

        buttonPermission.setOnClickListener(this);
        buttonLog.setOnClickListener(this);

        if (buttonHelp != null) {
            PackageManager manager = getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(
                        getPackageName(), 0);
                textViewFMDVersion.setText(info.versionName);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            buttonHelp.setOnClickListener(this);
        }
        int colorEnabled;
        int colorDisabled;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorEnabled = getColor(R.color.colorEnabled);
            colorDisabled = getColor(R.color.colorDisabled);
        }else {
            colorEnabled = getResources().getColor(R.color.colorEnabled);
            colorDisabled = getResources().getColor(R.color.colorDisabled);
        }

        if (Permission.CORE) {
            textViewCORE.setText(getString(R.string.Enabled));
            textViewCORE.setTextColor(colorEnabled);
        } else {
            textViewCORE.setText(getString(R.string.Disabled));
            textViewCORE.setTextColor(colorDisabled);
        }
        if (Permission.GPS) {
            textViewGPS.setText(getString(R.string.Enabled));
            textViewGPS.setTextColor(colorEnabled);
        } else {
            textViewGPS.setText(getString(R.string.Disabled));
            textViewGPS.setTextColor(colorDisabled);
        }
        if (Permission.DND) {
            textViewDND.setText(getString(R.string.Enabled));
            textViewDND.setTextColor(colorEnabled);
        } else {
            textViewDND.setText(getString(R.string.Disabled));
            textViewDND.setTextColor(colorDisabled);
        }
        if (Permission.DEVICE_ADMIN) {
            textViewDeviceAdmin.setText(getString(R.string.Enabled));
            textViewDeviceAdmin.setTextColor(colorEnabled);
        } else {
            textViewDeviceAdmin.setText(getString(R.string.Disabled));
            textViewDeviceAdmin.setTextColor(colorDisabled);
        }
        if (Permission.WRITE_SECURE_SETTINGS) {
            textViewWriteSecureSettings.setText(getString(R.string.Enabled));
            textViewWriteSecureSettings.setTextColor(colorEnabled);
        } else {
            textViewWriteSecureSettings.setText(getString(R.string.Disabled));
            textViewWriteSecureSettings.setTextColor(colorDisabled);
        }
        if (Permission.OVERLAY) {
            textViewOverlay.setText(getString(R.string.Enabled));
            textViewOverlay.setTextColor(colorEnabled);
        } else {
            textViewOverlay.setText(getString(R.string.Disabled));
            textViewOverlay.setTextColor(colorDisabled);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuItemSettings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == buttonAddContact) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        } else if (v == buttonPermission) {
            Intent permissionIntent = new Intent(this, IntroductionActivity.class);
            permissionIntent.putExtra(IntroductionActivity.POS_KEY, 1);
            startActivity(permissionIntent);
        } else if (v == buttonHelp) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/Nulide/findmydevice/-/wikis/home"));
            startActivity(intent);
        } else if(v == buttonLog){
            Intent logActivity = new Intent(this, LogActivity.class);
            startActivity(logActivity);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permission.initValues(this);
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        if (!settings.isIntroductionPassed() || !Permission.CORE) {
            Intent introductionIntent = new Intent(this, IntroductionActivity.class);
            startActivity(introductionIntent);
        }
    }

      @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable edited) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
