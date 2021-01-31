package de.nulide.findmydevice.data;

import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;

public class Contact {

    private String name;
    private String number;

    public Contact() {
    }

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean equals(@Nullable Contact toCheck) {
        return PhoneNumberUtils.compare(number, toCheck.number);
    }
}
