package de.nulide.findmydevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ListAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Whitelist;
import de.nulide.findmydevice.ui.MainPageViewAdapter;
import de.nulide.findmydevice.utils.SMS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout tabLayout;
    private TabItem tabItemInfo;
    private TabItem tabItemWhitelist;
    private ViewPager viewPager;

    private ListView listWhiteList;
    private Button buttonAddContact;

    private Whitelist whiteList;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SMS rec = new SMS();
        registerReceiver(rec, new IntentFilter(SMS.SMS_RECEIVED));
        /*

        Log.d("SMS", "SMS ready");

        GPS gps = new GPS(this);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Looper m = Looper.myLooper();
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, gps, m);
        lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, gps, m);
        Log.d("GPS", String.valueOf(gps.getLastBestLocation()));

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
        String networkOperator = tm.getNetworkOperator();
        Log.d("gsm", location.getCid() + "/"+ location.getLac());
        Log.d("gsm-2", networkOperator.substring(0,3)+ "/"+networkOperator.substring(3));*/

        whiteList = new Whitelist();

        tabLayout = findViewById(R.id.tablayout);
        tabItemInfo = findViewById(R.id.tabItemInfo);
        tabItemWhitelist = findViewById(R.id.tabItemWhitelist);
        viewPager = findViewById(R.id.viewPager);
        MainPageViewAdapter mPageViewAdapter = new MainPageViewAdapter(this);
        viewPager.setAdapter(mPageViewAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


    }

    public void reloadViews(){
        listWhiteList = findViewById(R.id.list_whitelist);
        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonAddContact.setOnClickListener(this);
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
                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            System.out.println("number is:"+cNumber);
                            whiteList.addContact(new Contact("?",cNumber));
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                }
                break;
        }
    }
}
