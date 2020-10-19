package de.nulide.findmydevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import de.nulide.findmydevice.ui.MainPageViewAdapter;
import de.nulide.findmydevice.utils.GPS;
import de.nulide.findmydevice.utils.SMS;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TabItem tabItemInfo;
    private TabItem tabItemWhitelist;
    private ViewPager viewPager;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SMS rec = new SMS();
        registerReceiver(rec, new IntentFilter(SMS.SMS_RECEIVED));

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
        Log.d("gsm-2", networkOperator.substring(0,3)+ "/"+networkOperator.substring(3));


        tabLayout = findViewById(R.id.tablayout);
        tabItemInfo = findViewById(R.id.tabItemInfo);
        tabItemWhitelist = findViewById(R.id.tabItemWhitelist);
        viewPager = findViewById(R.id.viewPager);
        MainPageViewAdapter mPageViewAdapter = new MainPageViewAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(mPageViewAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }
}
