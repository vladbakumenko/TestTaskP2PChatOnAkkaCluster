package ru.vladbakumenko.dto;

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
    private Address userAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(name, that.name) && Objects.equals(connectionAddress, that.connectionAddress) && Objects.equals(userAddress, that.userAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, connectionAddress, userAddress);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "name='" + name + '\'' +
                ", connectionAddress=" + connectionAddress +
                ", userAddress=" + userAddress +
                '}';
    }
}
