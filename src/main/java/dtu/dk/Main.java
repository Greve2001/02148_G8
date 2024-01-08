package dtu.dk;

public class Main {
    public static Thread guiThread;
    public static void main(String[] args) {
        guiThread = new Thread(new GUIStarter());
        guiThread.start();
    }
}

class GUIStarter implements Runnable{
    public void run() {
        MainFX.main(new String[0]);
    }
}
