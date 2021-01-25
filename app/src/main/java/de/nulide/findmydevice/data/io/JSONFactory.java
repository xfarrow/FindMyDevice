package de.nulide.findmydevice.data.io;

import java.util.Map;

import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.json.JSONContact;
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;

public class JSONFactory {

    public static Settings convertJSONSettings(JSONSettings jsonSettings) {
        Settings settings = new Settings();
        settings.putAll(jsonSettings);
        return settings;
    }

    public static JSONSettings convertSettings(Settings settings) {
        JSONSettings jsonSettings = new JSONSettings();
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
}
