package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import ru.vladbakumenko.model.ChatMembers;
import ru.vladbakumenko.model.ChatMessage;
import ru.vladbakumenko.model.Connection;

import java.util.List;

public class ClusterManager extends AbstractActor {

    private List<ChatMessage> messages;
    private List<String> members;

    public ClusterManager(List<ChatMessage> list, List<String> members) {
        this.messages = list;
        this.members = members;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ChatMessage.class,
                        message -> {
                            getContext().getSystem().log().info(message.getValue());
                            messages.add(message);
                        }
                )
                .match(ChatMembers.class,
                        message -> {
                            members.addAll(message.getMembers().stream()
                                    .map(Connection::getName).toList());
                        })
                .build();
    }

    public static Props getProps(List<ChatMessage> messages, List<String> members) {
        return Props.create(ClusterManager.class, messages, members);
    }
}
