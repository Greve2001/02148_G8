package dtu.dk.Model;

import javafx.animation.TranslateTransition;

public class Word {
    private String text;
    private TranslateTransition tt;
    private double fallDuration = 6.0; // seconds to reach the bottom

    public Word(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public double getFallDuration() {
        return fallDuration;
    }

    public void setTranslateTransition(TranslateTransition tt) {
        this.tt = tt;
    }

    public TranslateTransition getTranslateTransition() {
        return tt;
    }

}
