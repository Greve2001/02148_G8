package dtu.dk;


import dtu.dk.Controller.WordCreator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class WordCreatorTest {
    List<String> words;

    @Before
    public void setup() {
        words = WordCreator.getWords();
    }
    @Test
    public void noWordsWithWhitespace() {
        for (String word : words) {
            if (word.contains(" "))
                assert false;
        }
        assert true;
    }

    public static void main(String[] args) {
        List<String> words = WordCreator.getSubset(1000);
        System.out.println(words);
    }
}
