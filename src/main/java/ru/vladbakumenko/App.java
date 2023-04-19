package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.ClusterManager;
import ru.vladbakumenko.model.ChatMessage;
import ru.vladbakumenko.model.Connection;

public class App extends Application {

    private String username = "username-" + System.currentTimeMillis();
    private ActorSystem system;
    private ActorRef clusterListener;
    private ObservableList<ChatMessage> messages = FXCollections.observableArrayList();
    private ObservableList<String> members = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //console
        TextArea logArea = new TextArea();
        logArea.setEditable(true);

        //list-view
        ListView<String> membersView = new ListView<>();
        membersView.setItems(members);

        //list of members
        TextArea membersArea = new TextArea();
        membersArea.setPrefColumnCount(20);
        membersArea.setEditable(true);

        //host select
        TextField hostField = new TextField();
//        hostField.setPromptText("Введи адрес хоста");
        hostField.setText("127.0.0.1");
        final String[] host = {""};

        //port select
        TextField portField = new TextField();
//        portField.setPromptText("Введи номер порта");
        portField.setText("255");
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
                    logArea.appendText("Не введён адрес порта" + "\n");
                }
                if (nickname[0] != null && !nickname[0].isBlank()) {
                    username = nickname[0];
                }

                Address address = new Address("akka", "ClusterSystem", host[0], Integer.parseInt(port[0]));
                Connection connection = new Connection(username, address);

                clusterListener.tell(connection, ActorRef.noSender());
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

        messages.addListener(new ListChangeListener<ChatMessage>() {
            @Override
            public void onChanged(Change<? extends ChatMessage> change) {
                ChatMessage message = change.getList().get(0);
                logArea.appendText(message.getUsername() + ": " + message.getValue() + "\n");
                messages.remove(0);
            }
        });

//        members.addListener(new ListChangeListener<String>() {
//            @Override
//            public void onChanged(Change<? extends String> change) {
//                membersArea.setText("");
//
//                Set<String> result = new HashSet<>(members);
//
//                for (String name : result) {
//                    membersArea.appendText(name + "\n");
//                }
//            }
//        });

        VBox connectionPane = new VBox();
        connectionPane.getChildren().addAll(hostField, portField, nicknameField, button);

        BorderPane mainPane = new BorderPane();
        mainPane.setRight(membersView);
        mainPane.setTop(connectionPane);
        mainPane.setCenter(logArea);
        mainPane.setBottom(messageField);

        system = ActorSystem.create("ClusterSystem");
        clusterListener = system.actorOf(Props.create(ClusterListener.class));
        system.actorOf(ClusterManager.getProps(messages, members), "manager");

        stage.setScene(new Scene(mainPane, 650, 500));
        stage.setTitle("Твой хост: " + Cluster.get(system).readView().selfAddress().host().get() +
                " и порт: " + Cluster.get(system).readView().selfAddress().port().get());
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                system.terminate();
            }
        });
    }
}
