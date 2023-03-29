package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.MessageListener;
import ru.vladbakumenko.model.ChatMessage;

public class App extends Application {

    private ActorSystem system;
    private ActorRef clusterListener;
    private ActorRef messageListener;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //console
        TextArea logArea = new TextArea();
        logArea.setEditable(true);

        //message
        TextField messageField = new TextField();
        messageField.setPromptText("Ваше сообщение");

        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String text = messageField.getText();
                    ChatMessage message = new ChatMessage(text);
                    clusterListener.tell(message, messageListener);
                    messageField.setText("");
                }
            }
        });

        BorderPane pane = new BorderPane();
        pane.setCenter(logArea);
        pane.setBottom(messageField);

        stage.setScene(new Scene(pane, 400, 300));
        stage.show();

        system = ActorSystem.create("ClusterSystem");
        clusterListener = system.actorOf(Props.create(ClusterListener.class));
        messageListener = system.actorOf(MessageListener.getProps(logArea), "listener");
    }
}
