package de.nulide.findmydevice.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission {

    public static final int PERM_SMS_ID = 61341;
    public static final int PERM_GPS_ID = 61342;
    public static final int PERM_CONTACT_ID = 61343;
    public static final int PERM_DND_ID = 61344;

    public static final String PERM_SMS = Manifest.permission.SEND_SMS;
    public static final String PERM_GPS = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERM_BG_GPS = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    public static final String PERM_CONTACT = Manifest.permission.READ_CONTACTS;
    public static final String PERM_DND = Manifest.permission.ACCESS_NOTIFICATION_POLICY;


    public static boolean checkAll(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkPermission(activity, PERM_SMS) && checkPermission(activity, PERM_CONTACT) &&
                checkPermission(activity, PERM_DND) && checkDNDPermission(activity)){
                return true;
            }
        }else{
            if(checkPermission(activity, PERM_SMS) && checkPermission(activity, PERM_CONTACT) &&
                    checkPermission(activity, PERM_GPS)){
                return true;
            }
        }
        return false;
    }

    public static void requestDNDPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkDNDPermission(activity)) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                activity.startActivity(intent);
            }
        }
    }


    public static void requestPermission(Activity activity, String permission, int permId) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if(permId == PERM_GPS_ID){
                ActivityCompat.requestPermissions(activity, new String[]{permission,PERM_BG_GPS}, permId);
            }else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permId);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkDNDPermission(Activity activity){
        NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager.isNotificationPolicyAccessGranted()){
            return true;
        }
        return false;
    }

    public static boolean checkPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

}
