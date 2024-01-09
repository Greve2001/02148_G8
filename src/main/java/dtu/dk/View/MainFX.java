package dtu.dk.View;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
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

    private static Label prompt;


    //keylogger space
    SequentialSpace wordsTyped = new SequentialSpace();
    Thread keyLoggerThread = new Thread(new KeyPrinter());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //setting up the stage
        stage = primaryStage;
        primaryStage.setTitle("Word Wars!");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("intro.fxml"));
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(pane, 1280, 720);
        this.scene = scene;
        scene.getStylesheets().add("nice.css");


        //setting up keloger and starting keyPrinter
        stage.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            try {
                String key = event.getText();
                if(event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER){
                    if (prompt.getText().equals("")){
                        return;
                    }
                    key = "";
                    wordsTyped.put(prompt.getText());
                    prompt.setText(key);
                } else {
                    if(event.getCode() == KeyCode.BACK_SPACE && prompt.getText().length() > 0){
                        prompt.setText(prompt.getText().substring(0, prompt.getText().length() - 1));
                    }
                    prompt.setText(prompt.getText() + key);
                }
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
                pane = loader.load();
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

            setPointes();
        });
    }
    private class KeyPrinter implements Runnable{
        public void run() {
            while(true){
                try {
                    String key = (String) wordsTyped.get(new FormalField(String.class))[0];
                    System.out.println(key);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void setPointes(){
        MainFX.prompt = (Label) pane.lookup("#prompt");
        MainFX.prompt.setText("");
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        MainFX.stage = stage;
    }

}