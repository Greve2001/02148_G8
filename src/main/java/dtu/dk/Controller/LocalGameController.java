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
        myPlayer.setMaxStreak(myPlayer.getStreak());
        pair.getValue().loseLife();
        try {
            pair.getKey().getSpace().get(new ActualField(Protocol.LIFE), new FormalField(Integer.class));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            pair.getKey().getSpace().put(Protocol.LIFE, pair.getValue().getLives());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void correctlyTyped(Word word) {
        myPlayer.increaseStreak();
        myPlayer.setLastWord(word);
        myPlayer.increaseWordsTypedCorrectCounter();

        if ((myPlayer.getStreak() % GameConfigs.REQUIRED_STREAK) == 0) {
            myPlayer.setCanSendExtraWord(true);
        }
    }

    public void addWordToMyScreen(Word word) {
        myPlayer.addWordToScreen(word);
    }

    public void inCorrectlyTyped() {
        myPlayer.setMaxStreak(myPlayer.getStreak());
        myPlayer.zeroStreak();
    }
}
