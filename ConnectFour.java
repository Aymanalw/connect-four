import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

public class ConnectFour extends JFrame implements ActionListener {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private CircleButton[][] buttons;
    private boolean player1Turn;
    private boolean gameOver;
    private Difficulty difficulty; // Enum for difficulty level
    private JLabel statusLabel; // Label to show the current player

    public ConnectFour() {
        setTitle("Go Connect Four");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(ROWS, COLS));
        panel.setBackground(Color.blue);
        buttons = new CircleButton[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                buttons[row][col] = new CircleButton();
                buttons[row][col].setBackground(Color.blue);
                buttons[row][col].addActionListener(this);
                panel.add(buttons[row][col]);
            }
        }

        add(panel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 4));

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        bottomPanel.add(restartButton);

        statusLabel = new JLabel("Player 1's Turn");
        bottomPanel.add(statusLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        player1Turn = true;
        gameOver = false;

        chooseDifficulty();
    }

    private void chooseDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(this, "Select Difficulty Level:", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);

        switch (choice) {
            case 0 -> difficulty = Difficulty.EASY;
            case 1 -> difficulty = Difficulty.MEDIUM;
            case 2 -> difficulty = Difficulty.HARD;
            default -> difficulty = Difficulty.MEDIUM;
        }
    }

    private void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    private void restartGame() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                buttons[row][col].setBackground(Color.blue);
            }
        }
        player1Turn = true;
        gameOver = false;
        statusLabel.setText("Player 1's Turn");
        chooseDifficulty();
    }

    private boolean isColumnFull(int col) {
        return buttons[0][col].getBackground() != Color.blue;
    }

    private boolean checkWinner(Color color) {
        // Check horizontally
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (buttons[row][col].getBackground() == color &&
                        buttons[row][col + 1].getBackground() == color &&
                        buttons[row][col + 2].getBackground() == color &&
                        buttons[row][col + 3].getBackground() == color) {
                    return true;
                }
            }
        }

        // Check vertically
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                if (buttons[row][col].getBackground() == color &&
                        buttons[row + 1][col].getBackground() == color &&
                        buttons[row + 2][col].getBackground() == color &&
                        buttons[row + 3][col].getBackground() == color) {
                    return true;
                }
            }
        }

        // Check diagonally
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (buttons[row][col].getBackground() == color &&
                        buttons[row + 1][col + 1].getBackground() == color &&
                        buttons[row + 2][col + 2].getBackground() == color &&
                        buttons[row + 3][col + 3].getBackground() == color) {
                    return true;
                }
            }
        }

        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = COLS - 1; col >= 3; col--) {
                if (buttons[row][col].getBackground() == color &&
                        buttons[row + 1][col - 1].getBackground() == color &&
                        buttons[row + 2][col - 2].getBackground() == color &&
                        buttons[row + 3][col - 3].getBackground() == color) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (buttons[row][col].getBackground() == Color.blue) {
                    return false;
                }
            }
        }
        return true;
    }

    private int evaluateBoard() {
        if (checkWinner(Color.RED)) {
            return 1000;
        } else if (checkWinner(Color.YELLOW)) {
            return -1000;
        } else {
            return 0;
        }
    }

    private int minimax(int depth, boolean isMaximizing) {
        int score = evaluateBoard();

        if (score == 1000 || score == -1000 || depth == getMaxDepth()) {
            return score - depth;
        }
        if (isBoardFull()) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int col = 0; col < COLS; col++) {
                if (!isColumnFull(col)) {
                    dropPiece(col, Color.RED);
                    int currentScore = minimax(depth + 1, false);
                    bestScore = Math.max(bestScore, currentScore);
                    undoDrop(col);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int col = 0; col < COLS; col++) {
                if (!isColumnFull(col)) {
                    dropPiece(col, Color.YELLOW);
                    int currentScore = minimax(depth + 1, true);
                    bestScore = Math.min(bestScore, currentScore);
                    undoDrop(col);
                }
            }
            return bestScore;
        }
    }

    private int getMaxDepth() {
        return switch (difficulty) {
            case EASY -> 3;
            case MEDIUM -> 5;
            case HARD -> 7;
            default -> 5;
        };
    }

    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;
        for (int col = 0; col < COLS; col++) {
            if (!isColumnFull(col)) {
                dropPiece(col, Color.RED);
                int currentScore = minimax(0, false);

                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    bestMove = col;
                }
                undoDrop(col);
            }
        }
        return bestMove;
    }

    private void dropPiece(int col, Color color) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (buttons[row][col].getBackground() == Color.blue) {
                buttons[row][col].setBackground(color);
                break;
            }
        }
    }

    private void undoDrop(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (buttons[row][col].getBackground() != Color.blue) {
                buttons[row][col].setBackground(Color.blue);
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            JButton buttonClicked = (JButton) e.getSource();
            for (int col = 0; col < COLS; col++) {
                if (buttonClicked == buttons[0][col] && !isColumnFull(col)) {
                    if (player1Turn) {
                        dropPiece(col, Color.RED);
                        if (checkWinner(Color.RED)) {
                            JOptionPane.showMessageDialog(this, "Player 1 wins!");
                            gameOver = true;
                        } else if (isBoardFull()) {
                            JOptionPane.showMessageDialog(this, "It's a draw!");
                            gameOver = true;
                        } else {
                            player1Turn = false;
                            statusLabel.setText("Computer's Turn");
                            // AI move with delay and thinking message
                            new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws InterruptedException {
                                    JOptionPane.showMessageDialog(ConnectFour.this, "AI thinking...");
                                    Thread.sleep(1000);  // Delay of 1 second to simulate AI thinking
                                    return null;
                                }

                                @Override
                                protected void done() {
                                    try {
                                        get();  // Wait for the background task to complete
                                        int computerMove = findBestMove();
                                        dropPiece(computerMove, Color.YELLOW);
                                        if (checkWinner(Color.YELLOW)) {
                                            JOptionPane.showMessageDialog(ConnectFour.this, "Computer wins!");
                                            gameOver = true;
                                        } else if (isBoardFull()) {
                                            JOptionPane.showMessageDialog(ConnectFour.this, "It's a draw!");
                                            gameOver = true;
                                        }
                                        player1Turn = true;
                                        statusLabel.setText("Player 1's Turn");
                                    } catch (InterruptedException | ExecutionException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }.execute();
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConnectFour::new);
    }
}

class CircleButton extends JButton {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int diameter = Math.min(getWidth(), getHeight()) - 10;
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        g2d.setColor(getBackground());
        g2d.fillOval(x, y, diameter, diameter);

        g2d.setColor(getForeground());
        g2d.drawOval(x, y, diameter, diameter);

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(60, 60);
    }
}

enum Difficulty {
    EASY, MEDIUM, HARD
}
