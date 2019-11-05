package space.aqoleg.neurogame;

import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BrainTest {
    private static final int[][] FIELDS = {
            {0, 3, 4, 2, 5, 6, 0, 0, 0, 1, 0, 0, 5, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            {1, 3, 3, 3, 4, 0, 0, 0, 2, 3, 0, 5, 5, 6, 8, 7}
    };
    private static final int[] SCORES = {0, 8, -2, 36};

    @Test
    void initialize() {
        Field field = new Field();
        Brain brain = new Brain(field);
        field.start();
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertEquals(0.5, brain.getAnswer(direction));
        }
        brain.initialize();
        for (int direction = 0; direction < 4; direction++) {
            float answer = brain.getAnswer(direction);
            assertNotEquals(0.5, answer);
            assertTrue(answer > 0 && answer < 1);
        }
    }

    @Test
    void saveAndLoad() throws IOException {
        Field field = new Field();
        Brain brain = new Brain(field);
        File file = new File(new File(System.getProperty("user.home"), "Documents"), "neurogame.test");
        field.start();
        brain.initialize();
        float[] answers = new float[4];
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            answers[direction] = brain.getAnswer(direction);
        }
        assertTrue(brain.save(file));

        field.start();
        field.play(Field.LEFT);
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertNotEquals(answers[direction], brain.getAnswer(direction));
        }

        assertTrue(brain.load(file));
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertEquals(answers[direction], brain.getAnswer(direction));
        }

        brain.initialize();
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertNotEquals(answers[direction], brain.getAnswer(direction));
        }

        assertTrue(brain.load(file));
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertEquals(answers[direction], brain.getAnswer(direction));
        }

        DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
        stream.write(677);
        stream.writeFloat(0.998f);
        stream.close();
        assertFalse(brain.load(file));
        assertFalse(brain.load(new File("neurogame.tmp")));
        assertTrue(brain.save(file));

        assertTrue(file.delete());
    }

    @Test
    void perceive() {
        Brain brain = new Brain(new TestField());
        brain.initialize();
        brain.perceive();
        float[] out = new float[4];
        for (int direction = 0; direction < 4; direction++) {
            out[direction] = brain.getAnswer(direction);
            assertTrue(out[direction] > 0 && out[direction] < 1);
        }
        brain.initialize();
        brain.perceive();
        for (int direction = 0; direction < 4; direction++) {
            assertNotEquals(out[direction], brain.getAnswer(direction));
        }
    }

    @Test
    void learn() {
        Brain brain = new Brain(new TestField());
        brain.initialize();
        brain.perceive();
        float[] out = new float[4];
        for (int direction = 0; direction < 4; direction++) {
            out[direction] = brain.getAnswer(direction);
        }
        brain.add(3);
        brain.learn();
    }

    private class TestField extends Field {

        @Override
        int getNextFieldCell(int direction, int cellN) {
            return FIELDS[direction][cellN];
        }

        @Override
        int getScore(int direction) {
            return SCORES[direction];
        }
    }
}