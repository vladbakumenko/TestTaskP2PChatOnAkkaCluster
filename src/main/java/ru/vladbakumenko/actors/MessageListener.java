package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.scene.control.TextArea;
import ru.vladbakumenko.model.ChatMessage;

public class MessageListener extends AbstractActor {

    private final TextArea textArea;

    public MessageListener(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChatMessage.class,
                        message -> {
                            getContext().getSystem().log().info(message.getValue());
                            textArea.appendText(message.getValue() + "\n");
                        }
                )
                .build();
    }

    public static Props getProps(TextArea textArea) {
        return Props.create(MessageListener.class, textArea);
    }

}
