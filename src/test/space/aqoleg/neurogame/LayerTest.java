package space.aqoleg.neurogame;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LayerTest {
    @Test
    void get() {
        assertThrows(ExceptionInInitializerError.class,
                () -> Layer.getFirstLayer(1, new float[3], new float[3]));
        assertThrows(ExceptionInInitializerError.class,
                () -> Layer.getFirstLayer(3, new float[4], new float[3]));
        assertThrows(ExceptionInInitializerError.class,
                () -> Layer.getLayer(new float[1], new float[1], new float[3], new float[3]));
        assertThrows(ExceptionInInitializerError.class,
                () -> Layer.getLayer(new float[3], new float[4], new float[3], new float[3]));
        assertThrows(ExceptionInInitializerError.class,
                () -> Layer.getLayer(new float[3], new float[3], new float[4], new float[3]));
        Layer layer = Layer.getFirstLayer(4, new float[3], new float[3]);
        float[] weights = getWeights(layer);
        assertEquals(15, weights.length);
        for (float weight : weights) {
            assertEquals(0, weight);
        }
    }

    @Test
    void initialize() {
        Layer layer = Layer.getLayer(new float[9], new float[9], new float[9], new float[9]);
        layer.initialize();
        float[] weights = getWeights(layer);
        for (float weight : weights) {
            assertTrue(weight > -0.5 && weight < 0.5);
        }
    }

    @Test
    void saveAndLoad() throws IOException {
        Layer layer = Layer.getLayer(new float[5], new float[7], new float[5], new float[7]);
        layer.initialize();
        float[] weights = getWeights(layer);
        byte[] state = save(layer);
        layer = Layer.getLayer(new float[5], new float[7], new float[5], new float[7]);
        load(layer, state);
        float[] weights1 = getWeights(layer);
        assertArrayEquals(weights1, weights);
    }

    @Test
    void loadAndSave() throws IOException {
        byte[] state = new byte[]{0, 0, 0, 124, 11, 34, 7, 56, 55, 0, 1, 3, 13, 2, 3, 11, 12, 10, 14, 11, 8, 2, 9, 8};
        Layer layer = Layer.getFirstLayer(2, new float[2], new float[2]);
        load(layer, state);
        assertArrayEquals(state, save(layer));
        float[] weights = getWeights(layer);
        assertEquals(Float.intBitsToFloat(124), weights[0]);
    }

    @Test
    void setInputs() {
        float[] input = {0.9f, 0.08f};
        float[] output = {0, 0};
        Layer layer = Layer.getLayer(new float[2], new float[2], new float[2], new float[2]);
        assertFalse(layer.setInput(input));
        layer = Layer.getFirstLayer(3, new float[2], new float[2]);
        assertFalse(layer.setInput(input));
        layer = Layer.getFirstLayer(2, output, new float[2]);
        layer.initialize();
        assertTrue(layer.setInput(input));
        layer.perceive();
        float output0 = output[0];
        float output1 = output[1];
        layer.setInput(new float[]{8, 9});
        layer.perceive();
        assertNotEquals(output0, output[0]);
        assertNotEquals(output1, output[1]);
        layer.setInput(input);
        layer.perceive();
        assertEquals(output0, output[0]);
        assertEquals(output1, output[1]);
    }

    @Test
    void perceive() {
        float[] input = {2, 9};
        float[] output = {0, 0};
        Layer layer = Layer.getLayer(input, output, new float[2], new float[2]);
        layer.perceive();
        assertEquals(0.5, output[0]);
        assertEquals(0.5, output[1]);
        layer.initialize();
        layer.perceive();
        float output0 = output[0];
        float output1 = output[1];
        assertTrue(output0 > 0 && output0 < 1);
        assertTrue(output1 > 0 && output1 < 1);
        input[0] = 0.89f;
        layer.perceive();
        assertNotEquals(output0, output[0]);
        assertNotEquals(output1, output[1]);
        input[0] = 2;
        layer.perceive();
        assertEquals(output0, output[0]);
        assertEquals(output1, output[1]);
    }

    @Test
    void check() throws IOException {
        float[] input = {0.05f, 0.1f};
        float[] hidden = new float[2];
        float[] output = new float[2];
        float[] sigma = new float[2];
        float[] hiddenSigma = new float[2];

        Layer layer0 = Layer.getFirstLayer(2, hidden, hiddenSigma);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        dataStream.writeFloat(0.15f);
        dataStream.writeFloat(0.20f);
        dataStream.writeFloat(0.35f);
        dataStream.writeFloat(0.25f);
        dataStream.writeFloat(0.30f);
        dataStream.writeFloat(0.35f);
        dataStream.close();
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(byteStream.toByteArray()));
        layer0.load(inputStream);
        inputStream.close();
        float[] weights = getWeights(layer0);
        assertEquals(0.2f, weights[1]);
        assertEquals(0.25f, weights[3]);
        assertEquals(0.35f, weights[5]);

        Layer layer1 = Layer.getLayer(hidden, output, hiddenSigma, sigma);
        byteStream = new ByteArrayOutputStream();
        dataStream = new DataOutputStream(byteStream);
        dataStream.writeFloat(0.4f);
        dataStream.writeFloat(0.45f);
        dataStream.writeFloat(0.6f);
        dataStream.writeFloat(0.5f);
        dataStream.writeFloat(0.55f);
        dataStream.writeFloat(0.6f);
        dataStream.close();
        load(layer1, byteStream.toByteArray());
        weights = getWeights(layer1);
        assertEquals(0.4f, weights[0]);
        assertEquals(0.6f, weights[2]);
        assertEquals(0.55f, weights[4]);

        layer0.setInput(input);
        input[0] = 0.0009f;
        layer0.perceive();
        layer1.perceive();
        assertNotEquals(0.751365065574646, output[0]);
        assertNotEquals(0.7729284763336182, output[1]);
        input[0] = 0.05f;

        layer0.perceive();
        assertEquals(0.5932700037956238, hidden[0]);
        assertEquals(0.5968843698501587, hidden[1]);
        layer1.perceive();
        assertEquals(0.751365065574646, output[0]);
        assertEquals(0.7729284763336182, output[1]);
        sigma[0] = output[0] - 0.01f;
        sigma[1] = output[1] - 0.99f;
        layer1.learn(0.5f);
        weights = getWeights(layer1);
        assertEquals(0.3589164912700653, weights[0]);
        assertEquals(0.4086661636829376, weights[1]);
        assertEquals(0.5113012790679932, weights[3]);
        assertEquals(0.5613701343536377, weights[4]);
        assertEquals(0.0363503098487854, hiddenSigma[0]);
        layer0.learn(0.5f);
        weights = getWeights(layer0);
        assertEquals(0.14978072047233582, weights[0]);
        assertEquals(0.19956143200397491, weights[1]);
        assertEquals(0.24975115060806274, weights[3]);
        assertEquals(0.29950231313705444, weights[4]);

        layer0.perceive();
        layer1.perceive();
        assertEquals(0.05f, input[0]);

        for (int i = 0; i < 1000; i++) {
            sigma[0] = output[0] - 0.01f;
            sigma[1] = output[1] - 0.99f;
            layer1.learn(0.5f);
            layer0.learn(0.5f);
            layer0.perceive();
            layer1.perceive();
        }
        assertEquals(0.0440303310751915, output[0]);
        assertEquals(0.9573231935501099, output[1]);
    }

    @Test
    void threeLayerCheck() throws IOException {
        float[] input = {0.5f, 0.1f, 0.9f};
        float[] hidden0 = new float[3];
        float[] hidden1 = new float[3];
        float[] output = new float[2];
        float[] hiddenSigma0 = new float[3];
        float[] hiddenSigma1 = new float[3];
        float[] sigma = new float[2];
        float[] target = {0.087f, 0.1555f};

        Layer layer0 = Layer.getFirstLayer(3, hidden0, hiddenSigma0);
        layer0.initialize();
        layer0.setInput(input);
        Layer layer1 = Layer.getLayer(hidden0, hidden1, hiddenSigma0, hiddenSigma1);
        layer1.initialize();
        Layer layer2 = Layer.getLayer(hidden1, output, hiddenSigma1, sigma);
        layer2.initialize();

        float[] error = {1, 1};
        for (int i = 0; i < 100; i++) {
            layer0.perceive();
            layer1.perceive();
            layer2.perceive();
            assertTrue(output[0] > 0 && output[0] < 1);
            assertTrue(output[1] > 0 && output[1] < 1);
            sigma[0] = output[0] - target[0];
            sigma[1] = output[1] - target[1];
            float newError = (float) (0.5 * Math.pow(sigma[0], 2));
            assertTrue(newError <= error[0]);
            error[0] = newError;
            newError = (float) (0.5 * Math.pow(sigma[1], 2));
            assertTrue(newError <= error[1]);
            error[1] = newError;
            System.out.println(Arrays.toString(error));
            layer2.learn(0.5f);
            layer1.learn(0.5f);
            layer0.learn(0.5f);
        }
        assertTrue(error[0] <= 0.005);
        assertTrue(error[1] <= 0.005);
        System.out.println("output " + Arrays.toString(output));

        byte[] state0 = save(layer0);
        byte[] state1 = save(layer1);
        byte[] state2 = save(layer2);
        sigma[0] = 0;
        sigma[1] = 0;
        layer2.learn(0.5f);
        layer1.learn(0.5f);
        layer0.learn(0.5f);
        assertFalse(differentArrays(state0, save(layer0)));
        assertArrayEquals(state1, save(layer1));
        assertArrayEquals(state2, save(layer2));
        sigma[0] = 0.0001f;
        sigma[1] = 0;
        layer2.learn(0.5f);
        layer1.learn(0.5f);
        layer0.learn(0.5f);
        assertTrue(differentArrays(state0, save(layer0)));
        assertTrue(differentArrays(state1, save(layer1)));
        assertTrue(differentArrays(state2, save(layer2)));

        layer0.perceive();
        layer1.perceive();
        layer2.perceive();
        float out0 = output[0];
        float out1 = output[1];
        input[0] = 0.008f;
        layer0.perceive();
        layer1.perceive();
        layer2.perceive();
        assertNotEquals(out0, output[0]);
        assertNotEquals(out1, output[1]);
        input[0] = 0.5f;
        layer0.perceive();
        layer1.perceive();
        layer2.perceive();
        assertEquals(out0, output[0]);
        assertEquals(out1, output[1]);

        System.out.println("weights 0");
        float[] weights = getWeights(layer0);
        for (float weight : weights) {
            System.out.println(weight);
            assertTrue(weight != 0 && weight > -1 && weight < 1);
        }
        System.out.println("weights 1");
        weights = getWeights(layer1);
        for (float weight : weights) {
            System.out.println(weight);
            assertTrue(weight != 0 && weight > -1 && weight < 1);
        }
        System.out.println("weights 2");
        weights = getWeights(layer2);
        for (float weight : weights) {
            System.out.println(weight);
            assertTrue(weight != 0 && weight > -2 && weight < 2);
        }
    }

    private float[] getWeights(Layer layer) {
        float[] weights = null;
        try {
            byte[] state = save(layer);
            weights = new float[state.length / 4];
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(state));
            for (int i = 0; i < weights.length; i++) {
                weights[i] = stream.readFloat();
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weights;
    }

    private byte[] save(Layer layer) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        layer.save(dataStream);
        dataStream.close();
        return byteStream.toByteArray();
    }

    private void load(Layer layer, byte[] state) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(state));
        layer.load(stream);
        stream.close();
    }

    private boolean differentArrays(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return true;
            }
        }
        return false;
    }
}