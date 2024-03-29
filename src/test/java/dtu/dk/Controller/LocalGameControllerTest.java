package dtu.dk.Controller;

import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.Protocol;
import javafx.util.Pair;
import org.jspace.SequentialSpace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalGameControllerTest {
    LocalGameController localGameController;
    Pair<Peer, Player> pair;
    Me myPlayer;

    @Before
    public void setUp() {
        myPlayer = new Me();
        SequentialSpace space = new SequentialSpace();
        try {
            space.put(Protocol.LIFE, myPlayer.getLives());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pair = new Pair<>(
                new Peer(0, space, ""),
                myPlayer
        );

        localGameController = new LocalGameController(pair);
    }

    @Test
    public void loseLife() {
        assertEquals(3, myPlayer.getLives());
        localGameController.loseLife(pair);
        assertEquals(2, myPlayer.getLives());
    }

    @Test
    public void correctlyTyped() {
    }

    @Test
    public void addWordToMyScreen() {
        assertEquals(0, myPlayer.getWordsOnScreen().size());
        localGameController.addWordToMyScreen(new Word("Hello"));
        assertEquals(1, myPlayer.getWordsOnScreen().size());
        assertEquals("Hello", myPlayer.getWordsOnScreen().get(0).getText());
    }
}