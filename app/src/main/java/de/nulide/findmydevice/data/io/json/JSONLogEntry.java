package de.nulide.findmydevice.data.io.json;

public class JSONLogEntry {
    private long time;
    private String text;

    public JSONLogEntry(){

    }

    public JSONLogEntry(long time, String text){
        this.time = time;
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
