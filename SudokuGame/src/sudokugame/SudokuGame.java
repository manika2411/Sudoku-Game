import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;

public class SudokuGame extends JPanel implements MouseListener {
    private static final int SIZE = 9;
    private static final int CELL_SIZE = 50;
    private int[][] board = new int[SIZE][SIZE];
    private int[][] solution = new int[SIZE][SIZE];
    private boolean[][] fixedCells = new boolean[SIZE][SIZE];
    private boolean[][] incorrectCells = new boolean[SIZE][SIZE];
    private int selectedRow = -1, selectedCol = -1;
    private JButton solveButton, resetButton, hintButton;

    public SudokuGame() {
        setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE + 50));
        generateSudoku();
        addMouseListener(this);
        setFocusable(true);
        
        solveButton = new JButton("Solve");
        resetButton = new JButton("Reset");
        hintButton = new JButton("Hint");
        
        solveButton.addActionListener(e -> solveSudoku());
        resetButton.addActionListener(e -> generateSudoku());
        hintButton.addActionListener(e -> giveHint());

        JPanel panel = new JPanel();
        panel.add(solveButton);
        panel.add(resetButton);
        panel.add(hintButton);
        
        JFrame frame = new JFrame("Sudoku Game");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void generateSudoku() {
        do {
            randomizeBoard(solution);
            copyBoard(solution, board);
            removeNumbers(board);
        } while (!isUniqueSolution());
        
        markFixedCells();
        resetIncorrectCells();
        repaint();
    }

    private void resetIncorrectCells() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                incorrectCells[i][j] = false;
            }
        }
    }

    private void randomizeBoard(int[][] grid) {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) numbers.add(i);
        Collections.shuffle(numbers);
        int[][] tempGrid = new int[SIZE][SIZE];
        fillBoard(tempGrid);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = numbers.get(tempGrid[i][j] - 1);
            }
        }
    }

    private void fillBoard(int[][] grid) {
        backtrackSolve(grid);
    }

    private void copyBoard(int[][] from, int[][] to) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                to[i][j] = from[i][j];
    }

    private void removeNumbers(int[][] grid) {
        Random rand = new Random();
        int blanks = 50;
        while (blanks > 0) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if (grid[row][col] != 0) {
                grid[row][col] = 0;
                blanks--;
            }
        }
    }

    private void markFixedCells() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                fixedCells[i][j] = (board[i][j] != 0);
            }
        }
    }
    
    private boolean isValidMove(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++)
            if (board[row][i] == num || board[i][col] == num)
                return false;
        int boxRow = (row / 3) * 3, boxCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[boxRow + i][boxCol + j] == num)
                    return false;
        return true;
    }

    private boolean backtrackSolve(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValidMove(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (backtrackSolve(grid)) return true;
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidMove(int[][] grid, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++)
            if (grid[row][i] == num || grid[i][col] == num)
                return false;
        int boxRow = (row / 3) * 3, boxCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (grid[boxRow + i][boxCol + j] == num)
                    return false;
        return true;
    }

    private boolean isUniqueSolution() {
        int[][] tempBoard = new int[SIZE][SIZE];
        copyBoard(board, tempBoard);
        int count = countSolutions(tempBoard, 0, 0);
        return count == 1; 
    }

    private int countSolutions(int[][] grid, int row, int col) {
        if (row == SIZE) return 1;

        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SIZE - 1) ? 0 : col + 1;

        if (grid[row][col] != 0) {
            return countSolutions(grid, nextRow, nextCol);
        }

        int count = 0;
        for (int num = 1; num <= SIZE; num++) {
            if (isValidMove(grid, row, col, num)) {
                grid[row][col] = num;
                count += countSolutions(grid, nextRow, nextCol);
            }
        }
        grid[row][col] = 0; 
        return count;
    }

    private void solveSudoku() {
        copyBoard(solution, board);
        resetIncorrectCells();
        repaint();
    }

    private void giveHint() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = solution[i][j];
                    repaint();
                    return;
                }
            }
        }
    }
    
    public void keyPressed(KeyEvent e) {
    if (selectedRow != -1 && selectedCol != -1 && !fixedCells[selectedRow][selectedCol]) {
        char keyChar = e.getKeyChar();
        if (keyChar >= '1' && keyChar <= '9') {
            int num = keyChar - '0';
            board[selectedRow][selectedCol] = 0;
            if (isValidMove(selectedRow, selectedCol, num)) {
                board[selectedRow][selectedCol] = num;
                incorrectCells[selectedRow][selectedCol] = false;
            } else {
                board[selectedRow][selectedCol] = num;
                incorrectCells[selectedRow][selectedCol] = true;
            }
            repaint();
        } else if (keyChar == '0' || keyChar == KeyEvent.VK_BACK_SPACE) {
            board[selectedRow][selectedCol] = 0;
            incorrectCells[selectedRow][selectedCol] = false;
            repaint();
        }
    }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                g.setColor(fixedCells[i][j] ? Color.WHITE: Color.WHITE);
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (board[i][j] != 0) {
                    g.setFont(new Font("Arial", Font.BOLD, 20));
                    g.drawString(Integer.toString(board[i][j]), j * CELL_SIZE + 20, i * CELL_SIZE + 30);
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        int row = e.getY() / CELL_SIZE;
        int col = e.getX() / CELL_SIZE;
        if (!fixedCells[row][col]) {
            String input = JOptionPane.showInputDialog("Enter a number (1-9) or 0 to clear:");
            if (input != null && input.matches("[0-9]")) {
                board[row][col] = Integer.parseInt(input);
                repaint();
            }
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        new SudokuGame();
    }
}
