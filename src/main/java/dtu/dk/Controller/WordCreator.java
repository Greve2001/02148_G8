package dtu.dk.Controller;

import dtu.dk.Model.Word;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class WordCreator {
    private static final String wordFile = "words.csv";

    public static List<Word> getWords() {
        InputStream inputStream = WordCreator.class.getClassLoader().getResourceAsStream(wordFile);

        Scanner scanner = new Scanner(inputStream);
        List<Word> words = new ArrayList<>();

        while (scanner.hasNext()) {
            words.add(new Word(scanner.next()));
        }

        return words;
    }

    public static List<Word> getSubset(int size) {
        List<Word> words = WordCreator.getWords();
        Random rng = new Random();

        List<Word> subset = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int index = rng.nextInt(size);
            subset.add(words.get(index));
        }

        return subset;
    }
}
