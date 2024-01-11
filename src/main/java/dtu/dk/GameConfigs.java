package dtu.dk;

public class GameConfigs {
    // Default game values
    public static final int wordsInPLay = 1000;
    public static final int REQUIRED_STREAK = 10;
    public static final int START_SLEEP_TEMPO = 1000; // Milliseconds
    public static final int FALLEN_WORDS_BEFORE_INCREASING_TEMPO = 10;
    public static final int MIN_SLEEP_TEMPO = 50;


    // Word speed factor/ difficulty?

    /*
     *************** GameController settings ***************
     */

    // JavaFX scenes
    public static final String JAVA_FX_INTRO = "intro.fxml";
    public static final String JAVA_FX_HOST = "host.fxml";
    public static final String JAVA_FX_JOIN = "join.fxml";
    public static final String JAVA_FX_GAMESCREEN = "gameScreen.fxml";
    public static final String JAVA_FX_CSS = "nice.css";

    // getInformation() strings - regex
    public static final String JOIN = "join";
    public static final String HOST = "host";
    public static final String EXIT = "exit";
    public static final String QUIT = "quit";
    public static final String EMPTY_STRING = "";
    public static final String GET_USERNAME = "Please enter your username";
    public static final String GET_USERNAME_INVALID = "Invalid username - ";
    public static final String GET_HOST_IP = "Please enter the host IP";
    public static final String GET_LOCAL_IP = "Is this your IP address? ";
    public static final String GET_LOCAL_IP_IF_NOT = "If not type your IP address";
    public static final String GET_LOCAL_IP_Y = "y";
    public static final String GET_LOCAL_IP_YES = "yes";
    public static final String GET_LOCAL_IP_Y_YES = "(" + GET_LOCAL_IP_Y + "/" + GET_LOCAL_IP_YES + "). ";
    public static final String GET_LOCAL_IP_INVALID = "Invalid - ";
    public static final String REGEX_IP = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    public static final String TYPE_READY = "Type 'ready' to start the game";
    public static final String READY = "ready";
    public static final String UNKNOWN_CMD = "Unknown command: ";
    public static final String WAITING_FOR_PLAYERS_TO_TYPE_READY = "Waiting for other players to be ready";


    /*
     *************** SetupController ***************
     */

    // Ports
    public static final String INIT_PORT = "31125";
    public static final String DEFAULT_PORT_JOIN = "31127";
    public static final String DEFAULT_PORT_HOST = "31126";


}
