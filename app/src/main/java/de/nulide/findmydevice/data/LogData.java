package de.nulide.findmydevice.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;

public class LogData extends LinkedList<LogEntry> {

    public void add(long time, String text) {
        this.add(new LogEntry(time, text));
        IO.write(JSONFactory.convertLogData(this), IO.logFileName);
    }

    public List<String> getDates(){
        List<String> dates = new LinkedList<>();
        for(LogEntry logEntry : this){
            Date date = new Date(logEntry.getTime());
            dates.add(date.toString());
        }
        return dates;
    }

}
