package dtu.dk.Controller;

import dtu.dk.Exceptions.DuplicateURI;
import dtu.dk.Exceptions.PortNotAvailable;
import dtu.dk.Protocol;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static dtu.dk.Protocol.*;

public class ConnectionHandler implements Runnable {
    List<String> playerURIs;
    Space space;

    public ConnectionHandler(Space space, List<String> playerURIs) {
        this.playerURIs = playerURIs;
        this.space = space;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String uri = "";
                try {
                    uri = listenForPeerURI();
                    if (!uri.isEmpty()) sendPeerProtocol(uri, CONNECT_ACCEPTED);
                } catch (DuplicateURI e) {
                    System.err.println(e.getMessage());
                    sendPeerProtocol(e.uri, CONNECT_DUPLICATE_URI);
                } catch (PortNotAvailable e) {
                    System.err.println(e.getMessage());
                    sendPeerProtocol(e.uri, CONNECT_PORT_NOT_AVAILABLE);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(Initiator.errorSpaceNotAvailable);
        }
    }

    private String listenForPeerURI() throws InterruptedException, DuplicateURI, PortNotAvailable {
        String peerURI = (String) space.get(new ActualField(CONNECT), new FormalField(String.class))[1];
        if (!checkPort(peerURI) {
            throw new PortNotAvailable("Port is not available", peerURI);
        }
        if (!playerURIs.contains(peerURI)) {
            playerURIs.add(peerURI);
            System.out.println("Initiator: Peer connected: " + peerURI);
            return peerURI;

        } else {
            throw new DuplicateURI("Peer already connected", peerURI);
        }
    }

    /**
     * Checks if the port is available
     *
     * @param peerURI URI of the peer
     * @return true if the port is available, false if not
     */
    private boolean checkPort(String peerURI) {
        String[] splitURI = peerURI.split(":");
        String port = splitURI[2].split("/")[0];
        String ip = splitURI[1].substring(2);

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 3000);
            socket.close();
        } catch (ConnectException e) {
            return false; // port open, local repo is not running
        } catch (IOException e) {
            return false; // port closed
        }
        return true;
    }

    private void sendPeerProtocol(String uri, Protocol protocol) {
        try {
            space.put(CONNECTED, protocol, uri);
            System.out.println("Initiator sent " + protocol + " to peer: " + uri);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
