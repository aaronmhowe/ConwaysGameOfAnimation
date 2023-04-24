/**
 * Aaron Howe
 * HW11: Life-Controller
 * Minimal
 */

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * A class to implement the algorithm for Conway's Game of Life using boolean logic
 * @author Aaron Howe
 * @version JDK 17
 */
public class LifeBoard extends JPanel {

    // private field to initialize a 2D array
    private boolean[][] cells;
    // public field to set the grid size
    public static final int GRID_SIZE = 19;

    /**
     * Constructor to set the grid
     * @param initialState the initial state of the board
     */
    public LifeBoard(boolean[][] initialState) {
        cells = initialState;
        // set the layout in a grid using the GRID_SIZE field for dimensions
        setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                JButton button = new JButton();
                // method call updateCell to update the text of the buttons
                updateCell(button, cells[row][col]);
                // add the buttons to the board
                add(button);
            }
        }
    }

    /**
     * getter method to return the private field cells
     * @return the cells
     */
    public boolean[][] getCells() {
        return cells;
    }

    /**
     * method to create the board
     * @param row the rows of the grid
     * @param col the columns of the grid
     * @return the board
     */
    public boolean printBoard(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            return false;
        } else {
            return cells[row][col];
        }
    }

    /**
     * method to check if all surrounding neighbors are alive
     * @param row the rows we search for live cells
     * @param col the columns
     * @return the live neighbors
     */
    public int checkNeighbors(int row, int col) {
        // counter variable for the live cells
        int numNeighbors = 0;
        // top left
        if (printBoard(row - 1, col - 1)) {
            // increment the count if checks are satisfied
            numNeighbors++;
        }
        // top
        if (printBoard(row - 1, col)) {
            numNeighbors++;
        }
        // top right
        if (printBoard(row - 1, col + 1)) {
            numNeighbors++;
        }
        // left
        if (printBoard(row, col - 1)) {
            numNeighbors++;
        }
        // right
        if (printBoard(row, col + 1)) {
            numNeighbors++;
        }
        // bottom left
        if (printBoard(row + 1, col - 1)) {
            numNeighbors++;
        }
        // bottom
        if (printBoard(row + 1, col)) {
            numNeighbors++;
        }
        // bottom right
        if (printBoard(row + 1, col + 1)) {
            numNeighbors++;
        }
        return numNeighbors;
    }

    /**
     * method that returns true or false on whether a cell is alive
     * @param numNeighbors surrounding cells
     * @param cellState live or dead cell
     * @return alive or dead (true or false)
     */
    public boolean isAlive(int numNeighbors, boolean cellState) {
        if (cellState) {
            return numNeighbors == 2 || numNeighbors == 3;
        } else {
            return numNeighbors == 3;
        }
    }

    /**
     * method to generate the new board of cells
     */
    public void nextGen() {
        // construct new 2D array and set it to the dimensions of GRID_SIZE
        boolean[][] newState = new boolean[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                // set the state of newState using the returns of checkNeighbors and isAlive
                newState[row][col] = isAlive(checkNeighbors(row, col), cells[row][col]);
            }
        }
        // set the state of newState to cells and call updateBoard
        cells = newState;
        updateBoard();
    }

    /**
     * method to update the text on the buttons based on life or death
     * @param button the button representing the cell
     * @param alive live or dead cell
     */
    public void updateCell(JButton button, boolean alive) {
        if (alive) {
            button.setText("O");
        } else {
            button.setText(".");
        }
    }

    /**
     * method to update the components of the grid
     */
    public void updateBoard() {
        Component[] comp = getComponents();
        for (int i = 0; i < comp.length; i++) {
            JButton button = (JButton) comp[i];
            int row = i / GRID_SIZE;
            int col = i % GRID_SIZE;
            updateCell(button, cells[row][col]);
        }
        System.out.println(Arrays.deepToString(cells));
    }
}
