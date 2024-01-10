package dtu.dk;


import dtu.dk.Controller.WordCreator;
import dtu.dk.Model.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class WordCreatorTest {
    List<Word> words;

    @Before
    public void setup() {
        words = WordCreator.getWords();
    }
    @Test
    public void noWordsWithWhitespace() {
        boolean noWordsWithWhitespace = true;

        for (Word word : words) {
            if (word.getText().contains(" ")) {
                noWordsWithWhitespace = false;
                break;
            }
        }
        assertTrue(noWordsWithWhitespace);
    }
}
