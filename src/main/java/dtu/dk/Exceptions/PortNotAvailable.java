package dtu.dk.Exceptions;

public class PortNotAvailable extends Exception {
    public String uri;

    public PortNotAvailable(String message, String uri) {
        super(message);
        this.uri = uri;
    }
}
