package dtu.dk;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.io.IOException;

public class MainFX extends Application {
    //use thees to change scene
    private static AnchorPane pane;
    private static Scene scene;
    private static Stage stage;

    //keylogger space
    SequentialSpace keysPressed = new SequentialSpace();
    Thread keyLoggerThread = new Thread(new KeyPrinter());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //setting up the stage
        stage = primaryStage;
        primaryStage.setTitle("Word Wars!");
        FXMLLoader loadrer = new FXMLLoader(getClass().getClassLoader().getResource("intro.fxml"));
        try {
            pane = (AnchorPane) loadrer.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(pane, 1280, 720);
        this.scene = scene;
        scene.getStylesheets().add("nice.css");


        //setting up keloger and starting keyPrinter
        stage.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            try {
                keysPressed.put(event.getText());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        keyLoggerThread.start();

        //setting up the stage to close when the x is pressed
        stage.onCloseRequestProperty().setValue(e -> {
            Platform.exit();
            System.exit(0);
        });

        //showing the stage
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void changeScene(String fxml){
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getClassLoader().getResource(fxml));
            AnchorPane pane = null;
            try {
                pane = (AnchorPane) loader.load();
            } catch (IOException e) {
                System.err.println("Could not load fxml file: " + fxml);
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(pane, 1280, 720);
            scene.getStylesheets().add("nice.css");
            stage.setScene(scene);
            stage.show();
            MainFX.pane = pane;
            MainFX.scene = scene;
        });
    }

    private class KeyPrinter implements Runnable{
        public void run() {
            while(true){
                try {
                    String key = (String) keysPressed.get(new FormalField(String.class))[0];
                    System.out.println(key);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}