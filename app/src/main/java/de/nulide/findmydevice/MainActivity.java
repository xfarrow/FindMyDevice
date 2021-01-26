package de.nulide.findmydevice;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextMenu;
import android.view.Gravity;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.ui.MainPageViewAdapter;
import de.nulide.findmydevice.ui.WhiteListViewAdapter;
import de.nulide.findmydevice.utils.BCryptUtils;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TextView textViewRunningService;
    private TextView textViewWhiteListCount;
    private TextView textViewCORE;
    private TextView textViewGPS;
    private TextView textViewDND;
    private TextView textViewDeviceAdmin;
    private TextView textViewWriteSecureSettings;
    private TextView textViewOverlay;

    private Button buttonHelp;


    private ListView listWhiteList;
    private WhiteListViewAdapter listWhiteListAdapter;
    private Button buttonAddContact;

    private CheckBox checkBoxDeviceWipe;
    private CheckBox checkBoxAccessViaPin;
    private Button buttonEnterPin;
    private EditText editTextLockScreenMessage;
    private EditText editTextFmdCommand;
    private EditText editTextOpenCellIdKey;
    private Button buttonPermission;

    private WhiteList whiteList;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IO.context = this;
        Notifications.init(this);
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        settings = JSONFactory.convertJSONSettings(IO.read(JSONSettings.class, IO.settingsFileName));
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
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getString(R.string.Tab_Info));
        tabLayout.getTabAt(1).setText(getString(R.string.TAB_Whitelist));
        tabLayout.getTabAt(2).setText(getString(R.string.TAB_Settings));
        tabLayout.getTabAt(3).setText(getString(R.string.Tab_About));

        reloadViews();
        updateViews();
    }

    public void reloadViews() {
        textViewRunningService = findViewById(R.id.textViewServiceRunning);
        textViewWhiteListCount = findViewById(R.id.textViewWhiteListCount);

        listWhiteList = findViewById(R.id.list_whitelist);
        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonAddContact.setOnClickListener(this);

        checkBoxDeviceWipe = findViewById(R.id.checkBoxWipeData);
        checkBoxAccessViaPin = findViewById(R.id.checkBoxFMDviaPin);
        editTextLockScreenMessage = findViewById(R.id.editTextTextLockScreenMessage);
        buttonEnterPin = findViewById(R.id.buttonEnterPin);
        editTextFmdCommand = findViewById(R.id.editTextFmdCommand);
        editTextOpenCellIdKey = findViewById(R.id.editTextOpenCellIDAPIKey);
        buttonPermission = findViewById(R.id.buttonPermissions);

        textViewCORE = findViewById(R.id.textViewCORE);
        textViewGPS = findViewById(R.id.textViewGPS);
        textViewDND = findViewById(R.id.textViewDND);
        textViewDeviceAdmin = findViewById(R.id.textViewDeviceAdmin);
        textViewWriteSecureSettings = findViewById(R.id.textViewWriteSecureSettings);
        textViewOverlay = findViewById(R.id.textViewOverlay);

        buttonHelp = findViewById(R.id.buttonHelp);
    }

    public void updateViews() {
        textViewWhiteListCount.setText("" + whiteList.size());
        listWhiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(listWhiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);

        checkBoxAccessViaPin.setChecked((Boolean) settings.get(Settings.SET_ACCESS_VIA_PIN));
        checkBoxAccessViaPin.setOnCheckedChangeListener(this);
        checkBoxDeviceWipe.setChecked((Boolean) settings.get(Settings.SET_WIPE_ENABLED));
        checkBoxDeviceWipe.setOnCheckedChangeListener(this);
        editTextLockScreenMessage.setText((String) settings.get(Settings.SET_LOCKSCREEN_MESSAGE));
        editTextLockScreenMessage.addTextChangedListener(this);
        buttonEnterPin.setOnClickListener(this);
        editTextFmdCommand.setText((String) settings.get(Settings.SET_FMD_COMMAND));
        editTextFmdCommand.addTextChangedListener(this);
        editTextOpenCellIdKey.setText((String) settings.get(Settings.SET_OPENCELLID_API_KEY));
        editTextOpenCellIdKey.addTextChangedListener(this);
        buttonPermission.setOnClickListener(this);

        if (buttonHelp != null) {
            buttonHelp.setOnClickListener(this);
        }

        textViewRunningService.setText("I think it's running");
        if (Permission.CORE) {
            textViewCORE.setText(getString(R.string.Enabled));
            textViewCORE.setTextColor(Color.GREEN);
        } else {
            textViewCORE.setText(getString(R.string.Disabled));
            textViewCORE.setTextColor(Color.RED);
        }
        if (Permission.GPS) {
            textViewGPS.setText(getString(R.string.Enabled));
            textViewGPS.setTextColor(Color.GREEN);
        } else {
            textViewGPS.setText(getString(R.string.Disabled));
            textViewGPS.setTextColor(Color.RED);
        }
        if (Permission.DND) {
            textViewDND.setText(getString(R.string.Enabled));
            textViewDND.setTextColor(Color.GREEN);
        } else {
            textViewDND.setText(getString(R.string.Disabled));
            textViewDND.setTextColor(Color.RED);
        }
        if (Permission.DEVICE_ADMIN) {
            textViewDeviceAdmin.setText(getString(R.string.Enabled));
            textViewDeviceAdmin.setTextColor(Color.GREEN);
        } else {
            textViewDeviceAdmin.setText(getString(R.string.Disabled));
            textViewDeviceAdmin.setTextColor(Color.RED);
        }
        if (Permission.WRITE_SECURE_SETTINGS) {
            textViewWriteSecureSettings.setText(getString(R.string.Enabled));
            textViewWriteSecureSettings.setTextColor(Color.GREEN);
        } else {
            textViewWriteSecureSettings.setText(getString(R.string.Disabled));
            textViewWriteSecureSettings.setTextColor(Color.RED);
        }
        if (Permission.OVERLAY) {
            textViewOverlay.setText(getString(R.string.Enabled));
            textViewOverlay.setTextColor(Color.GREEN);
        } else {
            textViewOverlay.setText(getString(R.string.Disabled));
            textViewOverlay.setTextColor(Color.RED);
        }

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
        } else if (v == buttonEnterPin) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("PIN");
            alert.setMessage(getString(R.string.EnterPin));
            final EditText input = new EditText(this);
            input.setTransformationMethod(new PasswordTransformationMethod());
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        settings.set(Settings.SET_PIN, BCryptUtils.hashPassword(text));
                    }
                }
            });
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permission.initValues(this);
        settings = JSONFactory.convertJSONSettings(IO.read(JSONSettings.class, IO.settingsFileName));
        if (!settings.isIntroductionPassed() || !Permission.CORE) {
            Intent introductionIntent = new Intent(this, IntroductionActivity.class);
            startActivity(introductionIntent);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (1):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            String cName = phones.getString(phones.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            if (!cNumber.startsWith("0")) {
                                Contact contact = new Contact(cName, cNumber);
                                if (!whiteList.checkForDuplicates(contact)) {
                                    whiteList.add(contact);
                                    updateViews();
                                } else {
                                    Toast toast = Toast.makeText(this, getString(R.string.TOAST_DUBLICATE_CONTACT), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            } else {
                                Toast toast = Toast.makeText(this, getString(R.string.TOAST_LANDCODE_ERROR), Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(getString(R.string.Select_Action));
        menu.add(0, v.getId(), 0, getString(R.string.Delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (item.getTitle() == getString(R.string.Delete)) {
            whiteList.remove(index);
            updateViews();
        } else {
            return false;
        }
        return true;


    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkBoxDeviceWipe) {
            settings.set(Settings.SET_WIPE_ENABLED, isChecked);
        } else if (buttonView == checkBoxAccessViaPin) {
            settings.set(Settings.SET_ACCESS_VIA_PIN, isChecked);
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
        if (edited == editTextLockScreenMessage.getText()) {
            settings.set(Settings.SET_LOCKSCREEN_MESSAGE, edited.toString());
        } else if (edited == editTextFmdCommand.getText()) {
            if (edited.toString().isEmpty()) {
                Toast.makeText(this, "Empty Command not allowed\n Returning to default.[fmd]", Toast.LENGTH_LONG).show();
                settings.set(Settings.SET_FMD_COMMAND, "fmd");
            } else {
                settings.set(Settings.SET_FMD_COMMAND, edited.toString().toLowerCase());
            }
        } else if (edited == editTextOpenCellIdKey.getText()) {
            settings.set(Settings.SET_OPENCELLID_API_KEY, edited.toString());
        }

    }
}
