package dtu.dk;


import dtu.dk.Controller.WordCreator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class WordCreatorTest {
    List<String> words;

    @Before
    public void setup() {
        words = WordCreator.getWords();
    }
    @Test
    public void noWordsWithWhitespace() {
        boolean noWordsWithWhitespace = true;

        for (String word : words) {
            if (word.contains(" ")) {
                noWordsWithWhitespace = false;
                break;
            }
        }
        assertTrue(noWordsWithWhitespace);
    }

    public static void main(String[] args) {
        List<String> words = WordCreator.getSubset(1000);
        System.out.println(words);
    }
}
