package de.nulide.findmydevice.data.io;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.LogData;
import de.nulide.findmydevice.data.LogEntry;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.json.JSONContact;
import de.nulide.findmydevice.data.io.json.JSONLog;
import de.nulide.findmydevice.data.io.json.JSONLogEntry;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;

public class JSONFactory {

    public static Settings convertJSONSettings(JSONMap jsonSettings) {
        Settings settings = new Settings();
        if(jsonSettings != null) {
            settings.putAll(jsonSettings);
        }
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
        if(jsonWhiteList != null) {
            for (JSONContact jsonContact : jsonWhiteList) {
                whiteList.superAdd(convertJSONContact(jsonContact));
            }
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
        if(jsonSettings != null) {
            temp.putAll(jsonSettings);
        }
        return temp;
    }

    public static JSONMap convertTempConfigSMSRec(ConfigSMSRec configSMSRec) {
        JSONMap jsonSettings = new JSONMap();
        jsonSettings.putAll(configSMSRec);
        return jsonSettings;
    }

    public static LogEntry convertJSONLogEntry(JSONLogEntry jsonLogEntry){
        return new LogEntry(jsonLogEntry.getTime(), jsonLogEntry.getText());
    }

    public static JSONLogEntry convertLogEntry(LogEntry logEntry){
        return new JSONLogEntry(logEntry.getTime(), logEntry.getText());
    }

    public static LogData convertJSONLog(JSONLog jsonLog) {
        LogData temp = new LogData();
        if(jsonLog != null) {
            for(JSONLogEntry jsonLogEntry : jsonLog){
                temp.add(convertJSONLogEntry(jsonLogEntry));
            }
        }
        return temp;
    }

    public static JSONLog convertLogData(LogData logData) {
        JSONLog jsonLog = new JSONLog();
        for(LogEntry logEntry : logData){
            jsonLog.add(convertLogEntry(logEntry));
        }
        return jsonLog;
    }
}
