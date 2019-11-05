package space.aqoleg.neurogame;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    @Test
    void start() {
        Field field = new Field();
        field.start();
        checkStartField(field);
        field.play(Field.LEFT);
        field.play(Field.RIGHT);
        field.play(Field.DOWN);
        field.start();
        checkStartField(field);
    }

    @Test
    void save() throws IOException {
        Field field = new Field();
        field.play(Field.LEFT);
        field.play(Field.RIGHT);
        field.play(Field.DOWN);
        field.play(Field.LEFT);
        field.play(Field.DOWN);
        byte[] state = save(field);
        for (int cellN = 0; cellN < 16; cellN++) {
            assertEquals(state[cellN], field.getCell(cellN));
        }
    }

    @Test
    void load() throws IOException {
        byte[] state = new byte[]{
                2, 0, 3, 13,
                2, 3, 0, 12,
                0, 8, 4, 8,
                0, 9, 0, 8};
        Field field = new Field();
        load(field, state);
        for (int cellN = 0; cellN < 16; cellN++) {
            assertEquals(state[cellN], field.getCell(cellN));
        }
        assertFalse(field.areLoose());
        assertEquals(12, field.getScore(Field.DOWN));
        assertEquals(19, field.getScore(Field.UP));
        assertEquals(3, field.getScore(Field.LEFT));
        assertEquals(8, field.getScore(Field.RIGHT));
    }

    @Test
    void loadAndSave() throws IOException {
        byte[] state = new byte[]{
                0, 1, 3, 13,
                2, 3, 11, 12,
                10, 14, 11, 8,
                2, 9, 1, 8};
        Field field = new Field();
        field.start();
        load(field, state);
        assertArrayEquals(state, save(field));
        assertFalse(field.areLoose());
        assertEquals(13, field.getScore(Field.LEFT));
    }

    @Test
    void playUp() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 1, 0, 0,
                1, 2, 3, 13,
                1, 2, 1, 0,
                1, 1, 2, 0});
        assertEquals(9, field.getScore(Field.DOWN));
        assertEquals(9, field.getScore(Field.UP));
        assertEquals(7, field.getScore(Field.LEFT));
        assertEquals(6, field.getScore(Field.RIGHT));
        assertTrue(field.play(Field.UP));
        checkComputerTurn(field, new byte[]{
                2, 1, 3, 13,
                2, 3, 1, 0,
                0, 1, 2, 0,
                0, 0, 0, 0});
        assertFalse(field.areLoose());
    }

    @Test
    void playDown() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 3, 11, 2,
                0, 3, 1, 1,
                1, 0, 1, 0,
                0, 3, 1, 1});
        assertEquals(16, field.getScore(Field.DOWN));
        assertEquals(16, field.getScore(Field.UP));
        assertEquals(8, field.getScore(Field.LEFT));
        assertEquals(12, field.getScore(Field.RIGHT));
        assertTrue(field.play(Field.DOWN));
        checkComputerTurn(field, new byte[]{
                0, 0, 0, 0,
                0, 0, 11, 0,
                0, 3, 1, 2,
                2, 4, 2, 2});
        assertFalse(field.areLoose());
    }

    @Test
    void playLeft() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 1, 3, 2,
                0, 3, 1, 1,
                1, 2, 1, 0,
                0, 0, 1, 1});
        assertEquals(11, field.getScore(Field.DOWN));
        assertEquals(9, field.getScore(Field.UP));
        assertEquals(8, field.getScore(Field.LEFT));
        assertEquals(10, field.getScore(Field.RIGHT));
        assertTrue(field.play(Field.LEFT));
        checkComputerTurn(field, new byte[]{
                2, 3, 2, 0,
                3, 2, 0, 0,
                1, 2, 1, 0,
                2, 0, 0, 0});
        assertFalse(field.areLoose());
    }

    @Test
    void playRight() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 1, 3, 2,
                7, 3, 1, 1,
                1, 2, 1, 0,
                1, 0, 1, 1});
        assertEquals(12, field.getScore(Field.DOWN));
        assertEquals(12, field.getScore(Field.UP));
        assertEquals(10, field.getScore(Field.LEFT));
        assertEquals(10, field.getScore(Field.RIGHT));
        assertTrue(field.play(Field.RIGHT));
        checkComputerTurn(field, new byte[]{
                0, 2, 3, 2,
                0, 7, 3, 2,
                0, 1, 2, 1,
                0, 0, 1, 2});
        assertFalse(field.areLoose());
    }

    @Test
    void areLoose() throws IOException {
        byte[] state = new byte[]{
                1, 2, 3, 0,
                2, 3, 1, 8,
                1, 2, 3, 6,
                3, 1, 9, 4};
        Field field = new Field();
        load(field, state);
        assertEquals(-2, field.getScore(Field.DOWN));
        assertEquals(0, field.getScore(Field.UP));
        assertEquals(-2, field.getScore(Field.LEFT));
        assertEquals(0, field.getScore(Field.RIGHT));
        assertFalse(field.areLoose());
        assertFalse(field.play(Field.LEFT));
        assertTrue(field.play(Field.UP));
        assertEquals(-2, field.getScore(Field.DOWN));
        assertEquals(-2, field.getScore(Field.UP));
        assertEquals(-2, field.getScore(Field.LEFT));
        assertEquals(-2, field.getScore(Field.RIGHT));
        assertTrue(field.areLoose());
        assertFalse(field.play(Field.DOWN));
        assertFalse(field.play(Field.LEFT));
        assertFalse(field.play(Field.RIGHT));
        assertFalse(field.play(Field.UP));
    }

    @Test
    void play() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 2, 1, 2,
                5, 4, 3, 2,
                6, 7, 8, 9,
                13, 12, 11, 10});
        assertFalse(field.play(Field.LEFT));
        assertFalse(field.play(Field.RIGHT));
        assertFalse(field.areLoose());
        assertEquals(3, field.getScore(Field.DOWN));
        assertTrue(field.play(Field.DOWN));
        byte[] state = save(field);
        field.start();
        checkStartField(field);
        load(field, state);
        assertTrue(field.play(Field.LEFT));
        assertTrue(field.play(Field.LEFT));
        assertTrue(field.play(Field.LEFT));
        assertTrue(field.play(Field.DOWN));
        assertTrue(field.play(Field.RIGHT));
        state = save(field);
        field.start();
        load(field, state);
        assertTrue(field.play(Field.RIGHT));
        assertTrue(field.play(Field.RIGHT));
        assertTrue(field.play(Field.DOWN));
        assertTrue(field.play(Field.LEFT));
        assertTrue(field.play(Field.LEFT));
        assertTrue(field.play(Field.LEFT));
        assertEquals(14, field.getCell(12));
    }

    @Test
    void nextField() throws IOException {
        Field field = new Field();
        load(field, new byte[]{
                1, 0, 1, 3,
                1, 4, 3, 3,
                0, 0, 8, 2,
                13, 12, 1, 0});
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 0));
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 1));
        assertEquals(1, field.getNextFieldCell(Field.DOWN, 2));
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 3));
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 4));
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 5));
        assertEquals(3, field.getNextFieldCell(Field.DOWN, 6));
        assertEquals(0, field.getNextFieldCell(Field.DOWN, 7));
        assertEquals(2, field.getNextFieldCell(Field.DOWN, 8));
        assertEquals(4, field.getNextFieldCell(Field.DOWN, 9));
        assertEquals(8, field.getNextFieldCell(Field.DOWN, 10));
        assertEquals(4, field.getNextFieldCell(Field.DOWN, 11));
        assertEquals(13, field.getNextFieldCell(Field.DOWN, 12));
        assertEquals(12, field.getNextFieldCell(Field.DOWN, 13));
        assertEquals(1, field.getNextFieldCell(Field.DOWN, 14));
        assertEquals(2, field.getNextFieldCell(Field.DOWN, 15));
        assertEquals(2, field.getNextFieldCell(Field.LEFT, 0));
        assertEquals(3, field.getNextFieldCell(Field.LEFT, 1));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 2));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 3));
        assertEquals(1, field.getNextFieldCell(Field.LEFT, 4));
        assertEquals(4, field.getNextFieldCell(Field.LEFT, 5));
        assertEquals(4, field.getNextFieldCell(Field.LEFT, 6));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 7));
        assertEquals(8, field.getNextFieldCell(Field.LEFT, 8));
        assertEquals(2, field.getNextFieldCell(Field.LEFT, 9));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 10));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 11));
        assertEquals(13, field.getNextFieldCell(Field.LEFT, 12));
        assertEquals(12, field.getNextFieldCell(Field.LEFT, 13));
        assertEquals(1, field.getNextFieldCell(Field.LEFT, 14));
        assertEquals(0, field.getNextFieldCell(Field.LEFT, 15));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 0));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 1));
        assertEquals(2, field.getNextFieldCell(Field.RIGHT, 2));
        assertEquals(3, field.getNextFieldCell(Field.RIGHT, 3));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 4));
        assertEquals(1, field.getNextFieldCell(Field.RIGHT, 5));
        assertEquals(4, field.getNextFieldCell(Field.RIGHT, 6));
        assertEquals(4, field.getNextFieldCell(Field.RIGHT, 7));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 8));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 9));
        assertEquals(8, field.getNextFieldCell(Field.RIGHT, 10));
        assertEquals(2, field.getNextFieldCell(Field.RIGHT, 11));
        assertEquals(0, field.getNextFieldCell(Field.RIGHT, 12));
        assertEquals(13, field.getNextFieldCell(Field.RIGHT, 13));
        assertEquals(12, field.getNextFieldCell(Field.RIGHT, 14));
        assertEquals(1, field.getNextFieldCell(Field.RIGHT, 15));
        assertEquals(2, field.getNextFieldCell(Field.UP, 0));
        assertEquals(4, field.getNextFieldCell(Field.UP, 1));
        assertEquals(1, field.getNextFieldCell(Field.UP, 2));
        assertEquals(4, field.getNextFieldCell(Field.UP, 3));
        assertEquals(13, field.getNextFieldCell(Field.UP, 4));
        assertEquals(12, field.getNextFieldCell(Field.UP, 5));
        assertEquals(3, field.getNextFieldCell(Field.UP, 6));
        assertEquals(2, field.getNextFieldCell(Field.UP, 7));
        assertEquals(0, field.getNextFieldCell(Field.UP, 8));
        assertEquals(0, field.getNextFieldCell(Field.UP, 9));
        assertEquals(8, field.getNextFieldCell(Field.UP, 10));
        assertEquals(0, field.getNextFieldCell(Field.UP, 11));
        assertEquals(0, field.getNextFieldCell(Field.UP, 12));
        assertEquals(0, field.getNextFieldCell(Field.UP, 13));
        assertEquals(1, field.getNextFieldCell(Field.UP, 14));
        assertEquals(0, field.getNextFieldCell(Field.UP, 15));
    }

    private void checkStartField(Field field) {
        int countOfEmpty = 0;
        for (int cellN = 0; cellN < 16; cellN++) {
            int cell = field.getCell(cellN);
            if (field.getCell(cellN) == 0) {
                countOfEmpty++;
            } else {
                assertTrue(cell == 1 || cell == 2);
            }
        }
        assertEquals(14, countOfEmpty);
        assertFalse(field.areLoose());
    }

    private byte[] save(Field field) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        field.save(dataStream);
        dataStream.close();
        return byteStream.toByteArray();
    }

    private void load(Field field, byte[] state) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(state));
        field.load(stream);
        stream.close();
    }

    private void checkComputerTurn(Field field, byte[] expected) {
        int differences = 0;
        for (int cellN = 0; cellN < 16; cellN++) {
            int cell = field.getCell(cellN);
            if (expected[cellN] == 0 && cell != 0) {
                differences++;
                assertTrue(cell == 1 || cell == 2);
            } else {
                assertEquals(expected[cellN], cell);
            }
        }
        assertEquals(1, differences);
    }
}