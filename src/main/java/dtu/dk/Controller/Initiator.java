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
    private final Space initialSpace = new SequentialSpace();

    protected List<String> playerURIs = new ArrayList<>();

    public Initiator(String initiatorIP, String initiatorPort) {
        uri = "tcp://" + initiatorIP + ":" + initiatorPort + "/?conn";
    }

    @Override
    public void run() {
        repo.addGate(uri);
        repo.add(setupSpaceName, initialSpace);

        ConnectionHandler connHandler = new ConnectionHandler(initialSpace, playerURIs);
        new Thread(connHandler).start();
        new Thread(new ReadyHandler(initialSpace, playerURIs)).start();


        try {
            initialSpace.get(
                    new ActualField("local"),
                    new ActualField("allReady")
            );
            System.out.println("Initiator: Got allReady locally");

            sendPlayerList();
            sendWords();
            waitForLoadingDone();
            startGame();

            repo.closeGate(uri);
            System.out.println("Initiator: Closed gate");
            repo.remove(setupSpaceName);
            System.out.println("Initiator: Removed space");
            repo.shutDown();
            System.out.println("Initiator: Shut down repo");

        } catch (InterruptedException e) {
            System.err.println("Space does not exists");
            System.exit(1);
        }

        connHandler.setGameStarted();
        try {
            initialSpace.put(CONNECT, "");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Initiator Thread terminated successfully");

    }

    private void sendPlayerList() throws InterruptedException {
        // Generate random player order
        //TODO make random
        List<Integer> playerIDs = new ArrayList<>();
        for (int i = 0; i < playerURIs.size(); i++) {
            playerIDs.add(i);
        }

        initialSpace.put(
                PLAYERS,
                Utils.StringListToArray(playerURIs),
                Utils.IntegerListToArray(playerIDs)
        );
        System.out.println("Initiator: Sent player list");
    }

    private void sendWords() throws InterruptedException {
        List<String> words = WordCreator.getSubset(GameConfigs.WORDS_IN_PLAY);

        initialSpace.put(WORDS, Utils.StringListToArray(words));
        System.out.println("Initiator: Sent words");
    }

    private void waitForLoadingDone() throws InterruptedException {
        List<String> doneLoadingURIs = new ArrayList<>();

        while (doneLoadingURIs.size() != playerURIs.size()) {
            String uri = (String) initialSpace.get(
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
            initialSpace.put(START, peerURI);
        }
        System.out.println("Initiator: Sent Start to all peers");

        List<String> startedURIs = new ArrayList<>();
        while (startedURIs.size() < playerURIs.size()) {
            String uri = (String) initialSpace.get(
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



