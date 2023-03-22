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
        ActorRef actorRef = system.actorOf(Props.create(SimpleClusterListener.class));
        ChatMessage message = new ChatMessage("Hello from system!" + "\n");
        actorRef.tell(message, system.actorOf(Props.create(MessageSender.class)));

    }
}
