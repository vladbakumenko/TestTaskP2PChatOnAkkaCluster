package ru.vladbakumenko.ui.connection;

import akka.actor.ActorRef;
import akka.actor.Address;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConnectionControllerModel {
    private ActorRef clusterListener = null;
    private StringProperty host = new SimpleStringProperty("127.0.0.1");
    private StringProperty port = new SimpleStringProperty("255");
    private StringProperty nickname = new SimpleStringProperty("");

    public Address getSelfAddress() {
        return selfAddress;
    }

    public void setSelfAddress(Address selfAddress) {
        this.selfAddress = selfAddress;
    }

    private Address selfAddress = null;

    public ConnectionControllerModel(ActorRef clusterListener, StringProperty host, StringProperty port, StringProperty nickname) {
        this.clusterListener = clusterListener;
        this.host = host;
        this.port = port;
        this.nickname = nickname;
    }

    public ConnectionControllerModel() {
    }

    public ActorRef getClusterListener() {
        return clusterListener;
    }

    public void setClusterListener(ActorRef clusterListener) {
        this.clusterListener = clusterListener;
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public String getPort() {
        return port.get();
    }

    public StringProperty portProperty() {
        return port;
    }

    public void setPort(String port) {
        this.port.set(port);
    }

    public String getNickname() {
        return nickname.get();
    }

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname.set(nickname);
    }
}
