package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class UserActor extends AbstractActor {
    private final String username;

    public UserActor(String username) {
        this.username = username;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    // Обработка сообщения
                })
                .build();
    }

    public static Props props(String username) {
        return Props.create(UserActor.class, username);
    }
}
