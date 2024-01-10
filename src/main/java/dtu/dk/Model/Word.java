package dtu.dk.Model;

public class Word {
    private String text;
    private double fallDuration = 5.0; // seconds to reach the bottom

    public Word(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public double getFallDuration() {
        return fallDuration;
    }

}
