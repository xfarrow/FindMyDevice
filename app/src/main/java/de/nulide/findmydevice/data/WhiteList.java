package de.nulide.findmydevice.data;

import android.content.Context;

import java.util.LinkedList;

import de.nulide.findmydevice.data.io.IO;

public class WhiteList extends LinkedList<Contact> {

    Context context;

    private final static String FILE_NAME = "whitelist.dat";

    public WhiteList(){

    }

    public WhiteList(Context context) {
        this.context = context;
    }

    @Override
    public boolean add(Contact c){
        super.add(c);
        IO.writeWhiteList(this);
        return true;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
