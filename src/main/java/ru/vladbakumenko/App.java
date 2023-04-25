package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import ru.vladbakumenko.actors.ClusterListener;
import ru.vladbakumenko.actors.ClusterManager;
import ru.vladbakumenko.controller.ChatController;
import ru.vladbakumenko.controller.ConnectionController;
import ru.vladbakumenko.model.ConnectionControllerModel;

import java.util.Objects;

public class App extends Application {

    private FXMLLoader loader = new FXMLLoader();
    private Stage primaryStage;
    private AnchorPane rootLayout;
    private ActorSystem system = ActorSystem.create("ClusterSystem");
    private Cluster cluster = Cluster.get(system);
    private ActorRef clusterListener = system.actorOf(ClusterListener.getProps(cluster), "listener");
    public static final String GROUP_CHAT_NAME = "GROUP CHAT";

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        stage.setTitle("Твой хост: " + Cluster.get(system).readView().selfAddress().host().get() +
                " и порт: " + Cluster.get(system).readView().selfAddress().port().get());

        stage.show();

        showConnectionWindow();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                system.terminate();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @SneakyThrows
    public void showConnectionWindow() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Objects.requireNonNull(getClass().getResource("/Connection.fxml")));
        rootLayout = loader.load();

        Scene scene = new Scene(rootLayout, 400, 300);

        ConnectionController controller = loader.getController();
        controller.setApp(this);
        controller.getModel().setClusterListener(clusterListener);
        controller.getModel().setSelfAddress(cluster.selfAddress());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @SneakyThrows
    public void showChatWindow(ConnectionControllerModel connectionModel) {
        loader.setLocation(Objects.requireNonNull(getClass().getResource("/Chat.fxml")));
        rootLayout = loader.load();

        Scene scene = new Scene(rootLayout, 650, 500);
        ChatController controller = loader.getController();
        controller.setConnectionModel(connectionModel);
        system.actorOf(ClusterManager.getProps(controller),
                "manager");
        Stage chatStage = new Stage();
        chatStage.setScene(scene);
        chatStage.setTitle("Твой никнейм: " + connectionModel.getNickname());
        chatStage.show();
    }
}
