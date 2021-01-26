package de.nulide.findmydevice.data;

import java.util.HashMap;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class ConfigSMSRec extends HashMap<Integer, Object> {

    public static final int CONF_LAST_USAGE = 0;
    public static final int CONF_TEMP_WHITELISTED_CONTACT = 1;
    public static final int CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE = 2;

    public <T> void set(int key, T value) {
        super.put(key, value);
        IO.write(JSONFactory.convertTempConfigSMSRec(this), IO.SMSReceiverTempData);
    }

    public Object get(int key) {
        if (super.containsKey(key)) {
            return super.get(key);
        }
        return null;
    }

}
