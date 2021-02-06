package de.nulide.findmydevice.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class LogData extends HashMap<Integer, Object>{

    public void add(String text) {
        this.put(this.size(), text);
        IO.write(JSONFactory.convertLogData(this), IO.logFileName);
    }

    public List getList(){
        LinkedList<String> list = new LinkedList();
        for(Integer key : keySet()) {
            list.add((String) get(key));
        }
        return list;
    }

}
