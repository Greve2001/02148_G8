package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import org.jspace.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static dtu.dk.Protocol.*;

public class SetupController {
    String localURI, publicURI;
    SpaceRepository repo;
    RemoteSpace setupSpace;

    GameController gameController;

    List<String> playerURIs;
    List<Integer> playerIDs;
    List<String> words;

    public SetupController(GameController gameController) {
        this.gameController = gameController;
    }

    public void join(String localIP, String localPort, String initiatorIP) throws NoGameSetupException {
        try {
            prepareLocalRepository(localIP, localPort);
            connectToInitiator(initiatorIP);
            signalReady(); // TODO Should not be called sequentially


        } catch (Exception e) {
            repo.shutDown();
            throw new NoGameSetupException();
        }
    }

    public void host(String localIP, String localPort, String initiatorIP, String initiatorPort) throws NoGameSetupException {
        new Thread(new Initiator(localIP, initiatorPort)).start();

        join(localIP, localPort, initiatorIP);
    }

    private void prepareLocalRepository(String localIP, String localPort) {
        localURI = "tcp://" + localIP + ":" + localPort + "/?keep";
        publicURI = "tcp://" + localIP + ":" + localPort + "/peer?keep";

        repo = new SpaceRepository();
        Space peerSpace = new SequentialSpace();
        repo.addGate(localURI);
        repo.add("peer", peerSpace);
        System.out.println("Peer: Local repo is running");
    }

    private void connectToInitiator(String initiatorIP) throws IOException, InterruptedException {
        String initiatorURI = "tcp://" + initiatorIP + ":31125/setup?keep";
        setupSpace = new RemoteSpace(initiatorURI);

        setupSpace.put(CONNECT, publicURI);
        setupSpace.get(
                new ActualField(CONNECTED),
                new ActualField(publicURI)
        );
        System.out.println("Peer: Connected to Initiator");
    }

    public void signalReady() throws InterruptedException {
        setupSpace.put(READY, publicURI);
        System.out.println("Peer: Signaled ready");

        // Get players
        Object[] playerRes = setupSpace.query( // Player list
                new ActualField(PLAYERS),
                new FormalField(String[].class), // URIs
                new FormalField(Integer[].class) // Order
        );
        playerURIs = Arrays.asList((String[]) playerRes[1]);
        playerIDs = Arrays.asList((Integer[]) playerRes[2]);
        System.out.println("Peer: Got Players");

        // Get words
        words = Arrays.asList((String[]) setupSpace.query(
                new ActualField(WORDS),
                new FormalField(String[].class)
        )[1]);
        System.out.println("Peer: Got words");

        // Sent loading
        setupSpace.put(LOADING_DONE, publicURI);
        System.out.println("Peer: Sent loading done");

        // Wait for start
        setupSpace.get(
                new ActualField(START),
                new ActualField(publicURI)
        );
        System.out.println("Peer: Received start");

        // Sent started
        setupSpace.put(STARTED, publicURI);
        // TODO make gamecontroller start game
    }
}

