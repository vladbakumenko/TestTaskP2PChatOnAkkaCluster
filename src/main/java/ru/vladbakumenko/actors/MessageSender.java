package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.cluster.Member;
import ru.vladbakumenko.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageSender extends AbstractActor {

    List<Member> members = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Member.class,
                        message -> {
                            members.add(message);
                        })
                .match(ChatMessage.class,
                        message -> {
                            for (Member member : members) {
                                getContext().actorSelection(member.toString()).tell(message.getValue(), getSelf());
                            }
                        }
                )
                .build();
    }
}