package dtu.dk.Model;

import java.util.ArrayList;
import java.util.List;

public class Me extends Player {
    private final List<String> wordOnScreen = new ArrayList<>();

    public void addWordToScreen(String word) {
        wordOnScreen.add(word);
    }

    public void removeWordFromScreen(String word) {
        wordOnScreen.remove(word);
    }

    public List<String> getWordOnScreen() {
        return wordOnScreen;
    }
}
