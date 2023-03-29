package ru.vladbakumenko.model;

import javafx.event.ActionEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import scala.Serializable;


@Data
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private String value;
}
