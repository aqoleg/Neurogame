// Layer of the neural net
// activation function is sigmoid: out = 1 / (1 + e^(-c * net)), where c = 1
// out = 1 / (1 + e^(-net))
// error function: error = 0.5 * (target - out)^2
// sigma = dError/dOut = 0.5 * 2 * (target - out)^(2 - 1) * (-1) = out - target
// sigma = out - target
package space.aqoleg.neurogame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Layer {
    private final int inputsN; // = input.length = upSigma.length
    private final int outputsN; // = output.length = sigma.length
    private final float[] output; // 0 < output[n] < 1
    private final float[] upSigma; // sigma for each output of the up layer, null for first layer
    private final float[] sigma; // sigma for each output of this layer, sigma[n] = output[n] - target
    private final float[][] weights; // [out][in], weights[out][inputsN] is a bias
    private float[] input; // input[n] >= 0 for the first layer, 0 < input[n] < 1 for others

    private Layer(int inputsN, int outputsN, float[] input, float[] output, float[] upSigma, float[] sigma) {
        this.inputsN = inputsN;
        this.outputsN = outputsN;
        this.input = input;
        this.output = output;
        this.upSigma = upSigma;
        this.sigma = sigma;
        weights = new float[outputsN][inputsN + 1];
    }

    // Create first layer
    // inputsN - number of the input of the net, input set with setInput(input)
    // output of this layer is the input of the down layer
    // sigma of this layer is upSigma of the down layer
    // sigma.length = output.length, inputsN > 1
    static Layer getFirstLayer(int inputsN, float[] outputs, float[] sigmas) throws ExceptionInInitializerError {
        if (outputs.length != sigmas.length) {
            throw new ExceptionInInitializerError("output.length != sigma.length");
        }
        if (inputsN < 2) {
            throw new ExceptionInInitializerError("inputsN < 2");
        }
        return new Layer(inputsN, outputs.length, new float[inputsN], outputs, null, sigmas);
    }

    // Create middle layer
    // input of this layer is the output of the up layer
    // output of this layer is the input of the down layer or output of the net (for the last layer)
    // upSigma of this layer is sigma of the up layer
    // sigma of this layer is upSigma of the down layer or sigma of the net (for the last layer)
    // upSigma.length = input.length, sigma.length = output.length, input.length > 1
    static Layer getLayer(float[] inputs, float[] outputs, float[] upSigmas, float[] sigmas)
            throws ExceptionInInitializerError {
        if (inputs.length < 2) {
            throw new ExceptionInInitializerError("input.length < 2");
        }
        if (upSigmas.length != inputs.length) {
            throw new ExceptionInInitializerError("upSigma.length != input.length");
        }
        if (outputs.length != sigmas.length) {
            throw new ExceptionInInitializerError("output.length != sigma.length");
        }
        return new Layer(inputs.length, outputs.length, inputs, outputs, upSigmas, sigmas);
    }

    // Initialize with small, randomly chosen weights
    void initialize() {
        for (int out = 0; out < outputsN; out++) {
            for (int in = 0; in <= inputsN; in++) {
                weights[out][in] = (float) ((Math.random() - 0.5));
            }
        }
    }

    // Save weights to stream
    void save(DataOutputStream stream) throws IOException {
        for (int out = 0; out < outputsN; out++) {
            for (int in = 0; in <= inputsN; in++) {
                stream.writeFloat(weights[out][in]);
            }
        }
    }

    // Load weights from stream
    void load(DataInputStream stream) throws IOException {
        for (int out = 0; out < outputsN; out++) {
            for (int in = 0; in <= inputsN; in++) {
                weights[out][in] = stream.readFloat();
            }
        }
    }

    // Set input of the first layer
    boolean setInput(float[] input) {
        if (upSigma == null && input.length == inputsN) {
            this.input = input;
            return true;
        }
        return false;
    }

    // Do perceive
    void perceive() {
        // Do for each output
        for (int out = 0; out < outputsN; out++) {
            // calculate net = bias + sum(weight * input)
            float net = weights[out][inputsN]; // bias
            for (int in = 0; in < inputsN; in++) {
                net += weights[out][in] * input[in];
            }
            output[out] = (float) (1 / (1 + Math.exp(-net))); // use activation function
        }
    }

    // Learn, alpha - learning rate, 0 < alpha <= 1
    void learn(float alpha) {
        // Clear upSigma
        if (upSigma != null) {
            for (int in = 0; in < inputsN; in++) {
                upSigma[in] = 0;
            }
        }
        // Do for each output
        for (int out = 0; out < outputsN; out++) {
            // use back propagation algorithm to minimize error
            // dError/dWeight = dError/dOut * dOut/dNet * dNet/dWeight
            //
            // out = 1 / (1 + e^(-net)) = e^net / (1 + e^net)
            // dOut/dNet = (e^net * (1 + e^net) - e^net * e^net) / (1 + e^net)^2 = e^net / (1 + e^net)^2 =
            // = (e^net / (1 + e^net)) * (1 / (1 + e^net)) = out * (1 - out)
            //
            // net = bias + sum(weight * input)
            // dNet/dWeight = input
            //
            // node delta = dError/dNet = dError/dOut * dOut/dNet = sigma * out * (1 - out)
            float delta = sigma[out] * output[out] * (1 - output[out]);
            // Do for each input
            for (int in = 0; in < inputsN; in++) {
                // upSigma = sum(dError/dUpOut)
                // dError/dUpOut = dError/dInput = dError/dNet * dNet/dInput = delta * dNet/dInput
                //
                // net = bias + sum(weight * input)
                // dNet/dInput = weight
                // upSigma += delta * weight
                // use weight before it will be update!
                if (upSigma != null) {
                    upSigma[in] += delta * weights[out][in];
                }
                // dError/dWeight = delta * dNet/dWeight = delta * input
                // weight = weight - alpha * dError/dWeight
                weights[out][in] -= alpha * delta * input[in];
            }
        }
    }
}