package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.UpdateToken;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.*;
import static dtu.dk.UpdateToken.PLAYER_DROPPED;
import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    protected final LocalGameController localGameController;
    protected final MainFX ui;
    protected final Pair<Peer, Player> myPair;
    private final SequentialSpace fxWords = new SequentialSpace();
    private final ArrayList<Pair<Peer, Player>> activePeers;
    private final List<Word> commonWords = new ArrayList<>();
    boolean gameEnded = false;
    private final ArrayList<Pair<Peer, Player>> allPeers;
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
        new Thread(new UpdateChecker(this)).start();

        // Set needed start variables before starting the game locally
        myPair = allPeers.get(0);
        localGameController = new LocalGameController(myPair);
        myPair.getValue().setUsername(username);
        try {
            myPair.getKey().getSpace().put(LIFE, myPair.getValue().getLives());
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
                    case GameConfigs.Y, GameConfigs.YES, GameConfigs.EMPTY_STRING -> {
                        localIP = generatedIP;
                        exitDoWhile = true;
                    }
                    case GameConfigs.EXIT, GameConfigs.QUIT -> {
                        Platform.exit();
                        System.exit(0);
                    }
                    default ->
                            ui.changeNewestTextOnTextPane(GameConfigs.GET_LOCAL_IP_INVALID + GameConfigs.GET_LOCAL_IP + getLocalIPAddress() + GameConfigs.GET_LOCAL_IP_Y_YES + GameConfigs.GET_LOCAL_IP_IF_NOT);
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
                ui.changeNewestTextOnTextPane(GameConfigs.CONFIRM_USERNAME + username);
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


        ui.addTextToTextPane(username);
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

        new Thread(new WordTypedController(this)).start();
        new Thread(new WordHitController(this)).start();

    }

    private void spawnWords() {
        int wpm = GameConfigs.START_WPM;
        int wordsBeforeIncrease = GameConfigs.FALLEN_WORDS_BEFORE_INCREASING_TEMPO;

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
}

class WordTypedController implements Runnable {
    GameController gameController;
    Space fxWords;

    public WordTypedController(GameController gameController) {
        this.gameController = gameController;
        this.fxWords = gameController.getFxWords();
    }

    public void run() {
        //todo should exit when game ends
        String wordTyped;
        while (!gameController.gameEnded) {
            try {
                wordTyped = (String) fxWords.get(new ActualField(FxWordsToken.TYPED), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get typed word");
                throw new RuntimeException(e);
            }
            String finalWordTyped = wordTyped;
            List<Word> wordsOnScreen = gameController.localGameController.myPlayer.getWordsOnScreen();
            for (Word word : wordsOnScreen) {
                if (word.getText().equals(finalWordTyped)) {

                    gameController.ui.removeWordFalling(word);
                    gameController.localGameController.correctlyTyped();

                    gameController.ui.updateStreak(gameController.localGameController.myPlayer.getStreak());

                    gameController.ui.updateLastWord(word.getText());


                    break;
                }
                else{
                    gameController.localGameController.inCorrectlyTyped();
                    gameController.ui.updateStreak(gameController.localGameController.myPlayer.getStreak());

                }
            }

        }
    }
}

class WordHitController implements Runnable {
    GameController gameController;
    Space fxWords;

    public WordHitController(GameController gameController) {
        this.gameController = gameController;
        this.fxWords = gameController.getFxWords();
    }

    public void run() {
        //todo should exit when game ends
        String wordHit = null;
        while (!gameController.gameEnded) {
            try {
                wordHit = (String) fxWords.get(new ActualField(FxWordsToken.HIT), new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get word that hit the bottom of wordPane");
                throw new RuntimeException(e);
            }
            gameController.localGameController.loseLife(gameController.myPair);
            gameController.ui.updateLife(0, gameController.myPair.getValue().getLives());
            gameController.ui.updateStreak(gameController.myPair.getValue().getStreak());

            List<Pair<Peer, Player>> activePeerList = gameController.getActivePeers();

            for (int index = 1; index < activePeerList.size(); index++) {
                try {
                    activePeerList.get(index).getKey().getSpace().put(UPDATE, gameController.myPair.getValue().getLives() == 0 ? UpdateToken.DEATH : UpdateToken.LIFE, gameController.myPair.getKey().getID());
                } catch (InterruptedException e) {
                    System.out.println("Could not update life");
                }
            }

            if (gameController.myPair.getValue().getLives() == 0) {
                gameController.gameEnded = true;
                List<Word> wordsOnScreen = gameController.localGameController.myPlayer.getWordsOnScreen();
                for (Word word : wordsOnScreen) {
                    gameController.ui.removeWordFalling(word);
                }
                gameController.ui.changeScene(GameConfigs.JAVA_FX_JOIN);
                if (gameController.getActivePeers().size() == 1) {
                    gameController.ui.addTextToTextPane("You won the game");
                } else {
                    gameController.ui.addTextToTextPane("You lost the game");
                }

            }

        }
    }
}

class DisconnectChecker implements Runnable {

    ArrayList<Pair<Peer, Player>> activePeerList;
    GameController gameController;

    public DisconnectChecker(GameController gameController) {
        this.gameController = gameController;
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
                        activePeerList.get(index).getKey().getSpace().put(UPDATE, PLAYER_DROPPED, activePeerList.get(nextPeerIndex).getKey().getID());
                    } catch (InterruptedException ex) {
                        System.out.println("Another disconnect -.-");
                    }
                }
                if (activePeerList.size() > 1) {
                    activePeerList.remove(nextPeerIndex);
                }
                gameController.updateUIPlayerList();
                System.out.println("Player disconnected. Active peer list size = " + activePeerList.size());
            }
        }
    }
}

class UpdateChecker implements Runnable {
    GameController gameController;
    List<Pair<Peer, Player>> activePLayerList;
    Space localSpace;

    public UpdateChecker(GameController gameController) {
        this.gameController = gameController;
        this.activePLayerList = gameController.getActivePeers();
        this.localSpace = activePLayerList.get(0).getKey().getSpace();
    }

    public void run() {
        while (activePLayerList.size() > 1) {
            try {
                Object[] updateTup = localSpace.get( // Player list
                        new ActualField(UPDATE),
                        new FormalField(UpdateToken.class), // URIs
                        new FormalField(Integer.class) // Order
                );
                switch ((UpdateToken) updateTup[1]) {
                    case LIFE:
                        //get the id we need to check
                        //get the persons life and update it
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            //Check if the ID is correct
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                Object[] lifeTup = activePLayerList.get(index).getKey().getSpace().query(
                                        new ActualField(LIFE),
                                        new FormalField(Integer.class));
                                activePLayerList.get(index).getValue().setLives((Integer) lifeTup[1]);
                                gameController.updateUIPlayerList();
                                break;
                                //TODO - COULD HAVE - make this only check the people we display
                            }
                        }
                        break;
                    case DEATH:
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("Player died. Active peer list size = " + activePLayerList.size());
                                break;
                            }
                        }
                        break;
                    case SEND_WORD:
                        Object[] extraWordTup = localSpace.get(
                                new ActualField(EXTRA_WORD),
                                new FormalField(String.class));
                        //TODO actually send the word..
                        break;
                    case PLAYER_DROPPED:
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("Player disconnected. Active peer list size = " + activePLayerList.size());
                                break;
                            }
                        }
                        break;
                    default:
                        System.out.println("UpdateChecker error - wrong update protocol - did nothing..");
                }
            } catch (InterruptedException e) {
                System.out.println("UpdateChecker error - Cant get local space - something is wrong??");
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