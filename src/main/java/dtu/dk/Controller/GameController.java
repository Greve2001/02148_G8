package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.Protocol;
import dtu.dk.UpdateToken;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    private final MainFX ui;
    private final SequentialSpace fxWords = new SequentialSpace();
    private List<Pair<Peer, Player>> peers = new ArrayList<>();
    private Pair<Peer, Player> me;
    private String username;
    private String hostIP;
    private String localIP;
    private boolean isHost;
    private List<Word> wordsFalling;

    public GameController() {
        wordsFalling = new ArrayList<>();

        GUIRunner.startGUI();
        try {
            ui = MainFX.getUI();
            ui.setSpace(fxWords);
            ui.setWordsFallingList(wordsFalling);
        } catch (InterruptedException e) {
            System.err.println("Could not await latch");
            throw new RuntimeException(e);
        }

        String wordTyped = "";
        boolean exitDoWhile = false;
        do {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            wordTyped = wordTyped.toLowerCase();

            switch (wordTyped) {
                case "join" -> {
                    ui.changeScene(GameConfigs.JAVA_FX_JOIN);
                    isHost = false;
                    getInformation(isHost);
                    exitDoWhile = true;
                }
                case "host" -> {
                    ui.changeScene(GameConfigs.JAVA_FX_HOST);
                    isHost = true;
                    getInformation(isHost);
                    exitDoWhile = true;
                }
                case "exit", "quit" -> {
                    Platform.exit();
                    System.exit(0);
                }
                default -> System.out.println("Unknown command: " + wordTyped);
            }
        } while (!exitDoWhile);

        SetupController setupController = new SetupController(this);
        try {
            if (isHost) {
                setupController.host(localIP, GameConfigs.DEFAULT_PORT_HOST, localIP, GameConfigs.INIT_PORT);
            } else {
                setupController.join(localIP, GameConfigs.DEFAULT_PORT_JOIN, hostIP, GameConfigs.INIT_PORT);
            }
        } catch (NoGameSetupException e) {
            System.err.println("Could not start game");
            //todo add Alert / messageox
            throw new RuntimeException(e);
        }
        ui.addTextToTextPane("Type 'ready' to start the game");
        exitDoWhile = false;
        do {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            wordTyped = wordTyped.toLowerCase();

            switch (wordTyped) {
                case "ready" -> {
                    exitDoWhile = true;
                }
                default -> System.out.println("Unknown command: " + wordTyped);
            }
        } while (!exitDoWhile);
        ui.changeNewestTextOnTextPane("Waiting for other players to be ready");
        try {
            setupController.signalReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        peers = setupController.getPeers();
        me = peers.get(0);
        me.getValue().setUsername(username);

        ui.changeScene(GameConfigs.JAVA_FX_GAMESCREEN);
        startGame();
    }

    public void startGame() {
        Pair<Peer, Player> me = peers.get(0);

        // TODO: Should happen when a word hits the bottom of the screen
        loseLife(me);

        // TODO: Should happen when typing a word correct
        correctlyTyped();
    }

    private void loseLife(Pair<Peer, Player> pair) {
        pair.getValue().loseLife();
        if (pair.getValue().getLives() == 0) {
            try {
                pair.getKey().getSpace().put(Protocol.UPDATE, UpdateToken.DEATH, pair.getKey().getID());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                pair.getKey().getSpace().put(Protocol.UPDATE, UpdateToken.LIFE, pair.getKey().getID());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void correctlyTyped() {
        Pair<Peer, Player> me = peers.get(0);

        me.getValue().addStreak();
        if ((me.getValue().getStreak() % GameConfigs.REQUIRED_STREAK) == 0) {
            // TODO: Send word to next player
        }
    }

    //todo make strings and vars constant in gameSettings

    private void getInformation(boolean isHost) {
        if (isHost) {
            ui.addTextToTextPane(GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
        } else {
            ui.addTextToTextPane(GameConfigs.GET_HOST_IP);
        }
        boolean exitDoWhile = false;
        do {
            try {
                hostIP = (String) fxWords.get(new FormalField(String.class))[0];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (hostIP.matches(GameConfigs.REGEX_IP)) {
                exitDoWhile = true;
            } else if (hostIP.equals("exit") || hostIP.equals("quit")) {
                Platform.exit();
                System.exit(0);
            } else if (hostIP.equals(GameConfigs.GET_LOCAL_IP_YES)) {
                hostIP = getLocalIPAddress();
                exitDoWhile = true;
            } else {
                if (isHost) {
                    ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
                } else {
                    ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_HOST_IP);
                }
            }
        } while (!exitDoWhile);

        ui.addTextToTextPane(hostIP);
        if (!isHost) {
            ui.addTextToTextPane(GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
            exitDoWhile = false;
            do {
                try {
                    localIP = (String) fxWords.get(new FormalField(String.class))[0];
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (localIP.matches(GameConfigs.REGEX_IP)) {
                    exitDoWhile = true;
                } else if (localIP.equals("exit") || localIP.equals("quit")) {
                    Platform.exit();
                    System.exit(0);
                } else if (localIP.equals(GameConfigs.GET_LOCAL_IP_Y) || localIP.equals(GameConfigs.GET_LOCAL_IP_YES)) {
                    localIP = getLocalIPAddress();
                    exitDoWhile = true;
                } else {
                    ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
                }
            } while (!exitDoWhile);
            ui.addTextToTextPane(localIP);
        } else {
            localIP = hostIP;
        }

        ui.addTextToTextPane(GameConfigs.GET_USERNAME);
        exitDoWhile = false;
        do {
            try {
                username = (String) fxWords.get(new FormalField(String.class))[0];
            } catch (InterruptedException e) {
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
}

class GUIRunner implements Runnable {
    public static void startGUI() {
        new Thread(new GUIRunner()).start();
    }

    public void run() {
        MainFX.startFX();
    }
}