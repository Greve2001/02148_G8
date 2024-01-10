package dtu.dk.Controller;

import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
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
    public void setUp() throws Exception {
        myPlayer = new Me();

        pair = new Pair<>(
                new Peer(0, new SequentialSpace()),
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
        localGameController.addWordToMyScreen("Hello");
        assertEquals(1, myPlayer.getWordsOnScreen().size());
        assertEquals("Hello", myPlayer.getWordsOnScreen().get(0));
    }
}