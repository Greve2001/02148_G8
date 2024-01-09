package dtu.dk.Controller;

import dtu.dk.View.MainFX;
import javafx.application.Platform;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

public class GameController {

    private MainFX ui;

    private SequentialSpace wordsTyped = new SequentialSpace();

    private String username;
    private String hostIP;
    private String localIP;


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
        Boolean exitDoWhile = false;
        do {
            try {
                wordTyped = (String) wordsTyped.get(new FormalField(String.class))[0];

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            wordTyped = wordTyped.toLowerCase();

            switch (wordTyped) {
                case "join" -> {
                    ui.changeScene("join.fxml");
                    getInformation(false);
                    exitDoWhile = true;
                }
                case "host" -> {
                    ui.changeScene("host.fxml");
                    getInformation(true);
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

    public void startGame() {
        System.out.println("Username: " + username);
        System.out.println("Host IP: " + hostIP);
        System.out.println("Local IP: " + localIP);
    }
    //todo make strings and vars constant in gameSettings


    private void getInformation(boolean isHost) {
        if (isHost) {
            ui.addTextToTextPane("Please enter your IP address");
        } else {
            ui.addTextToTextPane("Please enter the host's IP address");
        }
        Boolean exitDoWhile = false;
        do {
            try {
                hostIP = (String) wordsTyped.get(new FormalField(String.class))[0];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (hostIP.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                exitDoWhile = true;
            } else if (hostIP.equals("exit") || hostIP.equals("quit")) {
                Platform.exit();
                System.exit(0);
            } else {
                if (isHost) {
                    ui.changeNewestTextOnTextPane("Invalid - Please enter your IP address");
                } else {
                    ui.changeNewestTextOnTextPane("Invalid - Please enter the host's IP address");
                }
            }
        } while (!exitDoWhile);
        ui.addTextToTextPane(hostIP);
        if (!isHost) {
            ui.addTextToTextPane("Please enter your IP address");
            exitDoWhile = false;
            do {
                try {
                    localIP = (String) wordsTyped.get(new FormalField(String.class))[0];
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (localIP.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                    exitDoWhile = true;
                } else if (localIP.equals("exit") || localIP.equals("quit")) {
                    Platform.exit();
                    System.exit(0);
                } else {
                    ui.changeNewestTextOnTextPane("Invalid - Please enter your IP address");
                }
            } while (!exitDoWhile);
            ui.addTextToTextPane(localIP);
        } else {
            localIP = hostIP;
        }
        ui.addTextToTextPane("Please enter your username");
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
                ui.changeNewestTextOnTextPane("Please enter a valid username");
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