package tetromino;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;

public class Tetronimo extends javax.swing.JPanel {

    Drawing draw = new Drawing();

    boolean playing = false;
    boolean dropping = false;
    int sleepTime = 400; // block drop interval, decreases with rows cleared
    static int score = 0;

    // array of active coordinates{ {x,y}, {x,y}, ... }
    int[][] activePiece;

    int nextKey = -1; // let nextPiece generator know nothing is prepared

    void runGame() {
        playing = true;
        while (playing) {
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
            if (!dropping) {
                clearLines();
                makePiece();
            } // contains inherent delay

            doDrop(); // will discover and react when game has been lost
            repaint();
        }
    }

    /**
     * For each row, if it's completely filled, pull all the other rows
     * down a la bubble sort
     */
    void clearLines() {
        for (int i = 0; i < ROWS; i++) {
            // row is assumed full, reject assumption if something is there
            boolean rowCleared = true;
            for (int gameKey : gameState[i]) {
                if (gameKey < 1)
                    rowCleared = false;
            }
            if (rowCleared) {
                // ripple all down, insert empty line at top
                for (int j = i; j > 0; j--) {
                    gameState[j] = gameState[j - 1];
                }
                gameState[0] = new int[COLS];

                sleepTime -= sleepTime / 200 * 5 + 1; // speed up
                score++;
            }
        }
    }

    void makePiece() {
        // first run, make an extra nextPiece
        if (nextKey == -1)
            nextKey = (int) (Math.random() * newPiece.length);

        // imaginary box with active piece inside
        // Translate piece to game position, allows rotation
        pseudoX = (COLS - ROTATION_COLS) / 2;
        pseudoY = 0;

        // 2d array activePiece is an array is {x,y} pairs
        // 0 coordinates at top left of game board
        activePiece = new int[newPiece[nextKey].length][2];
        for (int i = 0; i < newPiece[nextKey].length; i++) {
            activePiece[i][0] = newPiece[nextKey][i][0] + pseudoY;
            activePiece[i][1] = newPiece[nextKey][i][1] + pseudoX;
        }

        for (int[] square : activePiece) {
            if (gameState[square[0]][square[1]] > 0) {
                // game over if square is already occupied
                playing = false;
                return;
            }
        }

        // move piece to the board if it fits
        for (int[] square : activePiece) {
            gameState[square[0]][square[1]] = nextKey + 1;
        }

        // preview the next piece
        nextKey = (int) (Math.random() * newPiece.length);
        preview = new int[4][4];
        for (int[] bit : newPiece[nextKey]) {
            preview[bit[0]][bit[1]] = nextKey + 1;
        }

        dropping = true;
        repaint();
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
        }
    }

    void doDrop() {
        if (!playing || !dropping)
            return;
        // clean active piece off the board
        // but remember the colour
        int pieceType = gameState[activePiece[0][0]][activePiece[0][1]];

        // Remove the piece
        for (int[] pos : activePiece) {
            gameState[pos[0]][pos[1]] = -1;
        }

        // check if piece can appear one row down
        boolean canDrop = true;
        for (int[] square : activePiece) {
            if (square[0] >= ROWS - 1 || gameState[square[0] + 1][square[1]] > 0)
                canDrop = false;
        }

        if (!canDrop) {
            dropping = false;
        } else {
            pseudoY++; // keep track to help rotatePiece()
        }

        // put the piece in, where it was, or one row down
        for (int[] square : activePiece) {
            if (dropping) {
                square[0]++;
            }
            gameState[square[0]][square[1]] = pieceType;
        }
    }

    void keyboard(java.awt.event.KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_UP:
                rotatePiece(activePiece);
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                doDrop();
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                movePiece(-1);
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                movePiece(1);
                break;
            case java.awt.event.KeyEvent.VK_SPACE:
                while (dropping)
                    doDrop();
                break;
        }
        repaint();
    }

    public Tetronimo() {
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keyboard(evt);
            }
        });

        setPreferredSize(new java.awt.Dimension(500, 500));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 463, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 453, Short.MAX_VALUE));
    }

    void movePiece(int dir) {
        if (!dropping)
            return;
        // Remember what type of piece and remove it from board
        int pieceType = gameState[activePiece[0][0]][activePiece[0][1]];
        for (int[] loc : activePiece) {
            gameState[loc[0]][loc[1]] = -1;
        }

        // Can move be default unless there's a clash
        boolean canMove = true;
        for (int[] loc : activePiece) {
            if (loc[1] + dir < 0 ||
                    loc[1] + dir >= COLS ||
                    gameState[loc[0]][loc[1] + dir] > 0) {
                canMove = false;
                break;
            }
        }

        // Either put it back, or put it in new position
        for (int[] loc2 : activePiece) {
            if (canMove) {
                loc2[1] += dir;
            }
            gameState[loc2[0]][loc2[1]] = pieceType;
        }
        // Move tracking box
        if (canMove)
            pseudoX += dir;

    }

    // piece is modified here (or fails to modify)
    void rotatePiece(int[][] piece) {
        if (!dropping)
            return;
        boolean canRotate = true;
        // remember the type of piece (eg for colour)
        int pieceType = gameState[piece[0][0]][piece[0][1]];
        int[][] transform = new int[piece.length][2];

        // clear piece off the board
        for (int[] loc : piece) {
            gameState[loc[0]][loc[1]] = -1;
        }
        // we use pseudoX, pseudoY to define a rotation box (not the piece)
        // it is defined at piece creation, travels with the piece,
        // the box may need to be pulled back into the game board

        if (pseudoX < 0)
            pseudoX = 0;
        if (pseudoX > COLS - ROTATION_COLS)
            pseudoX = COLS - ROTATION_COLS;

        // Process each block in the piece. intermediate variable transform
        for (int i = 0; i < piece.length; i++) {
            // use a smaller grid of pseudo coordinates
            transform[i][0] = piece[i][0] - pseudoY;
            transform[i][1] = piece[i][1] - pseudoX;

            // turn 90 degrees (x = y, y = inverse of x)
            // map it back to the entire game coordinates
            int temp = transform[i][0];
            transform[i][0] = transform[i][1] + pseudoY;
            transform[i][1] = ROTATION_COLS - temp - 1 + pseudoX;

            // abort if the space is taken
            if (transform[i][0] > 19 ||
                    gameState[transform[i][0]][transform[i][1]] > 0) {
                canRotate = false;
                break;
            }
        }

        // replace existing coordinates if rotation succeeds
        if (canRotate) {
            for (int i = 0; i < piece.length; i++) {
                piece[i][0] = transform[i][0];
                piece[i][1] = transform[i][1];
            }
        }

        // put piece back on the board, whether it was rotated or not
        for (int[] loc : piece) {
            gameState[loc[0]][loc[1]] = pieceType;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        draw.paint(gameState, g);
        draw.drawPreview(preview, g);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetronimo");
        Tetronimo game = new Tetronimo();

        frame.getContentPane().setSize(1500, 1000);
        frame.getContentPane().add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        game.setFocusable(true);
        game.requestFocus();

        new Thread(() -> game.runGame()).start();

    }

    static final int ROWS = 20;
    static final int COLS = 10;
    final int ROTATION_ROWS = 4;
    final int ROTATION_COLS = 4;
    int pseudoX = 0;
    int pseudoY = 0;

    int[][] gameState = new int[ROWS][COLS];
    int[][] preview = new int[ROTATION_ROWS][ROTATION_COLS];

    final int[][][] newPiece = new int[][][]
    // coordinates in imaginary grid (for rotation mechanics)
    // Must fit within ROTATION_COLS x ROTATION_ROWS
    { { { 1, 1 }, { 1, 2 }, { 2, 1 }, { 2, 2 } },
            { { 1, 1 }, { 2, 0 }, { 2, 1 }, { 2, 2 } },
            { { 1, 0 }, { 1, 1 }, { 2, 1 }, { 2, 2 } },
            { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 } },
            { { 2, 1 }, { 0, 2 }, { 1, 2 }, { 2, 2 } },
            { { 1, 1 }, { 1, 2 }, { 2, 0 }, { 2, 1 } },
            { { 0, 1 }, { 1, 1 }, { 2, 1 }, { 2, 2 } }
            // extra pieces may be added,
    };

}
