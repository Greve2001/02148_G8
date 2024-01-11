package dtu.dk.Controller;


import dtu.dk.GameConfigs;
import dtu.dk.Utils;
import org.jspace.*;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.*;


public class Initiator implements Runnable {
    final static String errorSpaceNotAvailable = "The space used for initiating is not available";

    public final String setupSpaceName = "setup";
    private final String uri;
    private final SpaceRepository repo = new SpaceRepository();
    private final Space space = new SequentialSpace();

    protected List<String> playerURIs = new ArrayList<>();

    public Initiator(String initiatorIP, String initiatorPort) {
        uri = "tcp://" + initiatorIP + ":" + initiatorPort + "/?keep";
    }

    @Override
    public void run() {
        repo.addGate(uri);
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
            System.out.println("Initiator: Got allReady locally");

            // TODO: Should have safe way to stop or interrupt threads. They just run forever now
            //connHandler.stop();

            sendPlayerList();
            sendWords();
            waitForLoadingDone();
            startGame();

        } catch (InterruptedException e) {
            System.err.println("Space does not exists");
            System.exit(1);
        }

    }

    private void sendPlayerList() throws InterruptedException {
        // Generate random player order
        // TODO make random
        List<Integer> playerIDs = new ArrayList<>();
        for (int i = 0; i < playerURIs.size(); i++) {
            playerIDs.add(i);
        }

        space.put(
                PLAYERS,
                Utils.StringListToArray(playerURIs),
                Utils.IntegerListToArray(playerIDs)
        );
        System.out.println("Initiator: Sent player list");
    }

    private void sendWords() throws InterruptedException {
        List<String> words = WordCreator.getSubset(GameConfigs.WORDS_IN_PLAY);

        space.put(WORDS, Utils.StringListToArray(words));
        System.out.println("Initiator: Sent words");
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

        System.out.println("Initiator: All loadings done");
    }

    private void startGame() throws InterruptedException {
        for (String peerURI : playerURIs) {
            space.put(START, peerURI);
        }
        System.out.println("Initiator: Sent Start to all peers");

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
        System.out.println("Initiator: Received Started back from all peers");
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
            System.out.println("Initiator: Peer connected: " + peerURI);
            return peerURI;

        } else {
            System.out.println("Initiator: Duplicate peerURI");
            return "";
        }
    }

    private void sendPeerConformation(String uri) {
        try {
            space.put(CONNECTED, uri);
            System.out.println("Initiator sent " + CONNECT + " to peer: " + uri);
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
