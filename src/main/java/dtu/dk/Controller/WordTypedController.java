package dtu.dk.Controller;

import dtu.dk.FxWordsToken;
import dtu.dk.Model.Me;
import dtu.dk.Model.Word;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.EXTRA_WORD;
import static dtu.dk.Protocol.UPDATE;
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

        while (!gameController.gameEnded) {
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
            if (me.getLastWord() != null && me.getLastWord().getText().equals(wordTyped) && gameController.getActivePeers().size() > 1) {
                me.setLastWord(null);
                sendExtraWordToNextPlayer(new Word(wordTyped));
                gameController.ui.updateLastWord("");
                flag = false;
            }
            for (Word word : wordsOnScreen) {
                if (word.getText().equals(wordTyped)) {
                    gameController.localGameController.correctlyTyped(word);
                    gameController.ui.removeWordFalling(word);
                    gameController.ui.updateStreak(me.getStreak());
                    gameController.ui.updateLastWord(me.getLastWord().getText());
                    if (me.isCanSendExtraWord() && gameController.getActivePeers().size() > 1)
                        sendExtraWordToNextPlayer(word);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                gameController.localGameController.inCorrectlyTyped();
                gameController.ui.updateStreak(gameController.localGameController.myPlayer.getStreak());
            }
        }
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}