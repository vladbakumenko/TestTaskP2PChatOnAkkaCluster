package ru.vladbakumenko.ui.chat;

import akka.actor.ActorRef;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.vladbakumenko.dto.*;
import ru.vladbakumenko.ui.connection.ConnectionControllerModel;

import java.util.ArrayList;
import java.util.List;

import static ru.vladbakumenko.App.GROUP_CHAT_NAME;

public class ChatController {

    @FXML
    private ListView<String> listViewOfMembers;

    @FXML
    private TextArea logArea;

    @FXML
    private TextField messageField;

    private ChatControllerModel model = new ChatControllerModel();
    private ConnectionControllerModel connectionModel = new ConnectionControllerModel();
    private List<PrivateMessage> transportList;

    private ObservableList<GroupMessage> groupMessages = FXCollections.observableArrayList();
    private ObservableList<PrivateMessage> privateMessages = FXCollections.observableArrayList();
    private ObservableList<String> members = FXCollections.observableArrayList();

    public ObservableList<GroupMessage> getGroupMessages() {
        return groupMessages;
    }

    public void setGroupMessages(ObservableList<GroupMessage> groupMessages) {
        this.groupMessages = groupMessages;
    }

    public ObservableList<PrivateMessage> getPrivateMessages() {
        return privateMessages;
    }

    public void setPrivateMessages(ObservableList<PrivateMessage> privateMessages) {
        this.privateMessages = privateMessages;
    }

    public ObservableList<String> getMembers() {
        return members;
    }

    public void setMembers(ObservableList<String> members) {
        this.members = members;
    }

    public ConnectionControllerModel getConnectionModel() {
        return connectionModel;
    }

    public void setConnectionModel(ConnectionControllerModel connectionModel) {
        this.connectionModel = connectionModel;
    }

    public ChatControllerModel getModel() {
        return model;
    }

    public void setModel(ChatControllerModel model) {
        this.model = model;
    }

    public ListView<String> getListViewOfMembers() {
        return listViewOfMembers;
    }

    @FXML
    void initialize() {
        listViewOfMembers.setItems(members);

        //list-view listener
        listViewOfMembers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                model.setNameOfChat(t1);
                if (t1.equals(GROUP_CHAT_NAME)) {
                    logArea.clear();
                    logArea.setText(addHistoryToLog(model.getHistoryOfGroupMessages()));
                } else {
                    ChanelCompound chanelCompound = new ChanelCompound(connectionModel.getNickname(), t1);
                    if (model.getHistoryOfPrivateMessages().containsKey(chanelCompound)) {
                        transportList = model.getHistoryOfPrivateMessages().get(chanelCompound);
                    } else {
                        transportList = new ArrayList<>();
                        model.getHistoryOfPrivateMessages().put(chanelCompound, transportList);
                    }
                    logArea.clear();
                    logArea.setText(addHistoryToLog(transportList));
                }
            }
        });

        //history of group-messages
        groupMessages.addListener(new ListChangeListener<GroupMessage>() {
            @Override
            public void onChanged(Change<? extends GroupMessage> change) {
                GroupMessage message = change.getList().get(0);
                System.out.println(message.getValue() + " - group");
                if (model.getNameOfChat().equals(GROUP_CHAT_NAME)) {
                    logArea.appendText(message.getSenderName() + ": " + message.getValue() + "\n");
                }
                groupMessages.remove(0);

                model.getHistoryOfGroupMessages().add(message);
            }
        });

        //history of private-messages
        privateMessages.addListener(new ListChangeListener<PrivateMessage>() {
            @Override
            public void onChanged(Change<? extends PrivateMessage> change) {
                PrivateMessage message = change.getList().get(0);
                System.out.println(message.getValue() + " - private");

                System.out.println("model.getNameOfChat() " + model.getNameOfChat());
                System.out.println("message.getSenderName() " + message.getSenderName());
                System.out.println("message.getRecipientName() " + message.getRecipientName());

                if (model.getNameOfChat().equals(message.getSenderName()) ||
                        model.getNameOfChat().equals(message.getRecipientName())) {
                    logArea.appendText(message.getSenderName() + ": " + message.getValue() + "\n");
                }
                privateMessages.remove(0);

                ChanelCompound compound = new ChanelCompound(message.getSenderName(), message.getRecipientName());
                if (model.getHistoryOfPrivateMessages().containsKey(compound)) {
                    transportList = model.getHistoryOfPrivateMessages().get(compound);
                } else {
                    transportList = new ArrayList<>();
                    model.getHistoryOfPrivateMessages().put(compound, transportList);
                }
                transportList.add(message);
            }
        });
    }

    @FXML
    void enterKeyPressed() {
        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String text = messageField.getText();

                    if (model.getNameOfChat().equals(GROUP_CHAT_NAME)) {
                        GroupMessage message = new GroupMessage(connectionModel.getNickname(), text);
                        connectionModel.getClusterListener().tell(message, ActorRef.noSender());
                    } else {
                        System.out.println("model.getNameOfChat() " + model.getNameOfChat());
                        System.out.println("connectionModel.getNickname() " + connectionModel.getNickname());
                        PrivateMessage message = new PrivateMessage(connectionModel.getNickname(), text, model.getNameOfChat());
                        connectionModel.getClusterListener().tell(message, ActorRef.noSender());
                    }
                    messageField.setText("");
                }
            }
        });
    }

    private String addHistoryToLog(List<? extends GroupMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (GroupMessage message : messages) {
            sb.append(message.getSenderName()).append(": ").append(message.getValue()).append("\n");
        }
        return sb.toString();
    }
}
