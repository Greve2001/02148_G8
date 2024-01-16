package dtu.dk.Controller;

import dtu.dk.FxWordsToken;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.UpdateToken;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.UPDATE;

public class WordHitController implements Runnable {
    GameController gameController;
    Space fxWords;

    public WordHitController(GameController gameController) {
        this.gameController = gameController;
        this.fxWords = gameController.getFxWords();
    }

    public void run() {
        String wordHit = null;
        while (!gameController.gameEnded) {
            try {
                wordHit = (String) fxWords.get(
                        new ActualField(FxWordsToken.HIT),
                        new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get word that hit the bottom of wordPane");
                throw new RuntimeException(e);
            }
            gameController.localGameController.loseLife(gameController.myPair);
            gameController.ui.updateLife(0, gameController.myPair.getValue().getLives());
            gameController.ui.updateStreak(gameController.myPair.getValue().getStreak());

            List<Pair<Peer, Player>> activePeerList = gameController.getActivePeers();

            for (int index = 1; index < activePeerList.size(); index++) {
                try {
                    activePeerList.get(index).getKey().getSpace().put(
                            UPDATE,
                            gameController.myPair.getValue().getLives() == 0 ? UpdateToken.DEATH : UpdateToken.LIFE,
                            gameController.myPair.getKey().getID());
                } catch (InterruptedException e) {
                    System.err.println("Could not update life");
                }
            }

            if (gameController.myPair.getValue().getLives() == 0) {
                gameController.endGame();
            }
        }
    }
}