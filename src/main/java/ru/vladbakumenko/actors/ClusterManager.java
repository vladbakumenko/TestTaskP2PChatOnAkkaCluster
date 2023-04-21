package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.application.Platform;
import ru.vladbakumenko.App;
import ru.vladbakumenko.model.ChatMembers;
import ru.vladbakumenko.model.GroupMessage;
import ru.vladbakumenko.model.Connection;
import ru.vladbakumenko.model.PrivateMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusterManager extends AbstractActor {

    private List<GroupMessage> groupMessages;
    private List<PrivateMessage> privateMessages;
    private List<String> members;
    private Set<String> currentMembers = new HashSet<>();

    public ClusterManager(List<GroupMessage> groupMessages, List<PrivateMessage> privateMessages, List<String> members) {
        this.groupMessages = groupMessages;
        this.privateMessages = privateMessages;
        this.members = members;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrivateMessage.class,
                        message -> {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    privateMessages.add(message);
                                }
                            });
                        }
                )
                .match(GroupMessage.class,
                        message -> {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    groupMessages.add(message);
                                }
                            });
                        }
                )
                .match(ChatMembers.class,
                        message -> {
                            currentMembers.addAll(message.getMembers().stream()
                                    .map(Connection::getName).toList());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    members.clear();
                                    members.add(App.GROUP_CHAT_NAME);
                                    members.addAll(currentMembers);
                                }
                            });
                        })
                .build();
    }

    public static Props getProps(List<GroupMessage> groupMessages, List<PrivateMessage> privateMessages, List<String> members) {
        return Props.create(ClusterManager.class, groupMessages, privateMessages, members);
    }
}
