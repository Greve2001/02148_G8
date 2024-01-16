package dtu.dk.Controller;

import dtu.dk.View.MainFX;

public class GUIRunner implements Runnable {
    public static void startGUI() {
        new Thread(new GUIRunner()).start();
    }

    public void run() {
        MainFX.startFX();
    }
}