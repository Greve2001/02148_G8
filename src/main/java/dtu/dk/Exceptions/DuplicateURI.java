package dtu.dk.Exceptions;

public class DuplicateURI extends Exception {
    public String uri;
    public String message;

    public DuplicateURI(String message, String uri) {
        this.message = message;
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
