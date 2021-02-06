package de.nulide.findmydevice.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class LogData extends HashMap<Integer, Object>{

    public void add(String text) {
        this.put(this.size(), text);
        IO.write(JSONFactory.convertLogData(this), IO.logFileName);
    }

    public List getList(){
        ArrayList<String> list = new ArrayList<String>();
        for(Integer key : keySet()) {
            list.add((String) get(key));
        }
        return list;
    }

}
