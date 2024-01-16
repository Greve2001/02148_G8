package dtu.dk.Controller;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;

import static dtu.dk.Protocol.READY;

public class ReadyHandler implements Runnable {
    List<String> playerURIs;
    Space space;
    int readyCounter = 0;

    public ReadyHandler(Space space, List<String> playerURIs) {
        this.playerURIs = playerURIs;
        this.space = space;
    }

    @Override
    public void run() {
        try {
            while (readyCounter < playerURIs.size() || playerURIs.size() < 2) {
                readyListen();
            }
            System.out.println("Initiator: All Peers are ready");

            space.put("local", "allReady");
        } catch (InterruptedException e) {
            System.out.println(Initiator.errorSpaceNotAvailable);
        }

    }

    private void readyListen() throws InterruptedException {
        String peerURI = (String) space.get(
                new ActualField(READY),
                new FormalField(String.class)
        )[1];

        if (playerURIs.contains(peerURI)) {
            readyCounter++;
            System.out.println("Initiator: " + peerURI + " is ready");

        }
    }
}
