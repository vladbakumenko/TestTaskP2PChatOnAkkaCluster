package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ru.vladbakumenko.actors.MessageSender;
import ru.vladbakumenko.actors.SimpleClusterListener;
import ru.vladbakumenko.model.ChatMessage;

public class App {

    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("ClusterSystem");

        ActorRef simpleClusterListener = system.actorOf(Props.create(SimpleClusterListener.class));
        ActorRef listener = system.actorOf(Props.create(MessageSender.class), "listener");

        ChatMessage message = new ChatMessage("Hello from port 2551!" + "\n");

        simpleClusterListener.tell(message, system.actorSelection(simpleClusterListener.path()).anchor());

    }
}
