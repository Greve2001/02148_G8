package dtu.dk.Model;

import org.jspace.Space;

public class Peer {
    private int id;


    private Space space;

    private String URI;

    public Peer(int id, Space space, String URI) {
        this.id = id;
        this.space = space;
        this.URI = URI;
    }

    public String getURI(){
        return this.URI;
    }
    public int getID() {
        return id;
    }

    public Space getSpace() {
        return space;
    }
    public void setSpace(Space space) {
        this.space = space;
    }
}
