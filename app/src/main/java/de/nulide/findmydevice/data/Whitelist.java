package de.nulide.findmydevice.data;

import java.util.LinkedList;
import java.util.List;

public class Whitelist {
    private List<Integer> phoneNumbers;

    public Whitelist() {
        phoneNumbers = new LinkedList<>();
    }

    public List<Integer> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void addPhoneNumber(int number){
        phoneNumbers.add(number);
    }
}
