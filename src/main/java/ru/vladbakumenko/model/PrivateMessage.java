package ru.vladbakumenko.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivateMessage {
    private String userName;
    private String recipientName;
    private String value;
}
