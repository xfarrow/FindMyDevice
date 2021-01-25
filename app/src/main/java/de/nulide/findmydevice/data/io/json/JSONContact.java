package de.nulide.findmydevice.data.io.json;

public class JSONContact {

    private String name;
    private String number;

    public JSONContact() {
        name = "";
        number = "";
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
}
