package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.ClusterManager;
import ru.vladbakumenko.model.ChanelCompound;
import ru.vladbakumenko.model.Connection;
import ru.vladbakumenko.model.GroupMessage;
import ru.vladbakumenko.model.PrivateMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {
    private String username = "username-" + System.currentTimeMillis();
    private ActorSystem system = ActorSystem.create("ClusterSystem");
    private Cluster cluster = Cluster.get(system);
    private ActorRef clusterListener = system.actorOf(ClusterListener.getProps(cluster), "listener");
    private ObservableList<GroupMessage> groupMessages = FXCollections.observableArrayList();
    private ObservableList<PrivateMessage> privateMessages = FXCollections.observableArrayList();
    private ObservableList<String> members = FXCollections.observableArrayList();

    private ActorRef clusterManager = system.actorOf(ClusterManager.getProps(groupMessages, privateMessages, members),
            "manager");

    private List<GroupMessage> historyOfGroupMessages = new ArrayList<>();
    private List<PrivateMessage> transportList;
    private Map<ChanelCompound, List<PrivateMessage>> historyOfPrivateMessages = new HashMap<>();
    public static final String GROUP_CHAT_NAME = "GROUP CHAT";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //console
        TextArea logArea = new TextArea();
        logArea.setEditable(true);

        //list-view and buttons
        ListView<String> membersView = new ListView<>(members);
//        Button selectPrivateChat = new Button("Приватный чат");
//        Button selectGroupChat = new Button("Общий чат");

        //address of member for private chat
        final String[] nameOfChat = {GROUP_CHAT_NAME};

        //list of members
//        TextArea membersArea = new TextArea();
//        membersArea.setPrefColumnCount(20);
//        membersArea.setEditable(true);

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
//        nicknameField.setPromptText("Введи свой никнейм");
        nicknameField.setText(system.settings().config().getString("akka.remote.artery.canonical.port"));
        final String[] nickname = {""};

        //button for connect
        Button button = new Button("Подключиться");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StringBuilder sb = new StringBuilder();
                boolean show = false;

                host[0] = hostField.getText();
                port[0] = portField.getText();
                nickname[0] = nicknameField.getText();

                if (host[0].isBlank()) {
                    sb.append("Не введён адрес хоста." + "\n");
                    show = true;
                }
                if (port[0].isBlank()) {
                    sb.append("Не введён адрес порта." + "\n");
                    show = true;
                }
                if (nickname[0] != null && !nickname[0].isBlank()) {
                    username = nickname[0];
                }

                if (show) {
                    Stage newWindow = new Stage();
                    newWindow.setTitle("");
                    StackPane pane = new StackPane();
                    Label label = new Label(sb.toString());
                    pane.getChildren().add(label);
                    newWindow.setScene(new Scene(pane, 300, 100));
                    newWindow.show();
                }

                Address address = new Address("akka", "ClusterSystem", host[0], Integer.parseInt(port[0]));
                Connection connection = new Connection(username, address, cluster.selfAddress());
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

                    if (nameOfChat[0].equals(GROUP_CHAT_NAME)) {
                        GroupMessage message = new GroupMessage(username, text);
                        clusterListener.tell(message, ActorRef.noSender());
                    } else {
                        System.out.println(nameOfChat[0]);
                        PrivateMessage message = new PrivateMessage(username, text, nameOfChat[0]);
                        clusterListener.tell(message, ActorRef.noSender());
                    }
                    messageField.setText("");
                }
            }
        });

        groupMessages.addListener(new ListChangeListener<GroupMessage>() {
            @Override
            public void onChanged(Change<? extends GroupMessage> change) {
                GroupMessage message = change.getList().get(0);
                System.out.println(message.getValue() + " - group");
                if (nameOfChat[0].equals(GROUP_CHAT_NAME)) {
                    logArea.appendText(message.getSenderName() + ": " + message.getValue() + "\n");
                }
                groupMessages.remove(0);

                historyOfGroupMessages.add(message);
            }
        });

        privateMessages.addListener(new ListChangeListener<PrivateMessage>() {
            @Override
            public void onChanged(Change<? extends PrivateMessage> change) {
                PrivateMessage message = change.getList().get(0);
                System.out.println(message.getValue() + " - private");
                if (nameOfChat[0].equals(message.getSenderName()) ||
                        nameOfChat[0].equals(message.getRecipientName())) {
                    logArea.appendText(message.getSenderName() + ": " + message.getValue() + "\n");
                }
                privateMessages.remove(0);

                ChanelCompound compound = new ChanelCompound(message.getSenderName(), message.getRecipientName());
                if (historyOfPrivateMessages.containsKey(compound)) {
                    transportList = historyOfPrivateMessages.get(compound);
                } else {
                    transportList = new ArrayList<>();
                    historyOfPrivateMessages.put(compound, transportList);
                }
                transportList.add(message);
            }
        });

        membersView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1.equals(GROUP_CHAT_NAME)) {
                    logArea.clear();
                    logArea.setText(addHistoryToLog(historyOfGroupMessages));
                } else {
                    ChanelCompound chanelCompound = new ChanelCompound(username, t1);
                    if (historyOfPrivateMessages.containsKey(chanelCompound)) {
                        transportList = historyOfPrivateMessages.get(chanelCompound);
                    } else {
                        transportList = new ArrayList<>();
                        historyOfPrivateMessages.put(chanelCompound, transportList);
                    }
                    logArea.clear();
                    logArea.setText(addHistoryToLog(transportList));
                }
                nameOfChat[0] = t1;
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

        stage.setScene(new Scene(mainPane, 650, 500));
        stage.setTitle("Твой хост: " + Cluster.get(system).readView().selfAddress().host().get() +
                " и порт: " + Cluster.get(system).readView().selfAddress().port().get());
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                system.terminate();
            }
        });


//        selectPrivateChat.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                logArea.setVisible(false);
//            }
//        });
//
//        selectGroupChat.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                logArea.setVisible(true);
//                membersView.getSelectionModel().clearSelection();
//                addressOfChat[0] = "";
//            }
//        });
    }

    private String addHistoryToLog(List<? extends GroupMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (GroupMessage message : messages) {
            sb.append(message.getSenderName()).append(": ").append(message.getValue()).append("\n");
        }
        return sb.toString();
    }
}
