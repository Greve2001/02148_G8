package dtu.dk.Controller;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class WordCreator {

    public static List<String> makeWords()  {
        InputStream inputStream = WordCreator.class.getClassLoader().getResourceAsStream("words.csv");

        Scanner scanner = new Scanner(inputStream).useDelimiter(",");
        List<String> words = new ArrayList<>();

        while (scanner.hasNext()) {
            words.add(scanner.next());
        }
        Collections.shuffle(words);

        return words;
    }
}
