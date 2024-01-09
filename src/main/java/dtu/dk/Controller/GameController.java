package dtu.dk.Controller;

import dtu.dk.View.MainFX;

public class GameController {

    MainFX ui;

    public GameController() {
        ui = new MainFX();
        new Thread(() -> {
            ui.startFX();
        });
        try {
            ui.getLatch().await();
        } catch (InterruptedException e) {
            System.err.println("Could not await latch");
            throw new RuntimeException(e);
        }
    }

    public void startGame() {
        System.out.println("Game started");
    }


}
