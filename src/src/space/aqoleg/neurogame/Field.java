// Game field, 16 cells
// cells values represent as 2^n
package space.aqoleg.neurogame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class Field {
    // Move directions
    static final int DOWN = 0;
    static final int LEFT = 1;
    static final int RIGHT = 2;
    static final int UP = 3;
    // cell numbers for 4 lines to be squeezed
    private static final int[][][] MATRIX = new int[][][]{ // [direction][lineN][linePos]
            {{12, 8, 4, 0}, {13, 9, 5, 1}, {14, 10, 6, 2}, {15, 11, 7, 3}},
            {{0, 1, 2, 3}, {4, 5, 6, 7}, {8, 9, 10, 11}, {12, 13, 14, 15}},
            {{3, 2, 1, 0}, {7, 6, 5, 4}, {11, 10, 9, 8}, {15, 14, 13, 12}},
            {{0, 4, 8, 12}, {1, 5, 9, 13}, {2, 6, 10, 14}, {3, 7, 11, 15}}
    };
    private int[] field = new int[16];
    private int[][] nextFields = new int[4][16]; // [direction][cellN] possible fields after each move before computer turn
    private int scores[] = new int[4]; // [direction] scores of each move, if scores[direction] < 0 there is no move
    private boolean loose;

    // Clear field and make 2 computer steps
    void start() {
        Arrays.fill(field, 0);
        playComputer();
        playComputer();
        fillNextMoves();
    }

    // Save field state to stream
    void save(DataOutputStream stream) throws IOException {
        for (int cellN = 0; cellN < 16; cellN++) {
            stream.writeByte(field[cellN]);
        }
    }

    // Load field state from stream
    void load(DataInputStream stream) throws IOException {
        for (int cellN = 0; cellN < 16; cellN++) {
            field[cellN] = stream.readByte();
        }
        fillNextMoves(); // load other
    }

    int getCell(int cellN) {
        return field[cellN];
    }

    int getNextFieldCell(int direction, int cellN) {
        return nextFields[direction][cellN];
    }

    int getScore(int direction) {
        return scores[direction];
    }

    boolean areLoose() {
        return loose;
    }

    // If it is possible move, play in this direction, else return false
    boolean play(int direction) {
        if (loose || scores[direction] < 0) {
            return false;
        }
        System.arraycopy(nextFields[direction], 0, field, 0, 16);
        playComputer();
        fillNextMoves();
        return true;
    }

    // Fill one empty cell with 2 or 4
    private void playComputer() {
        // Find count of empty cells
        int countEmpty = 0;
        for (int cell : field) {
            if (cell == 0) {
                countEmpty++;
            }
        }
        // Get random position of empty cell to be filled
        int emptyPosToFill = (int) (Math.random() * countEmpty);
        // Find cell and fill it
        int currentEmptyPos = 0;
        for (int cellN = 0; cellN < 16; cellN++) {
            if (field[cellN] == 0) {
                if (currentEmptyPos == emptyPosToFill) {
                    field[cellN] = Math.random() <= 0.1 ? 2 : 1; // fill with 2 (90% odd) or 4 (10% odd)
                    return;
                } else {
                    currentEmptyPos++;
                }
            }
        }
    }

    // Calculate and fill nextFields, scores, loose
    private void fillNextMoves() {
        loose = true;
        // Do for each direction
        for (int direction = 0; direction < 4; direction++) {
            int addedCells = 0;
            int score = 0;
            // Do for each line
            for (int lineN = 0; lineN < 4; lineN++) {
                int[] newLine = {0, 0, 0, 0};
                int newLinePos = 0;
                int bufferCell = 0;
                for (int linePos = 0; linePos < 4; linePos++) {
                    int cell = field[MATRIX[direction][lineN][linePos]];
                    if (cell != 0) {
                        // do for non-empty cells
                        if (bufferCell == 0) {
                            // buffer is empty, put the cell to the buffer
                            bufferCell = cell;
                        } else if (bufferCell != cell) {
                            // buffer is not empty, put the cell from buffer to the newLine, fill buffer with new cell
                            newLine[newLinePos++] = bufferCell;
                            bufferCell = cell;
                        } else {
                            // buffer is not empty and the same as cell, put sum to the newLine, clear buffer
                            addedCells++;
                            score += cell;
                            newLine[newLinePos++] = cell + 1; // 2^n + 2^n = 2^(n + 1)
                            bufferCell = 0;
                        }
                    }
                }
                // if buffer is not empty, put it into newLine
                if (bufferCell != 0) {
                    newLine[newLinePos] = bufferCell;
                }
                // put new line in the nextFields
                for (int linePos = 0; linePos < 4; linePos++) {
                    nextFields[direction][MATRIX[direction][lineN][linePos]] = newLine[linePos];
                }
            }
            // Fill scores and loose
            if (addedCells > 1) {
                score += Math.pow(2, addedCells - 1); // extra scores if more then one addition
            }
            // if nothing changes this is impossible move, else no loose yet
            if (Arrays.equals(field, nextFields[direction])) {
                score = -2;
            } else {
                loose = false;
                score += getNextScores(nextFields[direction]); // add scores from next moves
            }
            this.scores[direction] = score;
        }
    }

    // Return average scores from all possible moves
    private int getNextScores(int[] field) {
        int score = 0;
        // Do for perpendicular directions
        for (int direction = 0; direction < 2; direction++) {
            int addedCells = 0;
            // Do for each line
            for (int lineN = 0; lineN < 4; lineN++) {
                int bufferCell = 0;
                for (int linePos = 0; linePos < 4; linePos++) {
                    int cell = field[MATRIX[direction][lineN][linePos]];
                    if (cell != 0) {
                        // do for non-empty cells
                        if (bufferCell != 0 && bufferCell == cell) {
                            // buffer is not empty and the same as cell, get scores, clear buffer
                            addedCells++;
                            score += cell;
                            bufferCell = 0;
                        } else {
                            // buffer is empty or different, fill buffer with new cell
                            bufferCell = cell;
                        }
                    }
                }
            }
            if (addedCells > 1) {
                score += Math.pow(2, addedCells - 1); // extra scores if more then one addition
            }
        }
        return score / 2;
    }
}