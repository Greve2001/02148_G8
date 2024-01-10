package dtu.dk.View;

import dtu.dk.Model.Word;
import org.jspace.SequentialSpace;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public interface GUIInterface {

    void changeScene(String fxml);

    CountDownLatch getLatch();

    void setSpace(SequentialSpace space);

    void setWordsFallingList(List<Word> wordsFalling);

    void makeWordFall(Word word);

    void removeWordFalling(Word word);

    void addTextToTextPane(String text);

    void changeNewestTextOnTextPane(String text);

    void updateLife(int player, int life) throws NullPointerException;

    void updatePlayerName(int player, String name) throws NullPointerException;

    void updateStreak(int streak) throws NullPointerException;

    void updateLastWord(String word) throws NullPointerException;

}
