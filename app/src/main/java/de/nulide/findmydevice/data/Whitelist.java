package de.nulide.findmydevice.data;

import java.util.LinkedList;
import java.util.List;

public class Whitelist {
    private List<Contact> contacts;

    public Whitelist() {
        contacts = new LinkedList<>();
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }
}
