package dtu.dk.Controller;

import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Me;
import dtu.dk.Model.Word;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.*;
import static dtu.dk.UpdateToken.SEND_WORD;

public class WordTypedController implements Runnable {
    GameController gameController;
    Space fxWords;

    public WordTypedController(GameController gameController) {
        this.gameController = gameController;
        this.fxWords = gameController.getFxWords();
    }

    public void run() {
        String wordTyped;

        while (gameController.getActivePeers().size() > 1) {
            try {
                wordTyped = (String) fxWords.get(
                        new ActualField(FxWordsToken.TYPED),
                        new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get typed word");
                throw new RuntimeException(e);
            }

            List<Word> wordsOnScreen = gameController.localGameController.myPlayer.getWordsOnScreen();
            Me me = gameController.localGameController.myPlayer;
            boolean flag = true;

            // If the player retype their last typed word
            if (me.getLastWord() != null && me.getLastWord().getText().equals(wordTyped) && gameController.getActivePeers().size() > 1) {
                me.setLastWord(null);
                attemptExtraWordSend(new Word(wordTyped));
                gameController.ui.updateLastWord("");
                flag = false;
            }
            // Try to match the player's typed word with a word on the screen
            for (Word word : wordsOnScreen) {
                if (word.getText().equals(wordTyped)) {
                    switch (word.getType()) {
                        case NORMAL:
                            normalWordTyped(me, word);
                            break;
                        case EXRTA_LIFE:
                            extraLifeWordTyped(me, word);
                            break;
                        default:
                            System.out.println("WordTypedController: Unknown word type");
                    }
                    flag = false;
                    break;
                }
            }
            // If no match to the player's typed word was found
            if (flag) {
                gameController.localGameController.inCorrectlyTyped();
                gameController.ui.updateStreak(gameController.localGameController.myPlayer.getStreak());
            }
        }
        System.out.println("WordTypedController terminated successfully");
    }

    private void extraLifeWordTyped(Me me, Word word) {
        gameController.ui.removeWordFalling(word);
        me.addLife();
        try {
            gameController.localGameController.peer.getSpace().get(new ActualField(LIFE), new FormalField(Integer.class));
            gameController.localGameController.peer.getSpace().put(LIFE, me.getLives());
        } catch (InterruptedException e) {
            System.err.println("Could not update life");
            throw new RuntimeException(e);
        }
        gameController.ui.updateLife(0, me.getLives());
        gameController.wordHitController.sendUpdateLifeOrDeath(1, false);
        gameController.wordHitController.sendUpdateLifeOrDeath(2, false);
        gameController.wordHitController.sendUpdateLifeOrDeath(gameController.getActivePeers().size() - 2, false);
        gameController.wordHitController.sendUpdateLifeOrDeath(gameController.getActivePeers().size() - 1, false);
    }

    private void normalWordTyped(Me me, Word word) {
        gameController.localGameController.correctlyTyped(word);

        gameController.ui.updateStreak(me.getStreak());
        gameController.ui.updateLastWord(me.getLastWord().getText());
        if (me.canSendExtraWord() && gameController.getActivePeers().size() > 1)
            attemptExtraWordSend(word);
    }

    private void attemptExtraWordSend(Word word) {
        int streak = gameController.localGameController.myPlayer.getStreak();
        double timesToSend = streak * GameConfigs.SEND_LAST_WORD_CHANCE;

        for (int i = 0; i < (int) timesToSend; i++) {
            sendExtraWordToNextPlayer(word);
        }
        if (timesToSend % 1 >= Math.random())
            sendExtraWordToNextPlayer(word);
    }

    private void sendExtraWordToNextPlayer(Word word) {
        try {
            Space nextPlayerSpace = gameController.getActivePeers().get(1).getKey().getSpace();
            nextPlayerSpace.put(
                    EXTRA_WORD,
                    word.getText()
            );
            nextPlayerSpace.put(
                    UPDATE,
                    SEND_WORD,
                    gameController.myPair.getKey().getID()
            );

            gameController.localGameController.myPlayer.increaseWordSentCounter();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}