package dtu.dk.Connection;

import dtu.dk.Controller.GameController;
import dtu.dk.Controller.SetupController;

public class InitiatingGameHostTest {
    public static void main(String[] args) {
        GameController gameController = new GameController();
        SetupController setupController = new SetupController(gameController);

        String localIP = "localhost";
        String localPort = "31126";
        String initiatorPort = "31125";
        setupController.host(localIP, localPort, localIP, initiatorPort);
    }
}