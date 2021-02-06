package de.nulide.findmydevice.data.io;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.json.JSONContact;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;

public class JSONFactory {

    public static Settings convertJSONSettings(JSONMap jsonSettings) {
        Settings settings = new Settings();
        settings.putAll(jsonSettings);
        return settings;
    }

    public static JSONMap convertSettings(Settings settings) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(settings);
        return jsonSettings;
    }

    public static Contact convertJSONContact(JSONContact jsonContact) {
        return new Contact(jsonContact.getName(), jsonContact.getNumber());
    }

    public static JSONContact convertContact(Contact contact) {
        JSONContact jsonContact = new JSONContact();
        jsonContact.setName(contact.getName());
        jsonContact.setNumber(contact.getNumber());
        return jsonContact;
    }

    public static WhiteList convertJSONWhiteList(JSONWhiteList jsonWhiteList) {
        WhiteList whiteList = new WhiteList();
        for (JSONContact jsonContact : jsonWhiteList) {
            whiteList.superAdd(convertJSONContact(jsonContact));
        }
        return whiteList;
    }

    public static JSONWhiteList convertWhiteList(WhiteList whiteList) {
        JSONWhiteList jsonWhiteList = new JSONWhiteList();
        for (Contact c : whiteList) {
            jsonWhiteList.add(convertContact(c));
        }
        return jsonWhiteList;
    }

    public static ConfigSMSRec convertJSONConfig(JSONMap jsonSettings) {
        ConfigSMSRec temp = new ConfigSMSRec();
        temp.putAll(jsonSettings);
        return temp;
    }

    public static JSONMap convertTempConfigSMSRec(ConfigSMSRec configSMSRec) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(configSMSRec);
        return jsonSettings;
    }

    public static LogData convertJSONLog(JSONMap jsonSettings) {
        LogData temp = new LogData();
        temp.putAll(jsonSettings);
        return temp;
    }

    public static JSONMap convertLogData(LogData configSMSRec) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(configSMSRec);
        return jsonSettings;
    }
}
