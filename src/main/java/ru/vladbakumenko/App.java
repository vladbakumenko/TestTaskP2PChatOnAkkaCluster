package ru.vladbakumenko;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.vladbakumenko.actors.MessageListener;
import ru.vladbakumenko.actors.SimpleClusterListener;
import ru.vladbakumenko.model.ChatMessage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //console
        TextArea logArea = new TextArea();
        logArea.setLayoutX(10);
        logArea.setLayoutY(150);
        logArea.setPrefRowCount(10);

        //message
        Pane pane = new Pane();
        Label label = new Label();
        label.setText("Ваше сообщение");
        label.setPadding(new Insets(10));

        TextArea textAreaForMessage = new TextArea();
        label.setLabelFor(textAreaForMessage);
        textAreaForMessage.setLayoutX(10);
        textAreaForMessage.setLayoutY(40);
        textAreaForMessage.setPrefRowCount(1);

        ActorSystem system = ActorSystem.create("ClusterSystem");

        ActorRef simpleClusterListener = system.actorOf(Props.create(SimpleClusterListener.class));
        ActorRef messageListener = system.actorOf(MessageListener.getProps(logArea), "listener");
        textAreaForMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String text = textAreaForMessage.getText();
                    ChatMessage message = new ChatMessage(text);
                    simpleClusterListener.tell(message, messageListener);
                    textAreaForMessage.setText("");
                }
            }
        });

        pane.getChildren().addAll(label, textAreaForMessage, logArea);

        Scene scene = new Scene(pane, 500, 500);
        stage.setScene(scene);
        stage.show();
    }
}
