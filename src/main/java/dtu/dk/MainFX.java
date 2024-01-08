package dtu.dk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {
    AnchorPane pane;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Word War!");
        FXMLLoader loadrer = new FXMLLoader(getClass().getClassLoader().getResource("gameScreen.fxml"));
        try {
            pane = (AnchorPane) loadrer.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(pane, 1280, 720);
        scene.getStylesheets().add("nice.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}