package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.MessageListener;
import ru.vladbakumenko.model.ChatMessage;

public class App extends Application {

    private String username = "username-" + System.currentTimeMillis();
    private ActorSystem system;
    private ActorRef clusterListener;

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

        //nickname select
        TextField nicknameField = new TextField();
        nicknameField.setPromptText("Введи свой никнейм");
        final String[] nickname = {""};

        //button for connect
        Button button = new Button("Подключиться");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                host[0] = hostField.getText();
                port[0] = portField.getText();
                nickname[0] = nicknameField.getText();

                if (host[0].isBlank()) {
                    logArea.appendText("Не введён адрес хоста" + "\n");
                }
                if (port[0].isBlank()) {
                    logArea.appendText("Не введён адреспорта" + "\n");
                }
                if (!nickname[0].isBlank()) {
                    username = nickname[0];
                }

                Address address = new Address("akka", "ClusterSystem", host[0], Integer.parseInt(port[0]));

                clusterListener.tell(address, ActorRef.noSender());
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
                    ChatMessage message = new ChatMessage(username, text);
                    clusterListener.tell(message, ActorRef.noSender());
                    messageField.setText("");
                }
            }
        });

        VBox connectionPane = new VBox();
        connectionPane.getChildren().addAll(hostField, portField, nicknameField, button);

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(connectionPane);
        mainPane.setCenter(logArea);
        mainPane.setBottom(messageField);

        system = ActorSystem.create("ClusterSystem");
        clusterListener = system.actorOf(Props.create(ClusterListener.class));
        system.actorOf(MessageListener.getProps(logArea), "listener");

        stage.setScene(new Scene(mainPane, 450, 500));
        stage.setTitle("Твой хост: " + Cluster.get(system).readView().selfAddress().host().get() +
                " и порт: " + Cluster.get(system).readView().selfAddress().port().get());
        stage.show();
    }
}
