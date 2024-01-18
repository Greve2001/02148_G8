package dtu.dk.Model;

public class Player {
    private String username = "";
    private int lives = 3;
    private int score = 0;
    private int streak = 0;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void addLife() {
        if (this.lives < 3)
            this.lives++;
    }

    public void loseLife() {
        if (this.lives == 0) return;
        this.streak = 0;
        this.lives--;
    }

    public int getStreak() {
        return streak;
    }

    public void zeroStreak() {
        this.streak = 0;
    }

    public void increaseStreak() {
        this.streak++;
    }
}
