package dtu.dk.Model;

import java.util.ArrayList;
import java.util.List;

public class Me extends Player {
    private final List<Word> wordOnScreen;
    private Word lastWord;
    private boolean canSendExtraWord;
    private int wordsTypedCorrectCounter;
    private int wordsSentCounter;
    private int maxStreak;

    public Me() {
        wordOnScreen = new ArrayList<>();
        lastWord = null;

        wordsTypedCorrectCounter = 0;
        wordsSentCounter = 0;
    }

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
        if (lastWord == null)
            wordsSentCounter++;
        this.lastWord = lastWord;
    }

    /**
     * The player can send an Extra word when reaching a streak of a certain size
     *
     * @return true the player can send a word to another player. Otherwise false
     */
    public boolean canSendExtraWord() {
        if (canSendExtraWord) {
            canSendExtraWord = false;
            wordsSentCounter++;
            return true;
        }
        return false;
    }

    public void setCanSendExtraWord(boolean canSendExtraWord) {
        this.canSendExtraWord = canSendExtraWord;
    }

    public int getWordsTypedCorrectCounter() {
        return wordsTypedCorrectCounter;
    }

    public void increaseWordsTypedCorrectCounter() {
        this.wordsTypedCorrectCounter++;
    }

    public int getWordsSentCounter() {
        return wordsSentCounter;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    /**
     * Only updates the maxStreak if streak is greater than the current maxStreak
     *
     * @param streak Your new maxStreak
     */
    public void setMaxStreak(int streak) {
        if (streak > this.maxStreak)
            maxStreak = streak;
    }
}
