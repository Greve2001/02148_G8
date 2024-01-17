package dtu.dk.Controller;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.CONNECT;
import static dtu.dk.Protocol.CONNECTED;

public class ConnectionHandler implements Runnable {
    List<String> playerURIs;
    Space space;
    private boolean gameStarted = false;

    public ConnectionHandler(Space space, List<String> playerURIs) {
        this.playerURIs = playerURIs;
        this.space = space;
    }

    @Override
    public void run() {
        try {
            while (!gameStarted) {
                String uri = listenForPeerURI();
                if (!uri.isEmpty())
                    sendPeerConformation(uri);
            }
        } catch (InterruptedException e) {
            System.out.println(Initiator.errorSpaceNotAvailable);
        }
        System.out.println("ConnectionHandler Thread terminated successfully");
    }

    private String listenForPeerURI() throws InterruptedException {
        String peerURI = (String) space.get(
                new ActualField(CONNECT),
                new FormalField(String.class)
        )[1];

        if (!playerURIs.contains(peerURI)) {
            playerURIs.add(peerURI);
            System.out.println("ConnectionHandler: Peer connected: " + peerURI);
            return peerURI;

        } else {
            System.out.println("ConnectionHandler: Duplicate peerURI");
            return "";
        }
    }

    public void setGameStarted() {
        gameStarted = true;
    }

    private void sendPeerConformation(String uri) {
        try {
            space.put(CONNECTED, uri);
            System.out.println("ConnectionHandler sent " + CONNECT + " to peer: " + uri);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
