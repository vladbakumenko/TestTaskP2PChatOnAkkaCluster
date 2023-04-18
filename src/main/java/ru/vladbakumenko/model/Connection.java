package ru.vladbakumenko.model;

import akka.actor.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Connection implements Serializable {
    private String name;
    private Address address;
}
