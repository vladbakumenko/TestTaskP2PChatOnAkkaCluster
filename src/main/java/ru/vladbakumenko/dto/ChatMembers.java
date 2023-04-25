package ru.vladbakumenko.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class ChatMembers implements Serializable {
    private Set<Connection> members = new HashSet<>();
}
