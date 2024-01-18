package dtu.dk.Model;

import dtu.dk.GameConfigs;
import dtu.dk.WordType;
import javafx.animation.TranslateTransition;

public class Word {
    private String text;
    private TranslateTransition tt;
    private double fallDuration = 6.0; // seconds to reach the bottom
    private static boolean fallSlowStatic = false;
    private WordType type;

    public Word(String text) {
        this.text = text;
        type = getATypeAtRandom();
        fallDuration = 1 + text.length() * 1.5;
    }

    public static void setFalingSlow(boolean b) {
        fallSlowStatic = b;
    }

    public static boolean isSlowFalling() {
        return fallSlowStatic;
    }

    public String getText() {
        return text;
    }

    public double getFallDuration() {
        if (fallSlowStatic) {
            return fallDuration + GameConfigs.FALL_SLOW_TIME;
        }
        return fallDuration;
    }

    public TranslateTransition getTranslateTransition() {
        return tt;
    }

    public void setTranslateTransition(TranslateTransition tt) {
        this.tt = tt;
    }

    private WordType getATypeAtRandom() {
        double r = Math.random();
        if (r < GameConfigs.EFFECT_WORD_CHANCE) {
            int temp = (int) (Math.random() * 2);
            switch (temp) {
                case 0:
                    return WordType.EXRTA_LIFE;
                case 1:
                    return WordType.FALL_SLOW;
                default:
                    System.err.println("Word: Unknown word type");
                    return WordType.NORMAL;
            }
        } else {
            return WordType.NORMAL;
        }
    }

    public WordType getType() {
        return type;
    }
}
