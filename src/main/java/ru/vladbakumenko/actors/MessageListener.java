package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.scene.control.TextArea;
import ru.vladbakumenko.model.ChatMessage;

import java.util.LinkedList;
import java.util.Queue;

public class MessageListener extends AbstractActor {

    private Queue<ChatMessage> queue;

    public MessageListener(Queue<ChatMessage> queue) {
        this.queue = queue;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChatMessage.class,
                        message -> {
                            getContext().getSystem().log().info(message.getValue());
                            queue.add(message);
                        }
                )
                .build();
    }

    public static Props getProps(Queue<ChatMessage> queue) {
        return Props.create(MessageListener.class, queue);
    }
}
