package dtu.dk;

public class GameConfigs {
    // Default game values
    public static final int wordsInPLay = 1000;
    public static final int REQUIRED_STREAK = 10;
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
    public static final String GET_USERNAME = "Please enter your username";
    public static final String GET_USERNAME_INVALID = "Invalid username - ";
    public static final String GET_HOST_IP = "Please enter the host IP";
    public static final String GET_LOCAL_IP = "Is this your IP address? ";
    public static final String GET_LOCAL_IP_IF_NOT = " if not type your IP address";
    public static final String GET_LOCAL_IP_Y = "y";
    public static final String GET_LOCAL_IP_YES = "yes";
    public static final String GET_LOCAL_IP_Y_YES = "(" + GET_LOCAL_IP_Y + "/" + GET_LOCAL_IP_YES + ")";
    public static final String GET_LOCAL_IP_INVALID = "Invalid - ";
    public static final String REGEX_IP = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";


    /*
     *************** SetupController ***************
     */

    // Ports
    public static final String INIT_PORT = "31125";
    public static final String DEFAULT_PORT_JOIN = "31127";
    public static final String DEFAULT_PORT_HOST = "31126";


}
