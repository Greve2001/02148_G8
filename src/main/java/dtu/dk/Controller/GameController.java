package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.PLAYER_DROPPED;
import static dtu.dk.Protocol.UPDATE;
import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    private final MainFX ui;
    private final SequentialSpace fxWords = new SequentialSpace();
    private final LocalGameController localGameController;

    private final ArrayList<Pair<Peer, Player>> activePeers;
    private ArrayList<Pair<Peer, Player>> allPeers;
    private final Pair<Peer, Player> myPair;
    private final List<Word> commonWords = new ArrayList<>();
    boolean gameEnded = false;

    private String username;
    private String hostIP;
    private String localIP;
    private boolean isHost;

    // TODO: make strings and vars constant in gameSettings
    public GameController() {
        GUIRunner.startGUI();

        try {
            ui = MainFX.getUI();
            ui.setSpace(fxWords);
        } catch (InterruptedException e) {
            System.err.println("Could not await latch");
            throw new RuntimeException(e);
        }

        SetupController setupController = new SetupController();

        // Initial Screen
        typeHostOrJoin();

        // Join and Host Screen
        if (!isHost) typeHostIP();
        typeMyIP();
        typeUsername();

        try {
            if (isHost) {
                setupController.host(localIP, GameConfigs.DEFAULT_PORT_HOST, localIP, GameConfigs.INIT_PORT);
            } else {
                setupController.join(localIP, GameConfigs.DEFAULT_PORT_JOIN, hostIP, GameConfigs.INIT_PORT);
            }
        } catch (NoGameSetupException e) {
            System.err.println("Could not start game");
            // TODO: Add Alert / Messagebox
            throw new RuntimeException(e);
        }

        typeReady();
        ui.changeNewestTextOnTextPane("Waiting for other players to be ready");

        try {
            setupController.signalReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Set needed start variables before starting the game locally
        allPeers = setupController.getPeers();
        activePeers = new ArrayList<>(allPeers);
        new Thread(new DisconnectChecker(this)).start();

        myPair = allPeers.get(0);
        localGameController = new LocalGameController(myPair);
        myPair.getValue().setUsername(username);
        for (String word : setupController.getWords()) {
            commonWords.add(new Word(word));
        }
        ui.setWordsFallingList(localGameController.myPlayer.getWordsOnScreen());

        ui.changeScene(GameConfigs.JAVA_FX_GAMESCREEN);
    }

    /**
     * On initial screen, the user should type "host" to host a game, and "join" to join a game
     */
    private void typeHostOrJoin() {
        String wordTyped;
        boolean exitDoWhile = false;

        do {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (wordTyped.toLowerCase()) {
                case "join" -> {
                    ui.changeScene(GameConfigs.JAVA_FX_JOIN);
                    isHost = false;
                    exitDoWhile = true;
                }
                case "host" -> {
                    ui.changeScene(GameConfigs.JAVA_FX_HOST);
                    isHost = true;
                    exitDoWhile = true;
                }
                case "exit", "quit" -> {
                    Platform.exit();
                    System.exit(0);
                }
                default -> System.out.println("Unknown command: " + wordTyped);
            }
        } while (!exitDoWhile);
    }

    /**
     * The user joining the game should type the host's IP
     */
    private void typeHostIP() {
        // Please enter the host IP
        ui.addTextToTextPane(GameConfigs.GET_HOST_IP);

        boolean exitDoWhile = false;
        do {
            try {
                hostIP = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (hostIP.matches(GameConfigs.REGEX_IP))
                exitDoWhile = true;
            else if (hostIP.equals("exit") || hostIP.equals("quit")) {
                Platform.exit();
                System.exit(0);
            } else
                ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_HOST_IP);

        } while (!exitDoWhile);

        ui.addTextToTextPane(hostIP);
    }

    private void typeMyIP() {
        // Is this your IP address?
        String generatedIP = getLocalIPAddress();
        ui.addTextToTextPane(GameConfigs.GET_LOCAL_IP + generatedIP + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);

        boolean exitDoWhile = false;
        String typedIP;
        do {
            try {
                typedIP = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (typedIP.matches(GameConfigs.REGEX_IP)) {
                localIP = typedIP;
                exitDoWhile = true;
            } else {
                switch (typedIP) {
                    case GameConfigs.GET_LOCAL_IP_Y, GameConfigs.GET_LOCAL_IP_YES, "" -> {
                        localIP = generatedIP;
                        exitDoWhile = true;
                    }
                    case "exit", "quit" -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    default -> {
                        ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
                    }
                }
            }
        } while (!exitDoWhile);

        ui.addTextToTextPane(localIP);
    }

    private void typeUsername() {
        boolean exitDoWhile;
        // Please enter your username
        ui.addTextToTextPane(GameConfigs.GET_USERNAME);
        exitDoWhile = false;
        do {
            try {
                username = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get username");
                throw new RuntimeException(e);
            }

            if (username.equals("exit") || username.equals("quit")) {
                Platform.exit();
                System.exit(0);
            } else if (username.length() < 10) {
                exitDoWhile = true;
            } else {
                ui.changeNewestTextOnTextPane(GameConfigs.GET_USERNAME_INVALID + GameConfigs.GET_USERNAME);
            }
        } while (!exitDoWhile);
        ui.addTextToTextPane(username);
    }

    private void typeReady() {
        boolean exitDoWhile;
        String wordTyped;
        ui.addTextToTextPane("Type 'ready' to start the game");
        exitDoWhile = false;
        do {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (wordTyped.toLowerCase()) {
                case "ready", "" -> {
                    exitDoWhile = true;
                }
                default -> System.out.println("Unknown command: " + wordTyped);
            }
        } while (!exitDoWhile);
    }

    public void startGame() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(this::spawnWords).start();
        // TODO: Should happen when a word hits the bottom of the screen
        localGameController.loseLife(myPair);

        // TODO: Should happen when typing a word correct
        localGameController.correctlyTyped();
    }

    private void spawnWords() {
        int sleepTempo = GameConfigs.START_SLEEP_TEMPO;

        for (int i = 0, fallenWords = 0; !gameEnded; i = (i + 1) % commonWords.size(), fallenWords++) {
            localGameController.addWordToMyScreen(commonWords.get(i));
            ui.makeWordFall(commonWords.get(i));

            if (fallenWords == GameConfigs.FALLEN_WORDS_BEFORE_INCREASING_TEMPO && sleepTempo > GameConfigs.MIN_SLEEP_TEMPO) {
                sleepTempo -= GameConfigs.MIN_SLEEP_TEMPO;
                fallenWords = 0;
            }

            try {
                Thread.sleep(sleepTempo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<Pair<Peer, Player>> getActivePeers() {
        return activePeers;
    }
}

class DisconnectChecker implements Runnable {

    ArrayList<Pair<Peer, Player>> activePeerList;

    public DisconnectChecker(GameController gameController) {
        activePeerList = gameController.getActivePeers();
    }

    public void run() {
        int nextPeerIndex = 1;
        while (activePeerList.size() > 1) {
            try { // get a non-existing string in the RemoteSpace of the person next in the disconnect line
                activePeerList.get(nextPeerIndex).getKey().getSpace().get(new ActualField("nonexist"));
            } catch (InterruptedException e) {
                //Communicate to all others that the person has disconnected - start from index 2 to exclude disconnected person
                for (int index = 2; index < activePeerList.size(); index++) {
                    try {
                        //TODO - this is not picked up by the other peers yet
                        activePeerList.get(index).getKey().getSpace().put(UPDATE, PLAYER_DROPPED, activePeerList.get(nextPeerIndex).getKey().getID());
                    } catch (InterruptedException ex) {
                        System.out.println("Another disconnect -.-");
                    }
                }
                if (activePeerList.size() > 1) {
                    activePeerList.remove(nextPeerIndex);
                }
                System.out.println("Player disconnectet. Active peer list size = " + activePeerList.size());
            }
        }
    }
}

class GUIRunner implements Runnable {
    public static void startGUI() {
        new Thread(new GUIRunner()).start();
    }

    public void run() {
        MainFX.startFX();
    }
}