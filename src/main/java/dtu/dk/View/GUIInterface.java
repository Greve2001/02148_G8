package dtu.dk.View;

import java.util.concurrent.CountDownLatch;

public interface GUIInterface {

    void changeScene(String fxml);

    CountDownLatch getLatch();

}
