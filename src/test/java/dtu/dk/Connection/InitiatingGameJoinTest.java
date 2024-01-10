package dtu.dk.Connection;

import dtu.dk.Controller.GameController;
import dtu.dk.Controller.SetupController;
import dtu.dk.Exceptions.NoGameSetupException;

public class InitiatingGameJoinTest {
    public static void main(String[] args) throws NoGameSetupException {
        GameController gameController = new GameController();
        SetupController setupController = new SetupController(gameController);

        String localIP = "localhost";
        String localPort = "31127";
        String initiatorPort = "31125";
        setupController.join(localIP, localPort, localIP, initiatorPort);
    }
}
