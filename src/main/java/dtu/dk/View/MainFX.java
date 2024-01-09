package dtu.dk.View;

import dtu.dk.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
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
    // Use these to change scene
    private static AnchorPane pane;
    private static Scene scene;

    private static Stage stage;

    private static Label prompt;

    // Keylogger space
    SequentialSpace wordsTyped = new SequentialSpace();
    Thread keyLoggerThread = new Thread(new KeyPrinter());

    public static void startFX() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        // Setting up the stage
        stage = primaryStage;
        primaryStage.setTitle("Word Wars!");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("intro.fxml"));
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene = new Scene(pane, 1280, 720);
        stage.setResizable(false);
        scene.getStylesheets().add("nice.css");


        // Setting up keylogger and starting keyPrinter
        stage.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            try {
                String key = event.getText();
                if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
                    if (prompt.getText().equals("")) {
                        return;
                    }
                    key = "";
                    wordsTyped.put(prompt.getText());
                    prompt.setText(key);
                } else {
                    if (event.getCode() == KeyCode.BACK_SPACE && prompt.getText().length() > 0) {
                        prompt.setText(prompt.getText().substring(0, prompt.getText().length() - 1));
                    }
                    prompt.setText(prompt.getText() + key);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        keyLoggerThread.start();

        // Setting up the stage to close when the x is pressed
        stage.onCloseRequestProperty().setValue(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Showing the stage
        primaryStage.setScene(scene);
        primaryStage.show();
        Main.latch.countDown();
    }

    public static void changeScene(String fxml) {
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getClassLoader().getResource(fxml));
            AnchorPane pane;
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

            setPoints();
        });
    }

    private class KeyPrinter implements Runnable {
        public void run() {
            while (true) {
                try {
                    String key = (String) wordsTyped.get(new FormalField(String.class))[0];
                    System.out.println(key);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void setPoints() {
        MainFX.prompt = (Label) pane.lookup("#prompt");
        MainFX.prompt.setText("");
    }
}