package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Me;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.Model.Word;
import dtu.dk.UpdateToken;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.LIFE;
import static dtu.dk.Protocol.*;
import static dtu.dk.UpdateToken.*;
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
        if (gameEnded)
            return;
        this.gameEnded = true;
        List<Word> wordsOnScreen = this.localGameController.myPlayer.getWordsOnScreen();
        for (Word word : wordsOnScreen) {
            this.ui.removeWordFalling(word);
        }
        this.ui.changeScene(GameConfigs.JAVA_FX_JOIN);
        if (this.getActivePeers().size() == 1) {
            this.ui.addTextToTextPane("You won the game");
        } else {
            this.ui.addTextToTextPane("You lost the game");
        }
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
        String wordTyped;

        while (!gameController.gameEnded) {
            try {
                wordTyped = (String) fxWords.get(
                        new ActualField(FxWordsToken.TYPED),
                        new FormalField(String.class))[1];
            } catch (InterruptedException e) {
                System.err.println("Could not get typed word");
                throw new RuntimeException(e);
            }

            List<Word> wordsOnScreen = gameController.localGameController.myPlayer.getWordsOnScreen();
            Me me = gameController.localGameController.myPlayer;
            boolean flag = true;
            if (me.getLastWord() != null && me.getLastWord().getText().equals(wordTyped) && gameController.getActivePeers().size() > 1) {
                me.setLastWord(null);
                sendExtraWordToNextPlayer(new Word(wordTyped));
                gameController.ui.updateLastWord("");
                flag = false;
            }
            for (Word word : wordsOnScreen) {
                if (word.getText().equals(wordTyped)) {
                    gameController.localGameController.correctlyTyped(word);
                    gameController.ui.removeWordFalling(word);
                    gameController.ui.updateStreak(me.getStreak());
                    gameController.ui.updateLastWord(me.getLastWord().getText());
                    if (me.isCanSendExtraWord() && gameController.getActivePeers().size() > 1)
                        sendExtraWordToNextPlayer(word);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                gameController.localGameController.inCorrectlyTyped();
                gameController.ui.updateStreak(gameController.localGameController.myPlayer.getStreak());
            }
        }
    }

    private void sendExtraWordToNextPlayer(Word word) {
        try {
            Space nextPlayerSpace = gameController.getActivePeers().get(1).getKey().getSpace();
            nextPlayerSpace.put(
                    EXTRA_WORD,
                    word.getText()
            );
            nextPlayerSpace.put(
                    UPDATE,
                    SEND_WORD,
                    gameController.myPair.getKey().getID()
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        String wordHit = null;
        while (!gameController.gameEnded) {
            try {
                wordHit = (String) fxWords.get(
                        new ActualField(FxWordsToken.HIT),
                        new FormalField(String.class))[1];
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
                    activePeerList.get(index).getKey().getSpace().put(
                            UPDATE,
                            gameController.myPair.getValue().getLives() == 0 ? UpdateToken.DEATH : UpdateToken.LIFE,
                            gameController.myPair.getKey().getID());
                } catch (InterruptedException e) {
                    System.err.println("Could not update life");
                }
            }

            if (gameController.myPair.getValue().getLives() == 0) {
                gameController.endGame();
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
                // Communicate to all others that the person has disconnected - start from index 2 to exclude disconnected person
                for (int index = 0; index < activePeerList.size(); index++) {
                    try {
                        activePeerList.get(index).getKey().getSpace().put(UPDATE, PLAYER_DROPPED, activePeerList.get(nextPeerIndex).getKey().getID());
                    } catch (InterruptedException ex) {
                        System.out.println("Another disconnect -.-");
                    }
                }

                //if (activePeerList.size() > 1) {
                //activePeerList.remove(nextPeerIndex);
                //}
                /*if (!gameController.gameEnded)
                    gameController.updateUIPlayerList();

                 */
                System.out.println("DisconnectChecker: Player disconnected. Active peer list size = " + activePeerList.size());
            }
        }
        //gameController.endGame();
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
        while (!gameController.gameEnded && activePLayerList.size() > 1) {
            try {
                Object[] updateTup = localSpace.get( // Player list
                        new ActualField(UPDATE),
                        new FormalField(UpdateToken.class), // URIs
                        new FormalField(Integer.class) // PlayerID
                );
                switch ((UpdateToken) updateTup[1]) {
                    case LIFE -> {
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
                    }
                    case DEATH -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("UpdateChecker: Player died. Active peer list size = " + activePLayerList.size());
                                break;
                            }
                        }
                    }
                    case SEND_WORD -> {
                        Object[] extraWordTup = localSpace.get(
                                new ActualField(EXTRA_WORD),
                                new FormalField(String.class));
                        gameController.ui.makeWordFall(new Word(gameController.commonWords.get((int) (Math.random() * gameController.commonWords.size())).getText()));
                    }
                    case PLAYER_DROPPED -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                activePLayerList.remove(index);
                                gameController.updateUIPlayerList();
                                System.out.println("UpdateChecker: Player disconnected. Active peer list size = " + activePLayerList.size());
                            }
                        }
                    }
                    case USERNAME -> {
                        for (int index = 1; index < activePLayerList.size(); index++) {
                            if (activePLayerList.get(index).getKey().getID() == (Integer) updateTup[2]) {
                                Object[] usernameTup = activePLayerList.get(index).getKey().getSpace().query(
                                        new ActualField(GET_USERNAME),
                                        new FormalField(String.class));
                                activePLayerList.get(index).getValue().setUsername((String) usernameTup[1]);
                                gameController.updateUIPlayerList();

                            }
                        }
                    }
                    default -> System.out.println("UpdateChecker error - wrong update protocol - did nothing..");
                }
                if (activePLayerList.size() == 1) {
                    gameController.endGame();
                }
            } catch (InterruptedException e) {
                System.err.println("UpdateChecker error - Can't get local space - Something is wrong??");
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