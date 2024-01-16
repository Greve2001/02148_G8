package dtu.dk.View;

import dtu.dk.FxWordsToken;
import dtu.dk.GameConfigs;
import dtu.dk.Model.Word;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainFX extends Application implements GUIInterface {
    // Used to prevent the program from continuing before the stage is shown
    private static CountDownLatch latch = new CountDownLatch(1);
    private static MainFX ui;
    // Used to change scene
    private static AnchorPane pane;
    private static Scene scene;
    private static Stage stage;
    private final Pane[][] hearts = new Pane[5][3];
    // Keylogger space
    SequentialSpace fxWords = new SequentialSpace();
    private Label[] playerNames = new Label[4];
    private Label prompt;
    private VBox textPane;
    private Pane wordPane;
    private Label streak;
    private HBox hBoxLastWord;
    private List<Word> wordsFalling;

    public static void startFX() {
        launch();
    }

    public static MainFX getUI() throws InterruptedException {
        latch.await();
        return ui;
    }

    @Override
    public void start(Stage primaryStage) {
        // Setting up the stage
        stage = primaryStage;
        primaryStage.setTitle("Word Wars!");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(GameConfigs.JAVA_FX_INTRO));
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene = new Scene(pane, 1280, 720);
        stage.setResizable(false);
        //scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(GameConfigs.JAVA_FX_CSS);


        // Setting up keylogger
        stage.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            try {
                String key = event.getText();
                if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
                    key = "";
                    fxWords.put(FxWordsToken.TYPED, prompt.getText());
                    prompt.setText(key);
                } else if (event.getCode() == KeyCode.BACK_SPACE) {
                    if (prompt.getText().length() > 0) {
                        prompt.setText(prompt.getText().substring(0, prompt.getText().length() - 1));
                    } else {
                        prompt.setText("");
                    }
                } else if (event.getCode() == KeyCode.INSERT) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    prompt.setText(clipboard.getString());
                } else {
                    prompt.setText(prompt.getText() + key);
                }
                String currentInput = prompt.getText();
                //update elemets on wordPane
                if (this.wordPane != null) {
                    for (Node node : wordPane.getChildren()) {
                        if (!currentInput.isEmpty() && ((getWordFromHBox((HBox) node).toLowerCase().startsWith(String.valueOf(currentInput.charAt(0)).toLowerCase())))) {
                            updateWordColor((HBox) node, currentInput);
                        } else {
                            resetWordColor((HBox) node);
                        }
                    }
                }

                // and last word
                if (hBoxLastWord != null) {
                    if (!currentInput.isEmpty() && ((getWordFromHBox(hBoxLastWord).startsWith(String.valueOf(currentInput.charAt(0)))))) {
                        updateWordColor(hBoxLastWord, currentInput);
                    } else {
                        resetWordColor(hBoxLastWord);
                    }
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        stage.onCloseRequestProperty().setValue(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Showing the stage
        primaryStage.setScene(scene);
        primaryStage.show();
        setPointers();
        this.ui = this;
        latch.countDown();
    }

    private void updateWordColor(HBox wordBox, String currentInput) {
        if (currentInput.length() <= wordBox.getChildren().size()) {
            ((Label) wordBox.getChildren().get(0)).setTextFill(Color.GREEN);
            for (int i = 0; i < wordBox.getChildren().size(); i++) {
                Label letter = (Label) wordBox.getChildren().get(i);
                if (i < currentInput.length()) {
                    if (letter.getText().equalsIgnoreCase(String.valueOf(currentInput.charAt(i)))) {
                        letter.setTextFill(Color.GREEN);
                    } else {
                        letter.setTextFill(Color.RED);

                    }
                } else {
                    letter.setTextFill(Color.WHITE);
                }
            }
        } else {
            for (Node node : wordBox.getChildren()) {
                if (node instanceof Label) {
                    ((Label) node).setTextFill(Color.RED);
                }
            }
        }
    }

    private void resetWordColor(HBox wordBox) {
        for (Node node : wordBox.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.WHITE);
            }
        }
    }


    @Override
    public void changeScene(String fxml) {
        System.out.println("Setting latch");
        latch = new CountDownLatch(1);
        System.out.println("Changing scene to: " + fxml);
        Platform.runLater(() -> {
            System.out.println("FX: Changing scene to: " + fxml);
            FXMLLoader loader = new FXMLLoader(MainFX.class.getClassLoader().getResource(fxml));
            AnchorPane pane;
            try {
                pane = loader.load();
            } catch (IOException e) {
                System.err.println("Could not load fxml file: " + fxml);
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(pane, 1280, 720);
            scene.getStylesheets().add(GameConfigs.JAVA_FX_CSS);
            stage.setScene(scene);
            //scene.setCursor(Cursor.NONE);
            stage.setResizable(false);
            stage.show();
            MainFX.pane = pane;
            MainFX.scene = scene;
            setPointers();
            latch.countDown();
            System.out.println("FX: Changed scene to: " + fxml);
        });
    }

    private void setPointers() {
        System.out.println("FX: Setting pointers");
        prompt = (Label) pane.lookup("#prompt");
        if (prompt != null)
            prompt.setText("");

        textPane = (VBox) pane.lookup("#textPane");
        if (textPane != null)
            textPane.getChildren().clear();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                hearts[i][j] = (Pane) pane.lookup("#heart_" + i + "_" + j);
            }
        }

        for (int i = 0; i < 4; i++) {
            playerNames[i] = (Label) pane.lookup("#playerName_" + i);
        }

        wordPane = (Pane) pane.lookup("#wordPane");
        if (wordPane != null)
            //wordPane.getChildren().clear();

            streak = (Label) pane.lookup("#streak");
        if (streak != null)
            streak.setText("0");

        System.out.println("FX: finding hBoxLastWord");

        hBoxLastWord = (HBox) pane.lookup("#hBoxLastWord");
        System.out.println("FX: found hBoxLastWord");
        if (hBoxLastWord != null)
            Platform.runLater(() -> updateLastWord(""));

        System.out.println("FX: Set pointers done");
    }

    public void setSpace(SequentialSpace space) {
        this.fxWords = space;
    }

    public void setWordsFallingList(List<Word> wordsFalling) {
        this.wordsFalling = wordsFalling;
    }


    @Override
    public CountDownLatch getLatch() {
        return latch;
    }

    @Override
    public void addTextToTextPane(String text) {
        awaitLatch();
        Platform.runLater(() -> {
            Label label = new Label(text);
            label.getStyleClass().add("textOnPane");
            textPane.getChildren().add(label);
        });
    }

    @Override
    public void changeNewestTextOnTextPane(String text) {
        awaitLatch();
        Platform.runLater(() -> {
            Label label = new Label(text);
            textPane.getChildren().remove(textPane.getChildren().size() - 1);
            label.getStyleClass().add("textOnPane");
            textPane.getChildren().add(label);
        });
    }

    /**
     * Updates the life of a player
     * player -2 = behind player -1
     * player -1 = behind me
     * player 0 = me
     * player 1 = infront of me
     * player 2 = infront of player 1
     *
     * @param player
     * @param life   emount of life for player [0:3]
     */
    public void updateLife(int player, int life) throws NullPointerException {
        awaitLatch();
        if (hearts[0][0] == null)
            throw new NullPointerException("hearts not initialized/found");
        int p = player + 2;

        Platform.runLater(() -> {
            for (int i = 0; i < 3; i++) {
                if (i < life) {
                    hearts[p][i].setVisible(true);
                } else {
                    hearts[p][i].setVisible(false);
                }
            }
        });
    }

    public void updatePlayerName(int player, String name) throws NullPointerException {
        awaitLatch();
        if (playerNames[0] == null)
            throw new NullPointerException("playerNames not initialized/found");

        if (player == 0)
            throw new NullPointerException("player 0 have no name");
        if (player > 2 || player < -2)
            throw new NullPointerException("No player " + player + " exists");
        switch (player) {
            case -2 -> player = 0;
            case -1 -> player = 1;
            case 1 -> player = 2;
            case 2 -> player = 3;
            default -> throw new NullPointerException("No player " + player + " exists");
        }
        int p = player;
        Platform.runLater(() -> {
            playerNames[p].setText(name);
        });
    }

    /**
     * Makes a word fall from the top of the screen to the bottom
     * When the word hits the bottom of the screen it is removed from the screen
     * and the word is added to the fxWords space with the token HIT
     * if the word is not in the wordsFalling list it is added to the list
     *
     * @param word
     */
    public void makeWordFall(Word word) {
        awaitLatch();
        if (wordPane == null)
            throw new NullPointerException("wordPane not initialized/found");
        if (!wordsFalling.contains(word)) {
            wordsFalling.add(word);
        }
        Platform.runLater(() -> {
            HBox wordBox = new HBox();
            wordBox.setId(word.getText());
            for (char letter : word.getText().toCharArray()) {
                Label letterLabel = new Label(String.valueOf(letter));
                letterLabel.getStyleClass().add("fallingLetter");
                wordBox.getChildren().add(letterLabel);
            }
            String currentInput = prompt.getText();
            if (!currentInput.isEmpty() && ((getWordFromHBox(wordBox).toLowerCase().startsWith(String.valueOf(currentInput.charAt(0)).toLowerCase())))) {
                updateWordColor(wordBox, currentInput);
            } else {
                resetWordColor(wordBox);
            }

            wordPane.getChildren().add(wordBox);
            int x = (int) (Math.random() * (wordPane.getWidth() - wordBox.getWidth()));
            wordBox.setLayoutX(x);
            TranslateTransition transition = new TranslateTransition();
            transition.setDuration(Duration.seconds(word.getFallDuration()));
            transition.setNode(wordBox);
            transition.setToY(wordPane.getHeight() - 24);
            transition.setInterpolator(javafx.animation.Interpolator.LINEAR);
            transition.setOnFinished(e -> {
                wordPane.getChildren().remove(wordBox);
                wordsFalling.remove(word);
                try {
                    fxWords.put(FxWordsToken.HIT, word.getText());
                } catch (InterruptedException ex) {
                    System.err.println("Could not put word.getText() in fxWords space");
                    throw new RuntimeException(ex);
                }
            });
            word.setTranslateTransition(transition);
            transition.play();
        });

    }

    /**
     * Removes a word from the screen
     * if the word is in the wordsFalling list it is removed from the list
     *
     * @param word
     */
    public void removeWordFalling(Word word) {
        awaitLatch();
        if (wordPane == null)
            throw new NullPointerException("wordPane not initialized/found");
        Platform.runLater(() -> {
                    List<Node> children = wordPane.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        Node node = children.get(i);
                        if (getWordFromHBox((HBox) node).equals(word.getText())) {
                            wordPane.getChildren().remove(node);
                            if (wordsFalling.contains(word))
                                wordsFalling.remove(word);
                            word.getTranslateTransition().stop();
                            break;
                        }
                    }
                }
        );
    }

    public String getWordFromHBox(HBox wordBox) {
        StringBuilder word = new StringBuilder();
        for (Node node : wordBox.getChildren()) {
            word.append(((Label) node).getText());
        }
        return word.toString();
    }

    public void updateStreak(int streak) throws NullPointerException {
        awaitLatch();
        if (this.streak == null)
            throw new NullPointerException("streak not initialized/found");
        Platform.runLater(() -> {
            this.streak.setText("" + streak);
        });
    }

    public void updateLastWord(String word) throws NullPointerException {
        awaitLatch();
        if (hBoxLastWord == null)
            throw new NullPointerException("hBoxLastWord not initialized/found");
        Platform.runLater(() -> {
            hBoxLastWord.getChildren().clear();
            for (char letterChar : word.toCharArray()) {
                Label letterLabel = new Label(String.valueOf(letterChar));
                letterLabel.getStyleClass().add("fallingLetter");
                hBoxLastWord.getChildren().add(letterLabel);
            }
        });
    }

    private void awaitLatch() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class KeyPrinter implements Runnable {
        public void run() {
            while (true) {
                try {
                    String key = (String) fxWords.get(new FormalField(String.class))[0];
                    System.out.println(key);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

