package dtu.dk;

import dtu.dk.Controller.GameController;

public class Main {
    public static void main(String[] args) {
        GameController gameController;
        String port;
        if (args.length == 1) {
            port = args[0];
            gameController = new GameController(port);
        } else {
            gameController = new GameController();
        }
        gameController.startGame();
    }
}

