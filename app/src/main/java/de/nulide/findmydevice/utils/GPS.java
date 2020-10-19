package de.nulide.findmydevice.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import de.nulide.findmydevice.receiver.SMSReceiver;

public class GPS implements LocationListener {

    static final int WAIT_TIME = 1000 * 5;
    private Location currentBestLocation = null;
    private Context context;
    private LocationManager locationManager;
    private String sender;

    @SuppressLint("MissingPermission")
    public GPS(Context context, String sender){
        this.context = context;
        this.sender = sender;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Looper m = Looper.myLooper();
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, m);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, m);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, currentBestLocation)) {
            currentBestLocation = location;
        }
        if (currentBestLocation == null) {
            currentBestLocation = location;
        }
        String Providor = getLastBestLocation().getProvider();
        String Latitude = getLastBestLocation().getLatitude()+ "";
        String Longitude = getLastBestLocation().getLongitude() + "";
        SMS.sendMessage(sender, Providor + ": " + Latitude + " " + Longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLastBestLocation() {
        @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > WAIT_TIME;
        boolean isSignificantlyOlder = timeDelta < -WAIT_TIME;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        String provider1 = location.getProvider();
        String provider2 = currentBestLocation.getProvider();
        if (provider1 == null) {
            return provider2 == null;
        }
        boolean isFromSameProvider = provider1.equals(provider2);

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    public GsmCellLocation sendGSMCellLocation(){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getNetworkOperator();
        @SuppressLint("MissingPermission") GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
        if (!TextUtils.isEmpty(operator)) {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            SMS.sendMessage(sender, "GSM-Cell-Data:\nMCC: "+ mcc +"\nMNC: " + mnc+ "\nCID: " + location.getCid()+ "\nLAC: " + location.getLac() + "\nSearch with: http://www.cell2gps.com/");
        }
        return location;
    }
}
