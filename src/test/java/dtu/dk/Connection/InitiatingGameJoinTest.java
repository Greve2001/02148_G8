package dtu.dk.Connection;

import dtu.dk.Controller.GameController;
import dtu.dk.Controller.SetupController;

public class InitiatingGameJoinTest {
    public static void main(String[] args) {
        GameController gameController = new GameController();
        SetupController setupController = new SetupController(gameController);

        String localIP = "10.209.189.18";
        String localPort = "31127";
        setupController.join(localIP, localPort, localIP);
    }
}
