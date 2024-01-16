package dtu.dk.Controller;

import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.net.UnknownHostException;
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
                String connURI = activePeerList.get(nextPeerIndex).getKey().getURI();
                String keepURI = connURI.replace("?conn", "?keep");
                System.out.println("keepURI is: " + keepURI);
                RemoteSpace keepSpace = new RemoteSpace(keepURI);
                keepSpace.get(new ActualField("nonexist"));
            } catch (InterruptedException e) {
                // Communicate to one behind and 3 in front of disconnect that there was a disconnect
                sendDisconnectToIndex(0, nextPeerIndex);
                sendDisconnectToIndex(2, nextPeerIndex);
                sendDisconnectToIndex(3, nextPeerIndex);
                sendDisconnectToIndex(activePeerList.size()-1, nextPeerIndex);
                System.out.println("DisconnectChecker: Player disconnected. Active peer list size = " + activePeerList.size());
            } catch (UnknownHostException e) {
                System.out.println("Trying to connect with keepURI in DisconnectChecker - Unknown host");
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println("Trying to connect with keepURI in DisconnectChecker - IO");
                throw new RuntimeException(e);
            }
        }
    }
    void sendDisconnectToIndex(int index, int nextPeerIndex){
        try {
            activePeerList.get(index).getKey().getSpace().put(
                    UPDATE,
                    PLAYER_DROPPED,
                    activePeerList.get(nextPeerIndex).getKey().getID());
        } catch (InterruptedException ex) {
            System.out.println("Another disconnect -.-");
        }
    }
}
