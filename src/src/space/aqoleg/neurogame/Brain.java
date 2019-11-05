// Make training set, learn, connect net and field
package space.aqoleg.neurogame;

import java.io.*;

class Brain {
    private static final int MAX_INPUTS = 1000;
    private final Field field;
    private final Net net;
    private final float[][][] inputs = new float[MAX_INPUTS][4][17]; // [inputsN][direction]
    private final int[] answers = new int[MAX_INPUTS];
    private final float[] outputs = new float[4];
    private int inputsN = 0;

    Brain(Field field) {
        this.field = field;
        net = Net.getNet(new int[]{17, 40, 20, 8});
    }

    // Initialize net
    void initialize() {
        net.initialize();
    }

    // Save net and field to file, return true if OK
    boolean save(File file) {
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
            net.save(stream);
            field.save(stream);
            stream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // Load net and field from file, return true if OK
    boolean load(File file) {
        if (!file.exists()) {
            return false;
        }
        try {
            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            net.load(stream);
            field.load(stream);
            stream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // Look at field, save it, make answers
    void perceive() {
        for (int direction = 0; direction < 4; direction++) {
            float[] input = inputs[inputsN][direction];
            // find max
            float max = 0;
            for (int cellN = 0; cellN < 16; cellN++) {
                int cell = field.getNextFieldCell(direction, cellN);
                if (cell > max) {
                    max = cell;
                }
            }
            // fill input with 1, 0.5, 0.25, ... , 0
            for (int cellN = 0; cellN < 16; cellN++) {
                int cell = field.getNextFieldCell(direction, cellN);
                input[cellN] = cell == 0 ? 0 : (float) (1 / Math.pow(2, max - cell));
            }
            // fill input[16] with score / 256 or -2
            float score = field.getScore(direction);
            if (score >= 0) {
                score /= 256f;
            }
            input[16] = score;
            // perceive
            outputs[direction] = net.getAnswer(input);
        }
    }

    // After perceive
    float getAnswer(int direction) {
        return outputs[direction];
    }

    int getDirection() {
        int answer = 0;
        float max = 0;
        for (int direction = 0; direction < 4; direction++) {
            if (outputs[direction] > max) {
                max = outputs[direction];
                answer = direction;
            }
        }
        return answer;
    }

    // Add player choice after perceive
    void add(int direction) {
        answers[inputsN] = direction;
        if (inputsN < MAX_INPUTS - 1) {
            inputsN++;
        }
    }

    void learn() {
        for (int i = 0; i < 1000; i++) {
            System.out.println("i = " + i);
            for (int set = 0; set < inputsN; set++) {
                float max = 0;
                int directionWithMaxOut = -1;
                for (int direction = 0; direction < 4; direction++) {
                    if (inputs[set][direction][16] < 0) {
                        outputs[direction] = -1;
                    } else {
                        outputs[direction] = net.getAnswer(inputs[set][direction]);
                        if (outputs[direction] > max) {
                            max = outputs[direction];
                            directionWithMaxOut = direction;
                        }
                    }
                }
                int answer = answers[set];
                if (directionWithMaxOut != answer) {
                    float answerOut = outputs[answer];
                    if (max - answerOut < 0.0001f) {
                        if ((max + answerOut) / 2f > 0.5) {
                            answerOut -= 0.0001;
                        } else {
                            max += 0.0001;
                        }
                    }
                    int l = net.learn(inputs[set][directionWithMaxOut], answerOut, (5000 - i) / 5000f, 10, 0.00001f);
                    int k = net.learn(inputs[set][answer], max, (5000 - i) / 5000f, 10, 0.00001f);
                    System.out.println(directionWithMaxOut + " to " + answerOut + " " + l);
                    System.out.println(answer + " to " + max + " " + k);
                }
            }
        }
        inputsN = 0;
    }
}