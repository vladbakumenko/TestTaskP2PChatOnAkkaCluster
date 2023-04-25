package ru.vladbakumenko.controller;

import akka.actor.ActorRef;
import akka.actor.Address;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ru.vladbakumenko.App;
import ru.vladbakumenko.model.Connection;
import ru.vladbakumenko.model.ConnectionUiModel;

public class ConnectionController {

    @FXML
    private Button connect;

    @FXML
    private TextField host;

    @FXML
    private TextField nickname;

    @FXML
    private TextField port;

    private ConnectionUiModel model = new ConnectionUiModel();

    private App app = null;

    private String defaultUserName = "username-" + System.currentTimeMillis();

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void setModel(ConnectionUiModel model) {
        this.model = model;
    }

    public ConnectionUiModel getModel() {
        return model;
    }

    @FXML
    void initialize() {
        host.textProperty().bindBidirectional(model.hostProperty());
        port.textProperty().bindBidirectional(model.portProperty());
        nickname.textProperty().bindBidirectional(model.nicknameProperty());

        connect.setOnAction(actionEvent -> {
            if (model.getNickname().isBlank()) {
                model.setNickname(defaultUserName);
            }
            Address address = new Address("akka", "ClusterSystem", model.getHost(),
                    Integer.parseInt(model.getPort()));

            Connection connection = new Connection(model.getNickname(), address, model.getSelfAddress());
            System.out.println(connection + "!!!!!!!!!!!!!!!!!!");
            model.getClusterListener().tell(connection, ActorRef.noSender());

            app.getPrimaryStage().close();
            app.showChatWindow(model);
        });
    }
}
