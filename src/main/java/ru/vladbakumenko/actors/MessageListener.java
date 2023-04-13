package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import ru.vladbakumenko.model.ChatMessage;

import java.util.List;
import java.util.Queue;

public class MessageListener extends AbstractActor {

    private List<ChatMessage> list;

    public MessageListener(List<ChatMessage> list) {
        this.list = list;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChatMessage.class,
                        message -> {
                            getContext().getSystem().log().info(message.getValue());
                            list.add(message);
                        }
                )
                .build();
    }

    public static Props getProps(List<ChatMessage> list) {
        return Props.create(MessageListener.class, list);
    }
}
