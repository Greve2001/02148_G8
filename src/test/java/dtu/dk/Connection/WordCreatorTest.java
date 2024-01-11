package dtu.dk.Connection;


import dtu.dk.Controller.WordCreator;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WordCreatorTest {
    public static void main(String[] args) {
        WordCreator wordCreator = new WordCreator();
        List<String> words = wordCreator.makeWords();
        assertTrue(words.get(0) instanceof String);
        assertFalse(words.isEmpty());

    }
}
