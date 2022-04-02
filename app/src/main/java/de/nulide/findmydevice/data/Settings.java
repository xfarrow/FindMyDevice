package de.nulide.findmydevice.data;


import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Timer;

import de.nulide.findmydevice.data.io.OldKeyIO;
import de.nulide.findmydevice.logic.command.helper.Ringer;
import de.nulide.findmydevice.tasks.SaveTimerTask;
import de.nulide.findmydevice.utils.CypherUtils;

public class Settings extends HashMap<Integer, Object> {

    public static final int newestIntroductionVersion = 4;

    public static final int settingsVersion = 2;

    public static final int SET_WIPE_ENABLED = 0;
    public static final int SET_ACCESS_VIA_PIN = 1;
    public static final int SET_LOCKSCREEN_MESSAGE = 2;
    public static final int SET_PIN = 3;
    public static final int SET_FMD_COMMAND = 4;
    public static final int SET_OPENCELLID_API_KEY = 5;
    public static final int SET_INTRODUCTION_VERSION = 6;
    public static final int SET_RINGER_TONE = 7;
    public static final int SET_SET_VERSION = 8;

    public static final int SET_FMDSERVER_UPLOAD_SERVICE = 101;
    public static final int SET_FMDSERVER_URL = 102;
    public static final int SET_FMDSERVER_UPDATE_TIME = 103;
    public static final int SET_FMDSERVER_ID = 104;
    public static final int SET_FMDSERVER_PASSWORD_SET = 105;
    public static final int SET_FMDSERVER_LOCATION_TYPE = 106; // 0=GPS, 1=CELL, 2=ALL
    public static final int SET_FMDSERVER_AUTO_UPLOAD = 107;
    public static final int SET_FMD_CRYPT_PUBKEY = 108;
    public static final int SET_FMD_CRYPT_PRIVKEY = 109;
    public static final int SET_FMD_CRYPT_HPW = 110;

    public static final int SET_FIRST_TIME_WHITELIST = 301;
    public static final int SET_FIRST_TIME_CONTACT_ADDED = 302;
    public static final int SET_FIRST_TIME_FMD_SERVER = 303;

    public static final int SET_APP_CRASHED_LOG_ENTRY = 401;
    public static final int SET_FMDSMS_COUNTER = 402;

    public static final int SET_GPS_STATE = 501;         // 0=GPS is off 1=GPS is on 2=GPS is turned on by FMD
    public static final int SET_LAST_KNOWN_LOCATION_LAT = 502;
    public static final int SET_LAST_KNOWN_LOCATION_LON = 503;
    public static final int SET_LAST_KNOWN_LOCATION_TIME = 504;



    private Timer afterChangeTimer;

    public Settings() {
    }

    public <T> void set(int key, T value) {
        super.put(key, value);
        write(false);
    }

    public <T> void setNow(int key, T value) {
        super.put(key, value);
        write(true);
    }

    public Object get(int key) {
        if (super.containsKey(key)) {
            return super.get(key);
        } else {
            switch (key) {
                case SET_WIPE_ENABLED:
                case SET_ACCESS_VIA_PIN:
                case SET_FIRST_TIME_WHITELIST:
                case SET_FIRST_TIME_CONTACT_ADDED:
                case SET_FIRST_TIME_FMD_SERVER:
                case SET_FMDSERVER_UPLOAD_SERVICE:
                case SET_FMDSERVER_PASSWORD_SET:
                    return false;
                case SET_FMDSERVER_AUTO_UPLOAD:
                    return true;
                case SET_FMD_COMMAND:
                    return "fmd";
                case SET_FMDSERVER_UPDATE_TIME:
                    return 60;
                case SET_INTRODUCTION_VERSION:
                case SET_FMDSMS_COUNTER:
                case SET_FMDSERVER_LOCATION_TYPE:
                case SET_SET_VERSION:
                    return 0;
                case SET_RINGER_TONE:
                    return Ringer.getDefaultRingtoneAsString();
                case SET_PIN:
                case SET_FMDSERVER_ID:
                case SET_LAST_KNOWN_LOCATION_LAT:
                case SET_LAST_KNOWN_LOCATION_LON:
                case SET_FMD_CRYPT_HPW:
                case SET_FMD_CRYPT_PRIVKEY:
                case SET_FMD_CRYPT_PUBKEY:
                    return "";
                case SET_GPS_STATE:
                    return 1;
                case SET_APP_CRASHED_LOG_ENTRY:
                case SET_LAST_KNOWN_LOCATION_TIME:
                    return -1;
                case SET_FMDSERVER_URL:
                    return "https://fmd.nulide.de:1008";
            }
        }
        return "";
    }


    public boolean isIntroductionPassed() {
        return newestIntroductionVersion == (Integer) get(SET_INTRODUCTION_VERSION);
    }

    public void setIntroductionPassed() {
        set(SET_INTRODUCTION_VERSION, newestIntroductionVersion);
        write(true);
    }

    public Keys getKeys() {
        if (get(SET_FMD_CRYPT_PUBKEY).equals("")) {
            return null;
        } else {

            EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(CypherUtils.decodeBase64((String) get(SET_FMD_CRYPT_PUBKEY)));
            PublicKey publicKey = null;
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(pubKeySpec);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            return new Keys(publicKey, (String) get(SET_FMD_CRYPT_PRIVKEY));


        }
    }

    public void setKeys(Keys keys){
        set(SET_FMD_CRYPT_PRIVKEY, keys.getEncryptedPrivateKey());
        set(SET_FMD_CRYPT_PUBKEY, CypherUtils.encodeBase64(keys.getPublicKey().getEncoded()));
    }

    public void updateSettings() {
        if (((Integer) get(SET_SET_VERSION)) < settingsVersion && ((Integer) get(SET_INTRODUCTION_VERSION)) > 0){
            if (!((String)get(SET_FMDSERVER_ID)).isEmpty()) {
                Keys keys = OldKeyIO.readKeys();
                String HashedPW = OldKeyIO.readHashedPW();
                setKeys(keys);
                set(SET_FMD_CRYPT_HPW, HashedPW);
                set(SET_SET_VERSION, settingsVersion);
            }
        }
    }

    private void write(boolean write_now) {
        SaveTimerTask saverTask = new SaveTimerTask(this);
        if (write_now) {
            saverTask.write();
        } else {
            if (afterChangeTimer != null) {
                afterChangeTimer.cancel();
            }
        }
        afterChangeTimer = new Timer();
        afterChangeTimer.schedule(saverTask, 300);
    }
}
