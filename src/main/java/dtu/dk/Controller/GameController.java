package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.util.List;

import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    private final MainFX ui;
    private final SequentialSpace wordsTyped = new SequentialSpace();

    private List<Pair<Peer, Player>> peers;

    private String username;
    private String hostIP;
    private String localIP;
    private boolean isHost;

    public GameController() {
        GUIRunner.startGUI();
        try {
            ui = MainFX.getUI();
            ui.setSpace(wordsTyped);
        } catch (InterruptedException e) {
            System.err.println("Could not await latch");
            throw new RuntimeException(e);
        }

        String wordTyped = "";
        boolean exitDoWhile = false;
        do {
            try {
                wordTyped = (String) wordsTyped.get(new FormalField(String.class))[0];
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
                setupController.host(localIP, GameConfigs.DEFAULT_PORT, localIP, GameConfigs.INIT_PORT);
            } else {
                setupController.join(localIP, GameConfigs.DEFAULT_PORT, hostIP, GameConfigs.INIT_PORT);
            }
        } catch (NoGameSetupException e) {
            System.err.println("Could not start game");
            //todo add Alert / messageox
            throw new RuntimeException(e);
        }
        ui.addTextToTextPane("Type 'ready' to start the game");
        wordTyped = "";
        exitDoWhile = false;
        do {
            try {
                wordTyped = (String) wordsTyped.get(new FormalField(String.class))[0];
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
        ui.changeScene(GameConfigs.JAVA_FX_GAMESCREEN);
    }

    public void startGame() {
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
                hostIP = (String) wordsTyped.get(new FormalField(String.class))[0];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (hostIP.matches(GameConfigs.REGEX_IP)) {
                exitDoWhile = true;
            } else if (hostIP.equals(GameConfigs.GET_LOCAL_IP_Y) || localIP.equals(GameConfigs.GET_LOCAL_IP_YES)) {
                Platform.exit();
                System.exit(0);
            } else if (hostIP.equals(GameConfigs.GET_LOCAL_IP_Y) || hostIP.equals(GameConfigs.GET_LOCAL_IP_YES)) {
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
                    localIP = (String) wordsTyped.get(new FormalField(String.class))[0];
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
                username = (String) wordsTyped.get(new FormalField(String.class))[0];
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