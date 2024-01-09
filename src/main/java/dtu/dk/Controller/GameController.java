package dtu.dk.Controller;

import dtu.dk.View.MainFX;

public class GameController {

    MainFX ui;

    public GameController() {
        GUIRunner.startGUI();
        try {
            ui = MainFX.getUI();
        } catch (InterruptedException e) {
            System.err.println("Could not await latch");
            throw new RuntimeException(e);
        }
    }

    public void startGame() {
        System.out.println("Game started");
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