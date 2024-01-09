package dtu.dk.View;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class MainFX extends Application implements GUIInterface {

    //used to prevent the program from continuing before the stage is shown
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static MainFX ui;
    // Use these to change scene
    private static AnchorPane pane;
    private static Scene scene;
    private static Stage stage;
    // Keylogger space
    SequentialSpace wordsTyped = new SequentialSpace();
    private Label prompt;
    private VBox textPane;

    public static void startFX() {
        launch();
    }

    public static MainFX getUI() throws InterruptedException {
        ;
        latch.await();
        return ui;
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


        // Setting up keylogger
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

        // Setting up the stage to close when the x is pressed
        stage.onCloseRequestProperty().setValue(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Showing the stage
        primaryStage.setScene(scene);
        primaryStage.show();
        setPointers();
        this.ui = this;
        latch.countDown();
    }

    @Override
    public void changeScene(String fxml) {
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
            stage.setResizable(false);
            stage.show();
            MainFX.pane = pane;
            MainFX.scene = scene;

            setPointers();
        });
    }

    private void setPointers() {
        prompt = (Label) pane.lookup("#prompt");
        if (prompt != null)
            prompt.setText("");
        textPane = (VBox) pane.lookup("#textPane");
        if (textPane != null)
            textPane.getChildren().clear();
    }

    public void setSpace(SequentialSpace space) {
        this.wordsTyped = space;
    }

    @Override
    public CountDownLatch getLatch() {
        return latch;
    }

    @Override
    public void addTextToTextPane(String text) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            label.getStyleClass().add("textOnPane");
            textPane.getChildren().add(label);
        });
    }

    @Override
    public void changeNewestTextOnTextPane(String text) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            textPane.getChildren().remove(textPane.getChildren().size() - 1);
            label.getStyleClass().add("textOnPane");
            textPane.getChildren().add(label);
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
}

