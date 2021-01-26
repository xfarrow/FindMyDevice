package de.nulide.findmydevice.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.collection.ArrayMap;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class Network {

    public static List<ScanResult> getWifiNetworks(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();

        List<ScanResult> results = wifiManager.getScanResults();
        return results;
    }

    public static Map<String, String> getAllIP() {
        ArrayMap<String, String> ip = new ArrayMap<>();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ip.put(intf.getDisplayName(), inetAddress.getHostAddress());
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
