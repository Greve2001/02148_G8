package dtu.dk.Model;

import java.util.ArrayList;
import java.util.List;

public class Me extends Player {
    private final List<Word> wordOnScreen = new ArrayList<>();
    private Word lastWord;
    private boolean canSendExtraWord;

    public void addWordToScreen(Word word) {
        wordOnScreen.add(word);
    }

    public List<Word> getWordsOnScreen() {
        return wordOnScreen;
    }

    public Word getLastWord() {
        return lastWord;
    }

    public void setLastWord(Word lastWord) {
        this.lastWord = lastWord;
    }

    public boolean isCanSendExtraWord() {
        if (canSendExtraWord) {
            canSendExtraWord = false;
            return true;
        }
        return false;
    }

    public void setCanSendExtraWord(boolean canSendExtraWord) {
        this.canSendExtraWord = canSendExtraWord;
    }
}
