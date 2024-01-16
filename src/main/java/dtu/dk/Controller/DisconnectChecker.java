package dtu.dk.Controller;

import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import javafx.util.Pair;
import org.jspace.ActualField;

import java.util.ArrayList;

import static dtu.dk.Protocol.UPDATE;
import static dtu.dk.UpdateToken.PLAYER_DROPPED;

public class DisconnectChecker implements Runnable {

    ArrayList<Pair<Peer, Player>> activePeerList;
    GameController gameController;

    public DisconnectChecker(GameController gameController) {
        this.gameController = gameController;
        activePeerList = gameController.getActivePeers();
    }

    public void run() {
        int nextPeerIndex = 1;
        while (activePeerList.size() > 1) {
            try { // get a non-existing string in the RemoteSpace of the person next in the disconnect line
                activePeerList.get(nextPeerIndex).getKey().getSpace().get(new ActualField("nonexist"));
            } catch (InterruptedException e) {
                // Communicate to all others that the person has disconnected - start from index 2 to exclude disconnected person
                for (int index = 0; index < activePeerList.size(); index++) {
                    try {
                        activePeerList.get(index).getKey().getSpace().put(UPDATE, PLAYER_DROPPED, activePeerList.get(nextPeerIndex).getKey().getID());
                    } catch (InterruptedException ex) {
                        System.out.println("Another disconnect -.-");
                    }
                }

                //if (activePeerList.size() > 1) {
                //activePeerList.remove(nextPeerIndex);
                //}
                /*if (!gameController.gameEnded)
                    gameController.updateUIPlayerList();

                 */
                System.out.println("DisconnectChecker: Player disconnected. Active peer list size = " + activePeerList.size());
            }
        }
        //gameController.endGame();
    }
}
