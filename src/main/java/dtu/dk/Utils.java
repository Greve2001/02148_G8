package dtu.dk;

import dtu.dk.Model.Word;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class Utils {
    public static Integer[] IntegerListToArray(List<Integer> list) {
        Integer[] arr = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    public static Word[] WordListToArray(List<Word> list) {
        Word[] arr = new Word[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    public static String[] StringListToArray(List<String> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    public static String getLocalIPAddress() {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345);

            return datagramSocket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            System.err.println("Could not find local IP");
            return "";
        }
    }
}
