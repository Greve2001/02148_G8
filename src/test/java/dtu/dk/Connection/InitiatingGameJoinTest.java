package dtu.dk.Connection;

import dtu.dk.Controller.GameController;
import dtu.dk.Controller.SetupController;
import dtu.dk.Exceptions.NoGameSetupException;
import org.jspace.SpaceRepository;

public class InitiatingGameJoinTest {
    public static void main(String[] args) throws NoGameSetupException {
        GameController gameController = new GameController();
        SetupController setupController = new SetupController(gameController);

        String localIP = "localhost";
        String localPort = "31127";
        setupController.join(localIP, localPort, localIP);
    }
}
