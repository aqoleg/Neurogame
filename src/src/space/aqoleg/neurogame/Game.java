package space.aqoleg.neurogame;

import java.io.File;

public class Game {
    private final File file = new File(new File(System.getProperty("user.home"), "Documents"), "neurogame.sv");
    private final Field field;
    private final Brain brain;
    private final Frame frame;

    private Game() {
        field = new Field();
        brain = new Brain(field);
        frame = new Frame(this, field);
    }

    public static void main(String[] args) {
        new Game().initialize();
    }

    void start() {
        field.start();
        brain.perceive();
        frame.printField();
        printAnswers();
    }

    void save() {
        if (brain.save(file)) {
            frame.printMessage("Saved in " + file.getPath());
        }
    }

    void load() {
        if (brain.load(file)) {
            brain.perceive();
            frame.printField();
            printAnswers();
        }
    }

    void move(int direction) {
        if (!field.play(direction)) {
            if (field.areLoose()) {
                frame.printMessage("Loose");
            } else {
                frame.printMessage("Try other direction");
            }
        } else {
            brain.add(direction);
            brain.perceive();
            frame.printField();
            printAnswers();
        }
    }

    void learn() {
        brain.learn();
    }

    private void initialize() {
        frame.createAndShowGUI();
        if (!brain.load(file)) {
            brain.initialize();
            start();
        } else {
            brain.perceive();
            printAnswers();
        }
        frame.printField();
    }

    private void printAnswers() {
        String msg = "";
        switch (brain.getDirection()) {
            case Field.DOWN:
                msg = "D";
                break;
            case Field.LEFT:
                msg = "L";
                break;
            case Field.RIGHT:
                msg = "R";
                break;
            case Field.UP:
                msg = "U";
                break;
        }
        frame.printMessage(msg + ": D" + brain.getAnswer(0) + ", L" + brain.getAnswer(1) +
                ", R" + brain.getAnswer(2) + ", U" + brain.getAnswer(3));
    }
}