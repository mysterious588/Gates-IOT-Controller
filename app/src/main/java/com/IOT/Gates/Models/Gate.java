package com.IOT.Gates.Models;

public class Gate {
    private String name, PIN, state, owners[];

    public Gate() {}

    public Gate(String name, String PIN) {
        this.name = name;
        this.PIN = PIN;
    }


    public Gate(String name, String PIN, String state) {
        this.name = name;
        this.PIN = PIN;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String[] getOwners() {
        return owners;
    }

    public void setOwners(String[] owners) {
        this.owners = owners;
    }
}
