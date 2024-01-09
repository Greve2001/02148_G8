package dtu.dk.Model;

import org.jspace.Space;

public class Peer {
    private int id;
    private Space space;

    public Peer(int id, Space space) {
        this.id = id;
        this.space = space;
    }

    public int getID() {
        return id;
    }

    public Space getSpace() {
        return space;
    }
}
