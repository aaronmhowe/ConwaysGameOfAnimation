import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
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
    private JPanel fileButtonPanel;
    private JLabel messageLabel;
    private JButton[][] buttonGrid;
    private JTextField gpmTextField;
    private JButton start;
    private JButton stop;
    private JButton reset;
    private JButton save;
    private JButton restore;
    private boolean buttonsEnabled;
    private Thread startAnimation;
    private Color alive = Color.GREEN;
    private Color dead = Color.RED;

    /**
     * Constructor for the grid of 2D Array of Booleans. Contains the logic for setting the state of cells manually,
     * and the logic for saving and restoring game files, as well as frame, panel, label, and button initialization.
     * The meat and potatoes for the user interface.
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

        // initialization for the save button
        this.save = new JButton("Save Game");
        this.save.addActionListener(e -> {
            // JFileChooser opens a file explorer for the user to create a save destination for the initial state
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    // file object to be assigned to the file selected by the user
                    File file = fileChooser.getSelectedFile();
                    // check if the file already exists (confirm overwrite check)
                    if (file.exists()) {
                        int newSaveFile = JOptionPane.showConfirmDialog(null, "This file already" +
                                " contains a previously saved state of the game, Are you sure you want to overwrite?",
                                "Overwrite File", JOptionPane.YES_NO_OPTION);
                        if (newSaveFile != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    // output stream object
                    FileOutputStream output = new FileOutputStream(file);
                    // writes objects to an output stream
                    ObjectOutputStream outputObj = new ObjectOutputStream(output);
                    // writes the initial state to the object output stream
                    outputObj.writeObject(initialState);
                    outputObj.close();
                    output.close();
                    messageLabel.setText("Game Saved Successfully!");
                } catch (IOException ex) {
                    messageLabel.setText("Error saving game: " + ex.getMessage());
                }
            }
        });

        this.fileButtonPanel = new JPanel();
        this.fileButtonPanel.add(save);

        this.restore = new JButton("Restore Saved Game");
        this.restore.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    // check if a user wants to overwrite current game state with a previously saved game
                    if (initialState != null) {
                        int foundSaveFile = JOptionPane.showConfirmDialog(null, "Loading a saved" +
                                " game will overwrite the current game. Are you sure you want to continue?", "Restore" +
                                " Previously Saved Game", JOptionPane.YES_NO_OPTION);
                        if (foundSaveFile != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    // input stream object
                    FileInputStream input = new FileInputStream(file);
                    // writes objects to an input stream
                    ObjectInputStream inputObj = new ObjectInputStream(input);
                    // reads the objects from the input stream and casts it to the 2D boolean array, set to the initial state
                    initialState = (boolean[][]) inputObj.readObject();
                    inputObj.close();
                    input.close();
                    // updates the current state of the game to the initial state from the file
                    board.setBoardState(initialState);
                    // call to updateGrid() to create the initial state
                    updateGrid();
                    messageLabel.setText("Game Restored Successfully!");
                } catch (IOException ex) {
                    messageLabel.setText("Error restoring game: " + ex.getMessage());
                } catch (ClassNotFoundException ex) {
                    messageLabel.setText("Error restoring game: Save file does not exist!");
                }
            }
        });

        this.fileButtonPanel.add(restore);
        this.buttonPanel.add(this.fileButtonPanel);

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
                button.setBackground(Color.WHITE);
                button.setEnabled(true);
                button.setPreferredSize(new Dimension(45,45));
                button.addActionListener(e -> {
                    // check if the buttons are enabled
                    if (buttonsEnabled) {
                        // object used to listen for which button is clicked
                        JButton clickedButton = (JButton) e.getSource();
                        int row = -1;
                        int col = -1;
                        for (int i1 = 0; i1 < buttonGrid.length; i1++) {
                            for (int j1 = 0; j1 < buttonGrid[0].length; j1++) {
                                // determines the coordinate of the button click determined by the row and column assignment
                                if (buttonGrid[i1][j1] == clickedButton) {
                                    row = i1;
                                    col = j1;
                                    break;
                                }
                            }
                        }
                        // toggles the boolean value of a button click, setting the text accordingly
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
                if (cells[i][j]) {
                    buttonGrid[i][j].setBackground(Color.GREEN);
                    buttonGrid[i][j].setForeground(Color.BLACK);
                    buttonGrid[i][j].setText("O");
                } else {
                    buttonGrid[i][j].setBackground(Color.RED);
                    buttonGrid[i][j].setForeground(Color.BLACK);
                    buttonGrid[i][j].setText(".");
                }
            }
        }
    }

    /**
     * method for the animation thread
     */
    public void startAnimation() {
        String gpmText = gpmTextField.getText().trim();
        // check to ensure the user has entered a value for gpm
        if (gpmText.isEmpty()) {
            start.setEnabled(true);
            messageLabel.setText("Please enter a value for generations per minute!");
            return;
        }
        // boolean object determines if an initial state is set
        boolean setInitialState = false;
        for (int i = 0; i < initialState.length; i++) {
            for (int j = 0; j < initialState[i].length; j++) {
                // check if the user set an initial state
                if (initialState[i][j]) {
                    setInitialState = true;
                    break;
                }
            }
        }
        // check if the user didn't set an initial state
        if (!setInitialState) {
            start.setEnabled(true);
            messageLabel.setText("You need to set an initial state of the game!");
            return;
        }
        buttonsEnabled = false;
        start.setEnabled(false);
        stop.setEnabled(true);

        // key listener for the JTextField
        gpmTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // ignores non-digit characters and backspacing (a user can still highlight the text and replace it)
                if (!Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                } else {
                    String text  = gpmTextField.getText();
                    String newText = text + c;
                    try {
                        int value = Integer.parseInt(newText);
                        // check to prevent a user from entering values outside the 1 to 250 range
                        if (value < 1 || value > 250) {
                            e.consume();
                            messageLabel.setText("Only enter values from 1 to 250!");
                        }
                    } catch (NumberFormatException n) {
                        e.consume();
                    }
                }
            }
        });
        startAnimation = new Thread(() -> {
            // state of the animation not running by default
            boolean status = false;
            // while running
            while (!status) {
                try {

                    // call on nextGen() to run new generations
                    board.nextGen();
                    // waits for the current generation to load before updating to a new generation
                    SwingUtilities.invokeAndWait(this::updateGrid);
                    // converts time in minutes to milliseconds, set at 30, divided by user input multiplied by 60 to run at *input* generations per minute
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


















