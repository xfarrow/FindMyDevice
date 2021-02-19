package de.nulide.findmydevice.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import de.nulide.findmydevice.receiver.DeviceAdminReceiver;

public class Permission {

    private static final int PERM_SMS_ID = 61341;
    private static final int PERM_GPS_ID = 61342;
    private static final int PERM_CONTACT_ID = 61343;

    public static boolean GPS = false;
    public static boolean DEVICE_ADMIN = false;
    public static boolean DND = false;
    public static boolean OVERLAY = false;
    public static boolean WRITE_SECURE_SETTINGS = false;
    public static boolean CORE = false;

    public static void initValues(Context context) {
        GPS = checkGPSPermission(context);
        DEVICE_ADMIN = checkDeviceAdminPermission(context);
        WRITE_SECURE_SETTINGS = checkWriteSecurePermission(context);
        OVERLAY = checkOverlayPermission(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DND = checkDNDPermission(context);
        }
        if (checkContactsPermission(context) && checkSMSPermission(context)) {
            CORE = true;
        }
    }

    public static void requestSMSPermission(Activity activity) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, PERM_SMS_ID);
        }else{
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, PERM_SMS_ID);
        }
    }

    public static void requestGPSPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERM_GPS_ID);
    }

    public static void requestContactPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, PERM_CONTACT_ID);
    }

    public static void requestOverlayPermission(Activity activity) {
        if (!checkOverlayPermission(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    public static void requestDNDPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkDNDPermission(activity)) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                activity.startActivity(intent);
            }
        }
    }

    public static void requestDeviceAdminPermission(Activity activity) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(activity, DeviceAdminReceiver.class));
        activity.startActivity(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkDNDPermission(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return mNotificationManager.isNotificationPolicyAccessGranted();
    }

    public static boolean checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static boolean checkWriteSecurePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkSMSPermission(Context context) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }else{
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean checkGPSPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkDeviceAdminPermission(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return devicePolicyManager.isAdminActive(new ComponentName(context, DeviceAdminReceiver.class));
    }

}
