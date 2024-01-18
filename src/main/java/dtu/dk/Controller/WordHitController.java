package dtu.dk.Controller;

import dtu.dk.FxWordsToken;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.UpdateToken;
import dtu.dk.WordType;
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
        while (gameController.getActivePeers().size() > 1) {
            String word;
            WordType type;
            try {
                Object[] objects = fxWords.get(
                        new ActualField(FxWordsToken.HIT),
                        new FormalField(String.class),
                        new FormalField(WordType.class));
                word = (String) objects[1];
                type = (WordType) objects[2];
            } catch (InterruptedException e) {
                System.err.println("Could not get word that hit the bottom of wordPane");
                throw new RuntimeException(e);
            }
            if (word.equals(""))
                break;
            switch (type) {
                case NORMAL:
                    System.out.println("WordHitController: Normal word hit the bottom");
                    handleNormalWordHit();
                    break;
                case EXRTA_LIFE:
                    System.out.println("WordHitController: Extra life Missed");
                    break;
                case FALL_SLOW:
                    System.out.println("WordHitController: Fall slow word hit missed");
                    break;
                default:
                    System.out.println("WordHitController: Unknown word type");
            }
        }
        System.out.println("WordHitController terminated successfully");
    }

    private void handleNormalWordHit() {
        gameController.localGameController.loseLife(gameController.myPair);
        gameController.ui.updateLife(0, gameController.myPair.getValue().getLives());
        gameController.ui.updateStreak(gameController.myPair.getValue().getStreak());

        if (gameController.myPair.getValue().getLives() != 0) {
            sendUpdateLifeOrDeath(1, false);
            sendUpdateLifeOrDeath(2, false);
            sendUpdateLifeOrDeath(gameController.getActivePeers().size() - 2, false);
            sendUpdateLifeOrDeath(gameController.getActivePeers().size() - 1, false);
        } else {
            for (int i = 1; i < gameController.getActivePeers().size(); i++) {
                sendUpdateLifeOrDeath(i, true);
            }
            gameController.endGame();
        }
    }

    void sendUpdateLifeOrDeath(int index, boolean death) {
        List<Pair<Peer, Player>> activePeerList = gameController.getActivePeers();
        //check if valid index
        if (index >= activePeerList.size() || index < 1) return;
        try {
            activePeerList.get(index).getKey().getSpace().put(
                    UPDATE,
                    death ? UpdateToken.DEATH : UpdateToken.LIFE,
                    gameController.myPair.getKey().getID());
        } catch (InterruptedException e) {
            System.err.println("Could not update life");
        }
    }
}