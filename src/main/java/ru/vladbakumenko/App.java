package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.MessageListener;
import ru.vladbakumenko.actors.UserActor;
import ru.vladbakumenko.model.ChatMessage;

public class App extends Application {

    private final String username = "user-" + System.currentTimeMillis();
    private ActorSystem system;
    private ActorRef clusterListener;
    private ActorRef messageListener;
    private ActorRef userActor;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //console
        TextArea logArea = new TextArea();
        logArea.setEditable(true);

        //host select
        TextField hostField = new TextField();
        hostField.setPromptText("Введи адрес хоста");
        final String[] host = {""};

        //port select
        TextField portField = new TextField();
        portField.setPromptText("Введи номер порта");
        final String[] port = {""};

        //button for connect
        Button button = new Button("Подключиться");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                host[0] = hostField.getText();
                port[0] = portField.getText();

                if(host[0].isBlank()) {
                    logArea.appendText("Не введён адрес хоста" + "\n");
                }
                if (port[0].isBlank()) {
                    logArea.appendText("Не введён адреспорта" + "\n");
                }

                Address address = new Address("akka", "ClusterSystem", host[0], Integer.parseInt(port[0]));

                clusterListener.tell(address, clusterListener);
            }
        });

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

        BorderPane pane1 = new BorderPane();
        pane1.setTop(hostField);
        pane1.setCenter(portField);
        pane1.setBottom(button);

        BorderPane pane2 = new BorderPane();
        pane2.setTop(pane1);
        pane2.setCenter(logArea);
        pane2.setBottom(messageField);

        stage.setScene(new Scene(pane2, 400, 300));
        stage.show();

        system = ActorSystem.create("ClusterSystem");
        clusterListener = system.actorOf(Props.create(ClusterListener.class));
        messageListener = system.actorOf(MessageListener.getProps(logArea), "listener");
        userActor = system.actorOf(UserActor.props(username));
    }
}
