package ru.vladbakumenko.controller;

import akka.actor.ActorRef;
import akka.actor.Address;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ru.vladbakumenko.App;
import ru.vladbakumenko.model.Connection;
import ru.vladbakumenko.model.ConnectionControllerModel;

public class ConnectionController {

    @FXML
    private Button connect;

    @FXML
    private TextField host;

    @FXML
    private TextField nickname;

    @FXML
    private TextField port;

    private ConnectionControllerModel model = new ConnectionControllerModel();

    private App app = null;

    private String defaultUserName = "username-" + System.currentTimeMillis();

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void setModel(ConnectionControllerModel model) {
        this.model = model;
    }

    public ConnectionControllerModel getModel() {
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
            model.getClusterListener().tell(connection, ActorRef.noSender());

            app.getPrimaryStage().close();
            app.showChatWindow(model);
        });
    }
}
