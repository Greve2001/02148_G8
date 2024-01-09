package dtu.dk;

import dtu.dk.View.MainFX;

import static java.lang.Thread.sleep;

public class Main {
    public static Thread guiThread;
    public static void main(String[] args) throws InterruptedException {
        guiThread = new Thread(new GUIStarter());
        guiThread.start();
        sleep(3000);
        MainFX.changeScene("gameScreen.fxml");
    }
}

class GUIStarter implements Runnable{
    public void run() {
        MainFX.main(new String[0]);
    }
}
