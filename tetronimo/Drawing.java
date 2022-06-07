package tetromino;

import java.awt.Color;
import java.awt.Graphics;

// Due to the projection/shadows, all content is painted from the bottom left
// to the top right

public class Drawing {

  void paint(int[][] gameState, Graphics g) {
    if (gameState == null)
      return;
    drawInnerFrame(g);

    for (int i = gameState.length - 1; i >= 0; i--) // row
    {
      for (int j = 0; j < gameState[0].length; j++) { // col
        if (gameState[i][j] < 1)
          continue; // ignore blank space
        drawBlock(j, i, gameState[i][j], g);
      }
    }

    drawOuterFrame(g);
    g.drawString("Lines cleared: " + Tetronimo.score,
        GAME_WIDTH + LEFT_PAD + 4 * BLOCK_WIDTH, GAME_HEIGHT / 3 + TOP_PAD);

  }

  void drawInnerFrame(Graphics g) {
    int x1 = LEFT_PAD;
    int x2 = LEFT_PAD + BLOCK_WIDTH * Tetronimo.COLS;
    int y1 = TOP_PAD + Tetronimo.ROWS * BLOCK_HEIGHT;
    int y2 = TOP_PAD;

    // outline of game box: do some shading but not the face
    for (int i = 0; i < DEPTH; i++) {
      g.setColor(BORDER_SHADES[TOP]);// 160 - 16 * i, 160 - 16 * i, 160 - 16 * i));
      g.drawLine(x1 + i, y1 - i, x2 + i, y1 - i);// bottom
      g.setColor(BORDER_SHADES[RIGHT]);// 120 - 12 * i, 120 - 12 * i, 120 - 12 * i));
      g.drawLine(x1 + i, y1 - i, x1 + i, y2 - i);// left
    }
  }

  // Show the next piece that will appear
  void drawPreview(int[][] preview, Graphics g) {
    for (int i = preview.length - 1; i >= 0; i--) {
      for (int j = 0; j < preview[0].length; j++) {
        if (preview[i][j] > 0)
          drawBlock((j + Tetronimo.COLS + 4), (i + Tetronimo.ROWS / 2),
              preview[i][j], g);
      }
    }
  }

  void drawBlock(int col, int row, int colour, Graphics g) {
    int x = col * BLOCK_WIDTH + LEFT_PAD + DEPTH / 4;
    int y = row * BLOCK_HEIGHT + TOP_PAD - DEPTH / 4;

    // draw black outline (in case it needs to cut through other drawing)
    g.setColor(new Color(0, 0, 0));
    g.drawRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);

    Color[] shades;
    shades = switch (colour) {
      case RED -> RED_SHADES;
      case BLUE -> BLUE_SHADES;
      case YELLOW -> YELLOW_SHADES;
      case PURPLE -> PURPLE_SHADES;
      case GREEN -> GREEN_SHADES;
      case ORANGE -> ORANGE_SHADES;
      default -> NAVY_SHADES;
    };

    // shade face
    g.setColor(shades[FACE]);
    g.fillRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);

    // shade top
    g.setColor(shades[TOP]);
    for (int t = 0; t < DEPTH / 2; t++) {
      g.drawLine(x + t, y - t, x + BLOCK_WIDTH + t, y - t);
    }

    // shade right
    g.setColor(shades[RIGHT]);
    for (int t = 0; t < DEPTH / 2; t++) {
      g.drawLine(x + BLOCK_WIDTH + t, y - t,
          x + BLOCK_WIDTH + t, y + BLOCK_HEIGHT - t);
    }

  }

  void drawOuterFrame(Graphics g) {
    int x1 = LEFT_PAD - THICKNESS;
    int x2 = LEFT_PAD + GAME_WIDTH + THICKNESS;
    int y1 = TOP_PAD + GAME_HEIGHT + THICKNESS;
    int y2 = TOP_PAD - THICKNESS;
    // shade outline of game box, but no facing
    for (int i = 0; i < DEPTH; i++) {
      g.setColor(BORDER_SHADES[TOP]);
      g.drawLine(x1 + i, y2 - i, x2 + i, y2 - i);// top
      g.setColor(BORDER_SHADES[RIGHT]);
      g.drawLine(x2 + i, y2 - i, x2 + i, y1 - i);// right
    }

    int w = GAME_WIDTH + 2 * THICKNESS;
    int h = GAME_HEIGHT + 2 * THICKNESS;
    // finally draw the face of the game box
    g.setColor(BORDER_SHADES[FACE]);
    for (int i = 0; i < THICKNESS; i++) {
      g.drawRect(x1 + i, y2 + i, w - 2 * i, h - 2 * i); // face
    }

  }

  final int TOP_PAD = 30;
  final int LEFT_PAD = 20;
  final int BLOCK_WIDTH = 20;
  final int BLOCK_HEIGHT = 20;
  final int GAME_WIDTH = Tetronimo.COLS * BLOCK_WIDTH;
  final int GAME_HEIGHT = Tetronimo.ROWS * BLOCK_HEIGHT;
  final int THICKNESS = 5;
  final int DEPTH = 8;

  final int RED = 1;
  final Color[] RED_SHADES = new Color[] { new Color(200, 20, 20),
      new Color(130, 10, 10),
      new Color(60, 0, 0) };
  final int BLUE = 2;
  final Color[] BLUE_SHADES = new Color[] { new Color(20, 120, 240),
      new Color(10, 70, 150),
      new Color(0, 40, 80) };
  final int YELLOW = 3;
  final Color[] YELLOW_SHADES = new Color[] { new Color(200, 200, 20),
      new Color(110, 110, 10),
      new Color(60, 60, 0) };
  final int PURPLE = 4;
  final Color[] PURPLE_SHADES = new Color[] { new Color(150, 20, 200),
      new Color(80, 10, 130),
      new Color(40, 0, 60) };
  final int GREEN = 5;
  final Color[] GREEN_SHADES = new Color[] { new Color(20, 200, 20),
      new Color(10, 100, 10),
      new Color(0, 40, 0) };
  final int ORANGE = 6;
  final Color[] ORANGE_SHADES = new Color[] { new Color(210, 120, 20),
      new Color(130, 55, 10),
      new Color(70, 25, 0) };
  final int NAVY = 7;
  final Color[] NAVY_SHADES = new Color[] { new Color(0, 0, 150),
      new Color(0, 0, 100),
      new Color(0, 0, 50) };
  final Color[] BORDER_SHADES = new Color[] { new Color(120, 120, 120),
      new Color(80, 80, 80),
      new Color(60, 60, 60) };
  final int FACE = 0;
  final int TOP = 1;
  final int RIGHT = 2;

}
