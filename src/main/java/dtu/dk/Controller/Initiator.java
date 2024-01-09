package dtu.dk.Controller;

import dtu.dk.Protocol;
import javafx.util.Pair;
import org.jspace.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static dtu.dk.Protocol.*;


public class Initiator {
    final static String errorSpaceNotAvailable = "The space used for initiating is not available";

    public final String setupSpaceName = "setup";
    private String uri;
    private SpaceRepository repo = new SpaceRepository();
    private Space space = new SequentialSpace();

    protected List<String> playerURIs = new ArrayList<>();

    public Initiator(String localIP) {
        this.uri = "tcp://" + localIP + ":31125/?keep";
        repo.addGate(this.uri);
        repo.add(setupSpaceName, space);

        Thread connHandler = new Thread(new ConnectionHandler(space, playerURIs));
        Thread readyHandler = new Thread(new ReadyHandler(space, playerURIs));

        connHandler.start();
        readyHandler.start();

        try {
            space.get(
                    new ActualField("local"),
                    new ActualField("allReady")
            );

            // TODO: Should have safe way to stop or interrupt threads. They just run forever now
            //connHandler.stop();

            sendPlayerList();
            sendWords();
            waitForLoadingDone();
            startGame();

        } catch (InterruptedException e) {
            System.out.println("Space does not exists");
            System.exit(1);
        }
    }

    private void sendPlayerList() throws InterruptedException {
        // Generate random player order
        // TODO make random
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < playerURIs.size(); i++) {
            order.add(i);
        }

        space.put(PLAYERS, playerURIs, order);
    }

    private void sendWords() throws InterruptedException {
        // TODO get actual words
        List<String> words = new ArrayList<>();
        words.add("Test");

        space.put(WORDS, words);
    }

    private void waitForLoadingDone() throws InterruptedException {
        List<String> doneLoadingURIs = new ArrayList<>();

        while (doneLoadingURIs.size() != playerURIs.size()) {
            String uri = (String) space.get(
                    new ActualField(LOADING_DONE),
                    new FormalField(String.class)
            )[1];

            if (doneLoadingURIs.contains(uri))
                continue;

            doneLoadingURIs.add(uri);
        }
    }

    private void startGame() throws InterruptedException {
        for (String peerURI : playerURIs) {
            space.put(START, peerURI);
        }

        List<String> startedURIs = new ArrayList<>();
        while (startedURIs.size() < playerURIs.size()) {
            String uri = (String) space.get(
                    new ActualField(STARTED),
                    new FormalField(String.class)
            )[1];

            if (startedURIs.contains(uri))
                continue;

            startedURIs.add(uri);
        }
    }
}

class ConnectionHandler implements Runnable {
    List<String> playerURIs;
    Space space;
    public ConnectionHandler(Space space, List<String> playerURIs) {
        this.playerURIs = playerURIs;
        this.space = space;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String uri = listenForPeerURI();
                if (!uri.isEmpty())
                    sendPeerConformation(uri);
            }
        } catch (InterruptedException e) {
            System.out.println(Initiator.errorSpaceNotAvailable);
        }
    }

    private String listenForPeerURI() throws InterruptedException {
        String peerURI = (String) space.get(
                new ActualField(CONNECT),
                new FormalField(String.class)
        )[1];

        if (!playerURIs.contains(peerURI)) {
            playerURIs.add(peerURI);
            System.out.println("Peer connected: " + peerURI);
            return peerURI;

        } else {
            System.out.println("Duplicate peerURI");
            return "";
        }
    }

    private void sendPeerConformation(String uri) {
        try {
            space.put(CONNECTED, uri);
            System.out.println("Sent: " + CONNECT + " to peer: " + uri);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

class ReadyHandler implements Runnable {
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
            while (readyCounter == playerURIs.size() && readyCounter >= 2) {
                readyListen();
            }
            System.out.println("All Peers are ready");

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

        if (playerURIs.contains(peerURI))
            readyCounter++;
    }

}
