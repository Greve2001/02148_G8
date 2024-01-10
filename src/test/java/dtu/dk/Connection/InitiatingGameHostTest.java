package dtu.dk.Connection;

import dtu.dk.Controller.GameController;
import dtu.dk.Controller.SetupController;
import dtu.dk.Exceptions.NoGameSetupException;

public class InitiatingGameHostTest {
    public static void main(String[] args) throws NoGameSetupException {
        SetupController setupController = new SetupController();

        String localIP = "localhost";
        String localPort = "31126";
        String initiatorPort = "31125";
        setupController.host(localIP, localPort, localIP, initiatorPort);
    }
}