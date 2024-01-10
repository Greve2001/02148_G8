package dtu.dk.Model;

import java.util.ArrayList;
import java.util.List;

public class Me extends Player {
    private final List<Word> wordOnScreen = new ArrayList<>();

    public void addWordToScreen(Word word) {
        wordOnScreen.add(word);
    }

    public void removeWordFromScreen(Word word) {
        wordOnScreen.remove(word);
    }

    public List<Word> getWordsOnScreen() {
        return wordOnScreen;
    }
}
