package dtu.dk.Model;

import dtu.dk.GameConfigs;
import dtu.dk.WordType;
import javafx.animation.TranslateTransition;

public class Word {
    private String text;
    private TranslateTransition tt;
    private double fallDuration = 6.0; // seconds to reach the bottom
    private WordType type;

    public Word(String text) {
        this.text = text;
        type = getATypeAtRandom();
    }

    public String getText() {
        return text;
    }

    public double getFallDuration() {
        return 1 + text.length() * 1.5;
    }

    public void setTranslateTransition(TranslateTransition tt) {
        this.tt = tt;
    }

    public TranslateTransition getTranslateTransition() {
        return tt;
    }

    private WordType getATypeAtRandom() {
        double r = Math.random();
        if (r < GameConfigs.EFFECT_WORD_CHANCE) {
            return WordType.EXRTA_LIFE;
        } else {
            return WordType.NORMAL;
        }
    }

    public WordType getType() {
        return type;
    }
}
