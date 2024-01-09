package dtu.dk.View;

import org.jspace.SequentialSpace;

import java.util.concurrent.CountDownLatch;

public interface GUIInterface {

    void changeScene(String fxml);

    CountDownLatch getLatch();

    void setSpace(SequentialSpace space);

    void addTextToTextPane(String text);

    void changeNewestTextOnTextPane(String text);

}
