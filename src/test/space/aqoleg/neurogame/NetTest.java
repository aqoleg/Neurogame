package space.aqoleg.neurogame;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NetTest {
    private static final float[][] INPUTS = {
            {0.0098f, 0.98f, 0.12f, 0.4f},
            {0.992f, 0.0114f, 0.546f, 0.6f},
            {0.333f, 0.761f, 0.755f, 0.4f},
            {0.621f, 0.431f, 0.115f, 0.005f},
            {0.31f, 0.1f, 0.25f, 0.65f},
            {0.119f, 0.771f, 0.15f, 0.5f}
    };
    private static final float[] TARGETS = {0.09f, 0.87f, 0.15f, 0.55f, 0.08f, 0.55f};
    private static final float[][] BIG_INPUTS = {
            {1, 0, 0, 0, 0, 0, 1, 0.5f, 0.25f, 0, 0, 0, 0, 0, 0.125f, 0, 0.33f},
            {0, 0.125f, 0.125f, 0, 0, 0, 0.25f, 0.25f, 0, 1, 0, 0, 0, 0.125f, 0, 0, 0.0433f}
    };

    @Test
    void get() {
        assertThrows(ExceptionInInitializerError.class,
                () -> Net.getNet(new int[]{2}));
        assertThrows(ExceptionInInitializerError.class,
                () -> Net.getNet(new int[]{9, 1}));
        Net net = Net.getNet(new int[]{9, 2});
        float[] weights = getWeights(net);
        assertEquals(23, weights.length);
        for (float weight : weights) {
            assertEquals(0, weight);
        }
    }

    @Test
    void initialize() {
        Net net = Net.getNet(new int[]{4, 5});
        for (int set = 0; set < 6; set++) {
            assertEquals(0.5, net.getAnswer(INPUTS[set]));
        }
        net.initialize();
        for (int set = 0; set < 6; set++) {
            float answer = net.getAnswer(INPUTS[set]);
            assertNotEquals(0.5, answer);
            assertTrue(answer > 0 && answer < 1);
        }
        float[] weights = getWeights(net);
        assertEquals(31, weights.length);
        for (float weight : weights) {
            assertTrue(weight > -0.5 && weight < 0.5);
        }
    }

    @Test
    void saveAndLoad() throws IOException {
        Net net = Net.getNet(new int[]{4, 5, 3});
        net.initialize();
        float[] weights = getWeights(net);
        byte[] state = save(net);
        float answer = net.getAnswer(INPUTS[1]);
        net.initialize();
        assertNotEquals(answer, net.getAnswer(INPUTS[1]));
        load(net, state);
        assertEquals(answer, net.getAnswer(INPUTS[1]));
        float[] weights1 = getWeights(net);
        assertArrayEquals(weights1, weights);
        assertArrayEquals(state, save(net));
    }

    @Test
    void getAnswer() {
        Net net = Net.getNet(new int[]{4, 9});
        net.initialize();
        float[] out = new float[6];
        for (int set = 0; set < 6; set++) {
            out[set] = net.getAnswer(INPUTS[set]);
            assertTrue(out[set] > 0 && out[set] < 1);
        }
        net.initialize();
        for (int set = 0; set < 6; set++) {
            assertNotEquals(out[set], net.getAnswer(INPUTS[set]));
        }
        assertEquals(0, net.getAnswer(new float[]{9, 9}));
    }

    @Test
    void learn() {
        Net net = Net.getNet(new int[]{4, 20, 8, 4});
        assertEquals(0, net.learn(new float[]{9, 8}, 0.9f, 0.6f, 500, 0.01f));
        learnNet(new int[]{4, 20, 8, 4});
        learnNet(new int[]{4, 8});
        learnNet(new int[]{4, 8, 5});
        learnNet(new int[]{4, 60, 40, 12});
    }

    @Test
    void big() {
        Net net = Net.getNet(new int[]{17, 40, 20, 8});
        net.initialize();
        float answer0 = net.getAnswer(BIG_INPUTS[0]);
        float answer1 = net.getAnswer(BIG_INPUTS[1]);
        assertNotEquals(answer0, answer1);
        net.initialize();
        assertNotEquals(answer0, net.getAnswer(BIG_INPUTS[0]));
        assertNotEquals(answer1, net.getAnswer(BIG_INPUTS[1]));
        for (int i = 0; i < 5000; i++) {
            System.out.print("i = " + i);
            int learnCount = 0;
            for (int set = 0; set < 2; set++) {
                int n = net.learn(BIG_INPUTS[set], TARGETS[set], (10000 - i) / 10000f, 1000, 0.001f);
                learnCount += n;
                System.out.print(", " + n);
            }
            System.out.println();
            if (learnCount == 0) {
                break;
            }
        }
        for (int set = 0; set < 2; set++) {
            float answer = net.getAnswer(BIG_INPUTS[set]);
            assertTrue(Math.abs(TARGETS[set] - answer) <= 0.01);
            System.out.println("Target " + TARGETS[set] + ", answer " + answer);
        }
        System.out.println("weights");
        float[] weights = getWeights(net);
        System.out.println(Arrays.toString(weights));
        for (float weight : weights) {
            assertTrue(weight != 0 && weight > -10 && weight < 10);
        }
    }

    private byte[] save(Net net) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        net.save(dataStream);
        dataStream.close();
        return byteStream.toByteArray();
    }

    private void load(Net net, byte[] state) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(state));
        net.load(stream);
        stream.close();
    }

    private float[] getWeights(Net net) {
        float[] weights = null;
        try {
            byte[] saved = save(net);
            weights = new float[saved.length / 4];
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(saved));
            for (int i = 0; i < weights.length; i++) {
                weights[i] = stream.readFloat();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weights;
    }

    private void learnNet(int[] map) {
        System.out.println("net " + Arrays.toString(map));
        Net net = Net.getNet(map);
        net.initialize();
        for (int i = 0; i < 5000; i++) {
            System.out.print("i = " + i);
            int learnCount = 0;
            for (int set = 0; set < 6; set++) {
                int n = net.learn(INPUTS[set], TARGETS[set], (10000 - i) / 10000f, 1000, 0.01f);
                learnCount += n;
                System.out.print(", " + n);
            }
            System.out.println();
            if (learnCount == 0) {
                break;
            }
        }
        for (int set = 0; set < 6; set++) {
            float answer = net.getAnswer(INPUTS[set]);
            assertTrue(Math.abs(TARGETS[set] - answer) <= 0.01);
            System.out.println("Target " + TARGETS[set] + ", answer " + answer);
        }
        System.out.println("weights");
        float[] weights = getWeights(net);
        System.out.println(Arrays.toString(weights));
        for (float weight : weights) {
            assertTrue(weight != 0 && weight > -10 && weight < 10);
        }
    }
}