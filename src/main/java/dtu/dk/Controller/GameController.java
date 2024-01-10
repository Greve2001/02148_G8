package dtu.dk.Controller;

import dtu.dk.Exceptions.NoGameSetupException;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Peer;
import dtu.dk.Model.Player;
import dtu.dk.View.MainFX;
import javafx.application.Platform;
import javafx.util.Pair;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static dtu.dk.Protocol.PLAYER_DROPPED;
import static dtu.dk.Protocol.UPDATE;
import static dtu.dk.Utils.getLocalIPAddress;

public class GameController {
    private final MainFX ui;
    private final SequentialSpace wordsTyped = new SequentialSpace();

    private ArrayList<Pair<Peer, Player>> activePeers;
    private ArrayList<Pair<Peer, Player>> allPeers;

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

        SetupController setupController = new SetupController();
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

        allPeers = setupController.getPeers();
        activePeers = new ArrayList<>(List.copyOf(allPeers));
        new Thread(new DisconnectChecker(this)).start();
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
            } else if (hostIP.equals("exit") || hostIP.equals("quit")) {
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
        ui.addTextToTextPane(username);
    }

    public ArrayList<Pair<Peer, Player>> getActivePeers() {
        return activePeers;
    }
}

class DisconnectChecker implements Runnable{

    ArrayList<Pair<Peer, Player>>  activePeerList;
    public DisconnectChecker(GameController gameController){
        activePeerList = gameController.getActivePeers();
    }
    public void run(){
        int nextPeerIndex = 1;
        while(true){
            try { // get a non existing string in the remotespace of the person next in the disconnect line
                if(activePeerList.size() > 1) {
                    activePeerList.get(nextPeerIndex).getKey().getSpace().get(new ActualField("nonexist"));
                }
            } catch (InterruptedException e) {
                //Communicate to all others that the person has disconnected - start from index 2 to exclude disconnected person
                for(int index = 2; index < activePeerList.size(); index++){
                    try {
                        activePeerList.get(index).getKey().getSpace().put(UPDATE, PLAYER_DROPPED, activePeerList.get(nextPeerIndex).getKey().getID());
                    } catch (InterruptedException ex) {
                        System.out.println("Another disconnect -.-");
                    }
                }
                if(activePeerList.size() > 1) {
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