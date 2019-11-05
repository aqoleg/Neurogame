package space.aqoleg.neurogame;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static space.aqoleg.neurogame.Field.*;

class Frame extends JFrame implements KeyListener {
    private static final Color[] COLORS = new Color[]{new Color(0xE6E6E6), new Color(0xE61739), new Color(0xE65C17),
            new Color(0xE6C317), new Color(0xA1E617), new Color(0x39E617), new Color(0x17E65C), new Color(0x17E6C3),
            new Color(0x17A1E6), new Color(0x1739E6), new Color(0x5C17E6), new Color(0xC317E6), new Color(0xE617A1),
            new Color(0x660A47), new Color(0x000000), new Color(0x000000), new Color(0x000000), new Color(0x000000)};
    private final Game game;
    private final Field field;
    private JLabel[] labels = new JLabel[16];
    private JLabel message;
    private boolean pressed;

    Frame(Game game, Field field) {
        super("Neurogame");
        this.game = game;
        this.field = field;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (!pressed) {
            pressed = true;
            switch (event.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    game.move(DOWN);
                    break;
                case KeyEvent.VK_LEFT:
                    game.move(LEFT);
                    break;
                case KeyEvent.VK_UP:
                    game.move(UP);
                    break;
                case KeyEvent.VK_RIGHT:
                    game.move(RIGHT);
                    break;
                case KeyEvent.VK_N:
                    game.start();
                    break;
                case KeyEvent.VK_L:
                    game.learn();
                    break;
                case KeyEvent.VK_F1:
                    game.save();
                    break;
                case KeyEvent.VK_F2:
                    game.load();
                    break;
                default:
                    printMessage("Arrow keys - move, F1 - save, F2 - load, N - new game, L - learn");
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        pressed = false;
    }

    void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel fieldPanel = new JPanel();
        GridLayout layout = new GridLayout(4, 4);
        layout.setHgap(4);
        layout.setVgap(4);
        fieldPanel.setLayout(layout);
        int cellSize = Math.min(Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height) / 7;
        Dimension dimension = new Dimension(cellSize, cellSize);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder());
        for (int i = 0; i < 16; i++) {
            labels[i] = new JLabel();
            labels[i].setPreferredSize(dimension);
            labels[i].setBorder(border);
            labels[i].setHorizontalAlignment(JLabel.CENTER);
            labels[i].setOpaque(true);
            labels[i].setFont(new Font(Font.DIALOG, Font.BOLD, cellSize / 4));
            fieldPanel.add(labels[i]);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        message = new JLabel();
        message.setPreferredSize(new Dimension(cellSize * 3, 32));
        bottomPanel.add(message);

        add(fieldPanel, BorderLayout.PAGE_START);
        add(bottomPanel, BorderLayout.PAGE_END);

        addKeyListener(this);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void printField() {
        for (int cellN = 0; cellN < 16; cellN++) {
            int value = field.getCell(cellN);
            labels[cellN].setBackground(COLORS[value]);
            labels[cellN].setText(value == 0 ? "" : String.valueOf(1 << value));
        }
    }

    void printMessage(String message) {
        this.message.setText(message);
    }
}