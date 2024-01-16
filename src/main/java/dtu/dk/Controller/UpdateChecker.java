package dtu.dk.Controller;

import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.UpdateToken;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.*;

public class UpdateChecker implements Runnable {
    GameController gameController;
    List<Pair<Peer, Player>> activePLayerList;
    Space localSpace;

    public UpdateChecker(GameController gameController) {
        this.gameController = gameController;
        this.activePLayerList = gameController.getActivePeers();
        this.localSpace = activePLayerList.get(0).getKey().getSpace();
    }

    public void run() {
        while (!gameController.gameEnded && activePLayerList.size() > 1) {
            try {
                Object[] updateTup = localSpace.get( // Player list
                        new ActualField(UPDATE),
                        new FormalField(UpdateToken.class), // URIs
                        new FormalField(Integer.class) // PlayerID
                );
                switch ((UpdateToken) updateTup[1]) {
                    case LIFE -> {
                        //get the id we need to check
                        //get the persons life and update it
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            //Check if the ID is correct
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                Object[] lifeTup = activePLayerList.get(index).getKey().getSpace().query(
                                        new ActualField(LIFE),
                                        new FormalField(Integer.class));
                                activePLayerList.get(index).getValue().setLives((Integer) lifeTup[1]);
                                gameController.updateUIPlayerList();
                                break;
                                //TODO - COULD HAVE - make this only check the people we display
                            }
                        }
                    }
                    case DEATH -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("UpdateChecker: Player died. Active peer list size = " + activePLayerList.size());
                                break;
                            }
                        }
                    }
                    case SEND_WORD -> {
                        Object[] extraWordTup = localSpace.get(
                                new ActualField(EXTRA_WORD),
                                new FormalField(String.class));
                        gameController.ui.makeWordFall(new Word(gameController.commonWords.get((int) (Math.random() * gameController.commonWords.size())).getText()));
                    }
                    case PLAYER_DROPPED -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("UpdateChecker: Player disconnected. Active peer list size = " + activePLayerList.size());
                            }
                        }
                    }
                    case USERNAME -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                Object[] usernameTup = activePLayerList.get(index).getKey().getSpace().query(
                                        new ActualField(GET_USERNAME),
                                        new FormalField(String.class));
                                activePLayerList.get(index).getValue().setUsername((String) usernameTup[1]);
                                gameController.updateUIPlayerList();

                            }
                        }
                    }
                    default -> System.out.println("UpdateChecker error - wrong update protocol - did nothing..");
                }
                if (activePLayerList.size() == 1) {
                    gameController.endGame();
                }
            } catch (InterruptedException e) {
                System.err.println("UpdateChecker error - Can't get local space - Something is wrong??");
            }
        }
    }
}