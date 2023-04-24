import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * User Interface for the Game of Life
 * @author Aaron Howe
 * @version JDK 17
 */
public class LifeUI extends Component {

    // member fields for the frame, panels, buttons for both the grid and user control buttons, and animation thread
    private LifeBoard board;
    private boolean[][] initialState;
    private JFrame lifeFrame;
    private JPanel lifePanel;
    private JPanel buttonPanel;
    private JLabel messageLabel;
    private JButton[][] buttonGrid;
    private JTextField gpmTextField;
    private JButton start;
    private JButton stop;
    private JButton reset;
    private boolean buttonsEnabled;
    private Thread startAnimation;

    /**
     * Constructor for the grid of 2D Array of Booleans
     * @param grid 2D into Array of Booleans to represent the cells of the game
     */
    public LifeUI(boolean[][] grid) {
        // instance of the LifeBoard class for the logic of the game
        this.board = new LifeBoard(grid);
        // initial state of the board, variably set by the user
        this.initialState = board.getCells();
        this.lifeFrame = new JFrame("Conway's Game of Life");
        this.lifeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.lifeFrame.setLayout(new BorderLayout());

        // layout for the Start, Stop, and Reset buttons
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new GridLayout(1,3));
        this.lifeFrame.add(this.buttonPanel, BorderLayout.NORTH);

        // text field for the user enter the rate at which the animation thread runs generations of life
        this.gpmTextField = new JTextField();
        this.buttonPanel.add(this.gpmTextField);

        // start button that begins the animation thread
        this.start = new JButton("Start");
        this.start.addActionListener(e -> {
            startAnimation();
        });

        this.buttonPanel.add(this.start);

        // stop button that pauses the animation thread
        this.stop = new JButton("Stop");
        this.stop.addActionListener(e -> stopBoard());

        this.buttonPanel.add(this.stop);

        // reset button that resets the cells to the initial state as set by the user
        this.reset = new JButton("Reset");
        this.reset.addActionListener(e -> resetBoard());

        this.buttonPanel.add(this.reset);

        // layout of the button grid of cells
        this.lifePanel = new JPanel();
        this.lifePanel.setLayout(new GridLayout(initialState.length, initialState[0].length));
        this.lifeFrame.add(this.lifePanel, BorderLayout.CENTER);

        this.messageLabel = new JLabel();
        this.lifeFrame.add(messageLabel, BorderLayout.SOUTH);

        this.buttonGrid = new JButton[initialState.length][initialState[0].length];

        // sets the state of the buttons to alive or dead based on user input
        for (int i = 0; i < initialState.length; i++) {
            for (int j = 0; j < initialState[i].length; j++) {
                JButton button = new JButton(initialState[i][j] ? "O" : ".");
                button.setEnabled(true);
                button.setPreferredSize(new Dimension(45,45));
                button.addActionListener(e -> {
                    if (buttonsEnabled) {
                        JButton clickedButton = (JButton) e.getSource();
                        int row = -1;
                        int col = -1;
                        for (int i1 = 0; i1 < buttonGrid.length; i1++) {
                            for (int j1 = 0; j1 < buttonGrid[0].length; j1++) {
                                if (buttonGrid[i1][j1] == clickedButton) {
                                    row = i1;
                                    col = j1;
                                    break;
                                }
                            }
                        }
                        initialState[row][col] = !initialState[row][col];
                        clickedButton.setText(initialState[row][col] ? "O" : ".");
                    }
                });
                this.buttonGrid[i][j] = button;
                this.lifePanel.add(button);
            }
        }
        this.lifeFrame.pack();
        this.lifeFrame.setVisible(true);
        this.buttonsEnabled = true;
    }

    /**
     * getter method for the grid of buttons
     * @return the grid of JButtons
     */
    public JButton[][] getGrid() {
        return buttonGrid;
    }

    /**
     * getter method for the frame
     * @return the JFrame
     */
    public JFrame getFrame() {
        return lifeFrame;
    }

    /**
     * method to update the buttons of the board
     */
    public void updateGrid() {
        // uses the board object to access the getCells method and set it to the new board
        boolean[][] cells = board.getCells();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                buttonGrid[i][j].setText(cells[i][j] ? "O" : ".");
            }
        }
    }

    /**
     * method for the animation thread
     */
    public void startAnimation() {
        buttonsEnabled = false;
        start.setEnabled(false);
        stop.setEnabled(true);
        startAnimation = new Thread(() -> {
            // state of the animation not running by default
            boolean status = false;
            // while running
            while (!status) {
                try {

                    // call on nextGen() to run new generations
                    board.nextGen();
                    // waits for the current generation to load before updating to a new generation
                    SwingUtilities.invokeAndWait(() -> {
                        updateGrid();
                    });
                    // converts time in minutes to milliseconds, set at 1, divided by user input multiplied by 60 to run at *input* generations per minute
                    Thread.sleep((long) (TimeUnit.MINUTES.toMillis(30) / (Double.parseDouble(gpmTextField.getText()) * 60)));
                } catch (InterruptedException ex) {
                    status = true;
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
            buttonsEnabled = true;
            start.setEnabled(true);
            stop.setEnabled(false);
        });
        // executes the thread
        startAnimation.start();
        messageLabel.setText("Running animations...");
    }

    /**
     * method for the stop button to pause the animation thread
     */
    public void stopBoard() {
        // if the start animation is running and "stop" is clicked, interrupts the thread
        if (startAnimation != null &&  startAnimation.isAlive()) {
            startAnimation.interrupt();
        }
        buttonsEnabled = true;
        gpmTextField.setEnabled(true);
        start.setText("Resume");
        start.setEnabled(true);
        stop.setEnabled(false);
        messageLabel.setText("Animations paused...");
    }

    /**
     * method for the reset button to reset the board to the initial state that was set by the user
     */
    public void resetBoard() {
        start.setText("Start");
        // create a new instance of LifeBoard with the initial state as its parameters
        this.board = new LifeBoard(initialState);
        // call on updateGrid() to reset the board
        updateGrid();
        messageLabel.setText("");
    }
}


















