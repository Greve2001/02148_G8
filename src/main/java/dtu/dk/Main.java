package dtu.dk;

import dtu.dk.View.MainFX;

import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;

public class Main {
    public static CountDownLatch latch = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException {
        GUIStarter.startGUI();
        latch.await();
        MainFX.changeScene("gameScreen.fxml");
    }
}

class GUIStarter implements Runnable{
    public void run() {
        MainFX.startFX();
    }

    public static void startGUI(){
        new Thread(new GUIStarter()).start();

    }
}
