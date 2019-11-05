// 2 or more connected layers, 1 output
package space.aqoleg.neurogame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Net {
    private static final float THRESHOLD = 0.01f; // stop to learn when Math.abs(sigma) <= THRESHOLD
    private final Layer[] layers; // layers[0] - input
    private final float[] output;
    private final float[] sigma;

    private Net(Layer[] layers, float[] output, float[] sigma) {
        this.layers = layers;
        this.output = output;
        this.sigma = sigma;
    }

    // Create and connect layers
    // map - number of neurons in each layer from first
    static Net getNet(int[] map) throws ExceptionInInitializerError {
        if (map.length < 2) {
            throw new ExceptionInInitializerError("map.length < 2");
        }
        for (int value : map) {
            if (value < 2) {
                throw new ExceptionInInitializerError("map value < 2");
            }
        }
        // layers order:
        // input[map[0]] - layers[0] - inputs[0][map[1]] - layers[1] - (inputs[1][map[2]] - ... - layers[n]) - output
        //               - layers[0] - sigmas[0][map[1]] - layers[1] - (sigmas[1][map[2]] - ... - layers[n]) - outSigma
        Layer[] layers = new Layer[map.length];
        float[] output = new float[1];
        float[] outSigma = new float[1];
        float[][] inputs = new float[layers.length - 1][];
        float[][] sigmas = new float[layers.length - 1][];
        for (int n = 0; n < inputs.length; n++) {
            inputs[n] = new float[map[n + 1]];
            sigmas[n] = new float[map[n + 1]];
        }
        // first layer
        layers[0] = Layer.getFirstLayer(map[0], inputs[0], sigmas[0]);
        // middle layers
        for (int layerN = 1; layerN < layers.length - 1; layerN++) {
            layers[layerN] = Layer.getLayer(inputs[layerN - 1], inputs[layerN], sigmas[layerN - 1], sigmas[layerN]);
        }
        // last layer
        layers[layers.length - 1] = Layer.getLayer(inputs[layers.length - 2], output, sigmas[layers.length - 2], outSigma);
        return new Net(layers, output, outSigma);
    }

    // Initialize each layer
    void initialize() {
        for (Layer layer : layers) {
            layer.initialize();
        }
    }

    // Save weights to stream
    void save(DataOutputStream stream) throws IOException {
        for (Layer layer : layers) {
            layer.save(stream);
        }
    }

    // Load weights from stream
    void load(DataInputStream stream) throws IOException {
        for (Layer layer : layers) {
            layer.load(stream);
        }
    }

    // Perceive this input
    // return output or 0 if incorrect input
    float getAnswer(float[] inputs) {
        if (layers[0].setInput(inputs)) {
            // perceive from input to output
            for (Layer layer : layers) {
                layer.perceive();
            }
            return output[0];
        }
        return 0;
    }

    // Learn with this inputs, target and alpha count times or till |sigma| > threshold
    // return count of learning or 0 if incorrect input
    int learn(float[] inputs, float target, float alpha, int count, float threshold) {
        if (layers[0].setInput(inputs)) {
            for (int i = 0; i < count; i++) {
                // perceive from input to output
                for (Layer layer : layers) {
                    layer.perceive();
                }
                // calculate sigma
                float sigma = output[0] - target;
                if (Math.abs(sigma) <= threshold) {
                    return i;
                }
                this.sigma[0] = sigma;
                // learn from output to input
                for (int layerN = layers.length - 1; layerN >= 0; layerN--) {
                    layers[layerN].learn(alpha);
                }
            }
            return count;
        }
        return 0;
    }
}