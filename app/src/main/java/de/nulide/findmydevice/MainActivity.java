package de.nulide.findmydevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.service.SMSForegroundService;
import de.nulide.findmydevice.service.SMSService;
import de.nulide.findmydevice.ui.MainPageViewAdapter;
import de.nulide.findmydevice.ui.WhiteListViewAdapter;
import de.nulide.findmydevice.utils.Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TextView textViewRunningService;
    private TextView textViewWhiteListCount;



    private ListView listWhiteList;
    private WhiteListViewAdapter listWhiteListAdapter;
    private Button buttonAddContact;

    private WhiteList whiteList;

    private Intent backgroundService;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!Permission.checkAll(this)) {
            Intent myIntent = new Intent(this, PermissionsActivity.class);
            startActivity(myIntent);
        }

        IO.context = this;
        whiteList = IO.readWhiteList();

        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewPager);
        MainPageViewAdapter mPageViewAdapter = new MainPageViewAdapter(this);
        viewPager.setAdapter(mPageViewAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Info");
        tabLayout.getTabAt(1).setText("WhiteList");

        reloadViews();
        updateViews();

        if(!SMSService.isRunning(this)){
            backgroundService = new Intent(getApplicationContext(), SMSService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent foregroundService = new Intent(this, SMSForegroundService.class);
                startForegroundService(foregroundService);
            }else {
                Intent backgroundService = new Intent(this, SMSService.class);
                startService(backgroundService);
            }        }
    }

    public void reloadViews(){
        textViewRunningService = findViewById(R.id.textViewServiceRunning);
        textViewWhiteListCount = findViewById(R.id.textViewWhiteListCount);

        listWhiteList = findViewById(R.id.list_whitelist);
        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonAddContact.setOnClickListener(this);
    }

    public void updateViews(){
        if(SMSService.isRunning(this)){
            textViewRunningService.setText("running");
            textViewRunningService.setTextColor(Color.GREEN);
        }else{
            textViewRunningService.setText("not running");
            textViewRunningService.setTextColor(Color.RED);
        }
        textViewWhiteListCount.setText(""+whiteList.size());
        listWhiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(listWhiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);
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
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String name =c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            String cName = phones.getString(phones.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            Log.d("main", cName);
                            whiteList.add(new Contact(cName,cNumber));
                            stopService(backgroundService);
                            startService(backgroundService);
                            reloadViews();
                            updateViews();
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
}
