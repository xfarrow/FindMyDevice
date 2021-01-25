package de.nulide.findmydevice.data;

import android.content.Context;

import java.util.LinkedList;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;

public class WhiteList extends LinkedList<Contact> {

    public WhiteList() {

    }

    public boolean superAdd(Contact c){
        return super.add(c);
    }

    @Override
    public Contact remove(int index){
        Contact c = get(index);
        super.remove(index);
        IO.write(this, IO.whiteListFileName);
        return c;
    }

    @Override
    public boolean add(Contact c) {
        c.setNumber(c.getNumber().replace(" ", ""));
        super.add(c);
        IO.write(this, IO.whiteListFileName);
        return true;
    }
}
