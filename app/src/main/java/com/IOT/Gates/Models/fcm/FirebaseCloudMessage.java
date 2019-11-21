package com.IOT.Gates.Models.fcm;

public class FirebaseCloudMessage {
    private String to;
    private Data data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public FirebaseCloudMessage(){}

    public FirebaseCloudMessage(String to, Data data) {
        this.to = to;
        this.data = data;
    }

    @Override
    public String toString() {
        return "FirebaseCloudMessage{" + "to='" + to + '\'' + ", data=" + data + '}';
    }
}
