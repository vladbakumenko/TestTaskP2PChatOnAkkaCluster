package ru.vladbakumenko.model;

import akka.actor.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
public class Connection implements Serializable {
    private String name;
    private Address connectionAddress;
    private Address recipientAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(name, that.name) && Objects.equals(connectionAddress, that.connectionAddress) && Objects.equals(recipientAddress, that.recipientAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, connectionAddress, recipientAddress);
    }
}
