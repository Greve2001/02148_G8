package dtu.dk.Controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class WordCreator {
    private static final String wordFile = "words.csv";

    public static List<String> getWords() {
        InputStream inputStream = WordCreator.class.getClassLoader().getResourceAsStream(wordFile);

        Scanner scanner = new Scanner(inputStream);
        List<String> words = new ArrayList<>();

        while (scanner.hasNext()) {
            words.add(scanner.next());
        }

        return words;
    }

    public static List<String> getSubset(int size) {
        List<String> words = WordCreator.getWords();
        Random rng = new Random();

        List<String> subset = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int index = rng.nextInt(words.size()-1)+1;
            subset.add(words.get(index));
        }

        return subset;
    }
}
