package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.*;
import static dtu.dk.UpdateToken.USERNAME;
import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    protected final LocalGameController localGameController;
    protected final MainFX ui;
    protected final Pair<Peer, Player> myPair;
    protected final List<Word> commonWords = new ArrayList<>();
    private final SequentialSpace fxWords = new SequentialSpace();
    private final ArrayList<Pair<Peer, Player>> activePeers;
    private final ArrayList<Pair<Peer, Player>> allPeers;
    boolean gameEnded = false;
    private String username;
    private String hostIP;
    private String localIP;
    private boolean isHost;

    public GameController() {
        this(GameConfigs.DEFAULT_PORT_JOIN);
    }

    public GameController(String port) {
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
                setupController.join(localIP, port, hostIP, GameConfigs.INIT_PORT);
            }
        } catch (NoGameSetupException e) {
            System.err.println("Could not start game");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Could not connect to host\n");
                alert.setContentText("Make sure the host is running\n" +
                        "If it is please check the host IP and try again\n" +
                        "If the problem persists check the fire wall for port " +
                        GameConfigs.INIT_PORT + " " + GameConfigs.DEFAULT_PORT_HOST + " " + GameConfigs.DEFAULT_PORT_JOIN);
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        Platform.exit();
                        System.exit(1);
                    }
                });
            });
            //throw new RuntimeException(e);
        }

        typeReady();
        ui.changeNewestTextOnTextPane(GameConfigs.WAITING_FOR_PLAYERS_TO_TYPE_READY);

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
        try {
            myPair.getKey().getSpace().put(LIFE, myPair.getValue().getLives());
            myPair.getKey().getSpace().put(GET_USERNAME, myPair.getValue().getUsername());
            for (int i = 1; i < activePeers.size(); i++) {
                activePeers.get(i).getKey().getSpace().put(UPDATE, USERNAME, myPair.getKey().getID());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                case GameConfigs.JOIN -> {
                    ui.changeScene(GameConfigs.JAVA_FX_JOIN);
                    isHost = false;
                    exitDoWhile = true;
                }
                case GameConfigs.HOST -> {
                    ui.changeScene(GameConfigs.JAVA_FX_HOST);
                    isHost = true;
                    exitDoWhile = true;
                }
                case GameConfigs.EXIT, GameConfigs.QUIT -> {
                    Platform.exit();
                    System.exit(0);
                }
                default -> System.err.println("Unknown command: " + wordTyped);
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
            else {
                switch (hostIP) {
                    case GameConfigs.EXIT, GameConfigs.QUIT -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    default ->
                            ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_HOST_IP);
                }
            }
        } while (!exitDoWhile);

        ui.addTextToTextPane(hostIP);
    }

    private void typeMyIP() {
        // Is this your IP address?
        String generatedIP = getLocalIPAddress();
        ui.addTextToTextPane(GameConfigs.GET_LOCAL_IP + generatedIP + " " + GameConfigs.Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);

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
                    case GameConfigs.Y, GameConfigs.YES, GameConfigs.EMPTY_STRING -> {
                        localIP = generatedIP;
                        exitDoWhile = true;
                    }
                    case GameConfigs.EXIT, GameConfigs.QUIT -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    default ->
                            ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
                }
            }
        } while (!exitDoWhile);

        ui.addTextToTextPane("IP: " + localIP);
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

            switch (username) {
                case GameConfigs.EXIT, GameConfigs.QUIT -> {
                    Platform.exit();
                    System.exit(0);
                }
                case GameConfigs.EMPTY_STRING -> {
                    username = GameConfigs.DEFAULT_USERNAME;
                    exitDoWhile = true;
                }
                default -> {
                    if (username.length() < 10)
                        exitDoWhile = true;
                    else
                        ui.changeNewestTextOnTextPane(GameConfigs.GET_USERNAME_INVALID + GameConfigs.GET_USERNAME);
                }
            }

            if (exitDoWhile) {
                ui.changeNewestTextOnTextPane(GameConfigs.CONFIRM_USERNAME1 + username + GameConfigs.CONFIRM_USERNAME2);
                String confirmation;
                try {
                    confirmation = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
                } catch (InterruptedException e) {
                    System.err.println("Could not get confirmation");
                    throw new RuntimeException(e);
                }

                switch (confirmation) {
                    case GameConfigs.Y, GameConfigs.YES, GameConfigs.EMPTY_STRING -> {
                    }
                    default -> {
                        ui.changeNewestTextOnTextPane(GameConfigs.GET_USERNAME);
                        exitDoWhile = false;
                    }
                }
            }
        } while (!exitDoWhile);

        ui.addTextToTextPane("Username: " + username);
    }

    private void typeReady() {
        boolean exitDoWhile;
        String wordTyped;
        ui.addTextToTextPane(GameConfigs.TYPE_READY);
        exitDoWhile = false;
        do {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (wordTyped.toLowerCase()) {
                case GameConfigs.READY, GameConfigs.EMPTY_STRING -> exitDoWhile = true;
                default -> System.out.println(GameConfigs.UNKNOWN_CMD + wordTyped);
            }
        } while (!exitDoWhile);
    }

    public void startGame() {
        new Thread(this::spawnWords).start();
        updateUIPlayerList();
        new Thread(new UpdateChecker(this)).start();
        new Thread(new WordTypedController(this)).start();
        new Thread(new WordHitController(this)).start();
    }

    private void spawnWords() {
        int wpm = GameConfigs.START_WPM;
        int wordsBeforeIncrease;

        for (int i = 0, fallenWords = 0; !gameEnded; i = (i + 1) % commonWords.size(), fallenWords++) {
            localGameController.addWordToMyScreen(commonWords.get(i));
            ui.makeWordFall(commonWords.get(i));

            wordsBeforeIncrease = wpm / GameConfigs.SEND_WORD_RATIO;

            if (fallenWords == wordsBeforeIncrease && wpm < GameConfigs.MAX_WPM) {
                wpm += GameConfigs.WPM_INCREASE;
                fallenWords = 0;
            }

            int sleepInterval = (60 / wpm) * 1000;

            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<Pair<Peer, Player>> getActivePeers() {
        return activePeers;
    }

    /**
     * Also update player lives
     * This only works for other players - not local life
     */
    protected void updateUIPlayerList() {
        if (gameEnded)
            return;
        switch (activePeers.size()) {
            case 1 -> {
                for (int index = -2; index < 3; index++) {
                    if (index != 0) {
                        ui.updatePlayerName(index, "");
                        ui.updateLife(index, 0);
                    }
                }
            }
            case 2 -> {
                ui.updatePlayerName(1, activePeers.get(1).getValue().getUsername());
                ui.updateLife(1, activePeers.get(1).getValue().getLives());
                for (int index = -2; index < 3; index++) {
                    if (index == 0 || index == 1) {
                        continue;
                    }
                    ui.updatePlayerName(index, "");
                    ui.updateLife(index, 0);
                }
            }
            case 3 -> {
                ui.updatePlayerName(1, activePeers.get(1).getValue().getUsername());
                ui.updateLife(1, activePeers.get(1).getValue().getLives());
                ui.updatePlayerName(-1, activePeers.get(activePeers.size() - 1).getValue().getUsername());
                ui.updateLife(-1, activePeers.get(activePeers.size() - 1).getValue().getLives());
                ui.updatePlayerName(-2, "");
                ui.updateLife(-2, 0);
                ui.updatePlayerName(2, "");
                ui.updateLife(2, 0);

            }
            case 4 -> {
                ui.updatePlayerName(1, activePeers.get(1).getValue().getUsername());
                ui.updateLife(1, activePeers.get(1).getValue().getLives());
                ui.updatePlayerName(-1, activePeers.get(activePeers.size() - 1).getValue().getUsername());
                ui.updateLife(-1, activePeers.get(activePeers.size() - 1).getValue().getLives());
                ui.updatePlayerName(-2, "");
                ui.updateLife(-2, 0);
                ui.updatePlayerName(2, activePeers.get(2).getValue().getUsername());
                ui.updateLife(2, activePeers.get(2).getValue().getLives());
            }

            default -> {
                ui.updatePlayerName(1, activePeers.get(1).getValue().getUsername());
                ui.updateLife(1, activePeers.get(1).getValue().getLives());
                ui.updatePlayerName(-1, activePeers.get(activePeers.size() - 1).getValue().getUsername());
                ui.updateLife(-1, activePeers.get(activePeers.size() - 1).getValue().getLives());
                ui.updatePlayerName(-2, activePeers.get(activePeers.size() - 2).getValue().getUsername());
                ui.updateLife(-2, activePeers.get(activePeers.size() - 2).getValue().getLives());
                ui.updatePlayerName(2, activePeers.get(2).getValue().getUsername());
                ui.updateLife(2, activePeers.get(2).getValue().getLives());
            }
        }
    }

    protected SequentialSpace getFxWords() {
        return fxWords;
    }

    public void endGame() {
        if (this.gameEnded)
            return;
        this.gameEnded = true;
        List<Word> wordsOnScreen = this.localGameController.myPlayer.getWordsOnScreen();
        for (Word word : wordsOnScreen) {
            this.ui.removeWordFalling(word);
        }
        this.ui.changeScene(GameConfigs.JAVA_FX_JOIN);
        if (this.activePeers.size() == 1) {
            this.ui.addTextToTextPane("You won the game");
        } else {
            this.ui.addTextToTextPane("You lost the game");
        }

        localGameController.myPlayer.setMaxStreak(localGameController.myPlayer.getStreak());

        this.ui.addTextToTextPane("");
        this.ui.addTextToTextPane("Stats:");
        this.ui.addTextToTextPane("You have typed " + localGameController.myPlayer.getWordsTypedCorrectCounter() + " words correct.");
        this.ui.addTextToTextPane("You have sent " + localGameController.myPlayer.getWordsSentCounter() + " words to other player.");
        this.ui.addTextToTextPane("Your placement: " + activePeers.size());
        this.ui.addTextToTextPane("You had a maximum streak of: " + localGameController.myPlayer.getMaxStreak());

    }
}