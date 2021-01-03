package de.nulide.findmydevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.ui.MainPageViewAdapter;
import de.nulide.findmydevice.ui.WhiteListViewAdapter;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.utils.ServiceHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TextView textViewRunningService;
    private TextView textViewWhiteListCount;


    private ListView listWhiteList;
    private WhiteListViewAdapter listWhiteListAdapter;
    private Button buttonAddContact;

    private CheckBox checkBoxDeviceWipe;
    private EditText editTextPin;
    private EditText editTextLockScreenMessage;

    private WhiteList whiteList;
    private Settings settings;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Permission.checkAll(this)) {
            Intent myIntent = new Intent(this, PermissionsActivity.class);
            startActivity(myIntent);
        }

        IO.context = this;
        whiteList = IO.read(WhiteList.class, IO.whiteListFileName);
        settings = IO.read(Settings.class, IO.settingsFileName);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewPager);
        MainPageViewAdapter mPageViewAdapter = new MainPageViewAdapter(this);
        viewPager.setAdapter(mPageViewAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Info");
        tabLayout.getTabAt(1).setText("WhiteList");
        tabLayout.getTabAt(2).setText("Settings");

        reloadViews();
        updateViews();
        ServiceHandler.startServiceSomehow(this);
    }

    public void reloadViews() {
        textViewRunningService = findViewById(R.id.textViewServiceRunning);
        textViewWhiteListCount = findViewById(R.id.textViewWhiteListCount);

        listWhiteList = findViewById(R.id.list_whitelist);
        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonAddContact.setOnClickListener(this);

        checkBoxDeviceWipe = findViewById(R.id.checkBoxWipeData);
        editTextLockScreenMessage = findViewById(R.id.editTextTextLockScreenMessage);
        editTextPin = findViewById(R.id.editTextPin);
    }

    public void updateViews() {
        if (ServiceHandler.isRunning(this)) {
            textViewRunningService.setText("running");
            textViewRunningService.setTextColor(Color.GREEN);
        } else {
            textViewRunningService.setText("not running");
            textViewRunningService.setTextColor(Color.RED);
        }
        textViewWhiteListCount.setText("" + whiteList.size());
        listWhiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(listWhiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);

        checkBoxDeviceWipe.setChecked(settings.isWipeEnabled());
        checkBoxDeviceWipe.setOnCheckedChangeListener(this);
        editTextLockScreenMessage.setText(settings.getLockScreenMessage());
        editTextLockScreenMessage.addTextChangedListener(this);
        editTextPin.setText(settings.getPin());
        editTextPin.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);

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
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            String cName = phones.getString(phones.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            if(!cNumber.startsWith("0")) {
                                whiteList.add(new Contact(cName, cNumber));
                                ServiceHandler.restartService(this);
                                reloadViews();
                                updateViews();
                            }else{
                                Toast toast = Toast.makeText(this,"Please add Landcode to the number.", Toast.LENGTH_LONG);
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
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (item.getTitle() == "Delete") {
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
        if(buttonView == checkBoxDeviceWipe){
            settings.setWipeEnabled(isChecked);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s == editTextLockScreenMessage.getText()){
            settings.setLockScreenMessage(s.toString());
        }else if(s == editTextPin.getText()){
            settings.setPin(s.toString());
        }
    }
}
