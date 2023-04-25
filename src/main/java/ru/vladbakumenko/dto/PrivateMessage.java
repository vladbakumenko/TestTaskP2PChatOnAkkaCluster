package ru.vladbakumenko.dto;

import java.io.Serializable;

public class PrivateMessage extends GroupMessage implements Serializable {
    private String recipientName;

    public PrivateMessage(String senderName, String value, String recipientName) {
        super(senderName, value);
        this.recipientName = recipientName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
