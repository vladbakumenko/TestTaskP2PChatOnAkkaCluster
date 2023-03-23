package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.cluster.Member;
import ru.vladbakumenko.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageSender extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChatMessage.class,
                        message -> {
                            System.out.println(message.getValue());
                        }
                )
                .build();
    }
}