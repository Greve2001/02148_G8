package dtu.dk;

import dtu.dk.View.MainFX;

import static java.lang.Thread.sleep;

public class Main {
    public static Thread guiThread;
    public static void main(String[] args) throws InterruptedException {
        GUIStarter.startGUI();
        MainFX.changeScene("gameScreen.fxml");
    }
}

class GUIStarter implements Runnable{
    public void run() {
        MainFX.main(new String[0]);
    }

    public static void startGUI(){
        new Thread(new GUIStarter()).start();
        while (MainFX.getStage() == null){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
