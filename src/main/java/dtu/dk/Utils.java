package dtu.dk;

import java.util.List;

public class Utils {
    public static Integer[] IntegerListToArray(List<Integer> list) {
        Integer[] arr = new Integer[list.size()];
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
}
