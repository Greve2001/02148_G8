package dtu.dk.Controller;

import dtu.dk.GameConfigs;
import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.Protocol;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class LocalGameController {
    Peer peer;
    Me myPlayer;

    public LocalGameController(Pair<Peer, Player> myPeerPlayerPair) {
        peer = myPeerPlayerPair.getKey();
        myPlayer = (Me) myPeerPlayerPair.getValue();
    }

    public void loseLife(Pair<Peer, Player> pair) {
        pair.getValue().loseLife();
        try {
            pair.getKey().getSpace().get(new ActualField(Protocol.LIFE), new FormalField(Integer.class));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (pair.getValue().getLives() == 0) {
            try {
                pair.getKey().getSpace().put(Protocol.LIFE, 0);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                pair.getKey().getSpace().put(Protocol.LIFE, pair.getValue().getLives());
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

    public void addWordToMyScreen(Word word) {
        myPlayer.addWordToScreen(word);
    }
}
