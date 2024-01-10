package dtu.dk.Controller;

import dtu.dk.GameConfigs;
import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Protocol;
import dtu.dk.UpdateToken;
import javafx.util.Pair;

public class LocalGameController {
    Peer peer;
    Me myPlayer;

    public LocalGameController(Pair<Peer, Player> myPeerPlayerPair) {
        peer = myPeerPlayerPair.getKey();
        myPlayer = (Me) myPeerPlayerPair.getValue();
    }

    public void loseLife(Pair<Peer, Player> pair) {
        pair.getValue().loseLife();

        if (pair.getValue().getLives() == 0) {
            try {
                pair.getKey().getSpace().put(Protocol.UPDATE, UpdateToken.DEATH, pair.getKey().getID());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                pair.getKey().getSpace().put(Protocol.UPDATE, UpdateToken.LIFE, pair.getKey().getID());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void correctlyTyped() {
        myPlayer.addStreak();

        if ((myPlayer.getStreak() % GameConfigs.REQUIRED_STREAK) == 0) {
            // TODO: Send word to next player
        }
    }

    public void addWordToMyScreen(String word) {
        myPlayer.addWordToScreen(word);
    }
}
