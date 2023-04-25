package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.application.Platform;
import ru.vladbakumenko.App;
import ru.vladbakumenko.controller.ChatController;
import ru.vladbakumenko.model.ChatMembers;
import ru.vladbakumenko.model.GroupMessage;
import ru.vladbakumenko.model.Connection;
import ru.vladbakumenko.model.PrivateMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusterManager extends AbstractActor {

    private ChatController controller;
    private Set<String> currentMembers = new HashSet<>();

    public ClusterManager(ChatController controller) {
        this.controller = controller;
    }

    public static Props getProps(ChatController controller) {
        return Props.create(ClusterManager.class, controller);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrivateMessage.class,
                        message -> {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    controller.getPrivateMessages().add(message);
                                }
                            });
                        }
                )
                .match(GroupMessage.class,
                        message -> {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    controller.getGroupMessages().add(message);
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
                                    controller.getMembers().clear();
                                    controller.getMembers().add(App.GROUP_CHAT_NAME);
                                    controller.getMembers().addAll(currentMembers);
                                }
                            });
                        })
                .build();
    }
}
