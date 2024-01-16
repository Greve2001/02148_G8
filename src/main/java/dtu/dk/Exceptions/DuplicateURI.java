package dtu.dk.Exceptions;

public class DuplicateURI extends Exception {
    public String uri;

    public DuplicateURI(String message, String uri) {
        super(message);
        this.uri = uri;
    }
}
