package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import javafx.util.Pair;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dtu.dk.Protocol.*;

public class SetupController {
    String localURI, publicURI;
    SpaceRepository repo;
    Space localPeerSpace;
    RemoteSpace setupSpace;
    int localID;

    GameController gameController;

    List<String> playerURIs;
    List<Integer> playerIDs;
    List<String> words;

    List<Pair<Peer, Player>> peers = new ArrayList<>();

    public SetupController(GameController gameController) {
        this.gameController = gameController;
    }

    public void host(String localIP, String localPort, String initiatorIP, String initiatorPort) throws NoGameSetupException {
        new Thread(new Initiator(localIP, initiatorPort)).start();

        join(localIP, localPort, initiatorIP, initiatorPort);
    }

    public void join(String localIP, String localPort, String initiatorIP, String initiatorPort) throws NoGameSetupException {
        try {
            prepareLocalRepository(localIP, localPort);
            connectToInitiator(initiatorIP, initiatorPort);

        } catch (Exception e) {
            repo.shutDown();
            e.printStackTrace();
            throw new NoGameSetupException();
        }
    }

    private void prepareLocalRepository(String localIP, String localPort) {
        localURI = "tcp://" + localIP + ":" + localPort + "/?keep";
        publicURI = "tcp://" + localIP + ":" + localPort + "/peer?keep";

        repo = new SpaceRepository();
        localPeerSpace = new SequentialSpace();
        repo.addGate(localURI);
        repo.add("peer", localPeerSpace);
        System.out.println("Peer: Local repo is running");
    }

    private void connectToInitiator(String initiatorIP, String initiatorPort) throws IOException, InterruptedException {
        String initiatorURI = "tcp://" + initiatorIP + ":" + initiatorPort + "/setup?keep";
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

        loadSetupRequirements();
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

    private void loadSetupRequirements() throws InterruptedException {
        // Get players
        Object[] playerRes = setupSpace.query( // Player list
                new ActualField(PLAYERS),
                new FormalField(String[].class), // URIs
                new FormalField(Integer[].class) // PlayerIDs
        );
        playerURIs = Arrays.asList((String[]) playerRes[1]);
        playerIDs = Arrays.asList((Integer[]) playerRes[2]);
        System.out.println("Peer: Got Players");

        // Find localID
        int i = playerURIs.indexOf(publicURI);
        localID = playerIDs.get(i);
        System.out.println("Peer: LocalID: " + localID);

        connectToPeers();

        // Get words
        words = Arrays.asList((String[]) setupSpace.query(
                new ActualField(WORDS),
                new FormalField(String[].class)
        )[1]);
        System.out.println("Peer: Got words");
    }

    private void connectToPeers() {
        // Add me first in the list og peers
        peers.add(new Pair<>(
                new Peer(localID, localPeerSpace),
                new Me()
        ));

        // Insert peers in correct order.
        Pair<Peer, Player> pair;

        for (int i = localID + 1 % playerURIs.size(); i < playerURIs.size(); i++) {
            pair = createPeerPlayer(i);
            if (pair != null)
                peers.add(pair);
        }

        for (int i = 0; i < localID; i++) {
            pair = createPeerPlayer(i);
            if (pair != null)
                peers.add(pair);
        }
    }

    private Pair<Peer, Player> createPeerPlayer(int index) {
        try {
            String playerURI = playerURIs.get(index);
            int playerID = playerIDs.get(index);

            RemoteSpace peerSpace = new RemoteSpace(playerURI);
            Peer peer = new Peer(playerID, peerSpace);
            return new Pair<>(peer, new Player());

        } catch (Exception e) {
            System.out.println("Peer: Player removed");
            return null;
        }
    }

    public List<Pair<Peer, Player>> getPeers() {
        return peers;
    }

    public List<String> getWords() {
        return words;
    }
}

