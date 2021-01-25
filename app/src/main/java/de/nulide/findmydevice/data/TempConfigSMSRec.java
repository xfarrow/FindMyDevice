package de.nulide.findmydevice.data;

import java.util.HashMap;
import java.util.Timer;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.utils.SaveTimerTask;

public class TempConfigSMSRec extends HashMap<String, Object> {

    public static final String CONF_LAST_USAGE = "CONF_LAST_USAGE";
    public static final String CONF_TEMP_WHITELISTED_CONTACT = "CONF_TEMP_WHITELISTED_CONTACT";
    public static final String CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE = "CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE";

    public <T> void set(String key, T value){
        super.put(key, value);
        IO.write(JSONFactory.convertTempConfigSMSRec(this),IO.SMSReceiverTempData);
    }

    public Object get(String key){
        if(super.containsKey(key)){
            return super.get(key);
        }
        return null;
    }

}
