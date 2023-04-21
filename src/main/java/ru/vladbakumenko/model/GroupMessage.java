package ru.vladbakumenko.model;

import scala.Serializable;

public class GroupMessage implements Serializable {
    private String senderName;
    private String value;

    public GroupMessage(String senderName, String value) {
        this.senderName = senderName;
        this.value = value;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
