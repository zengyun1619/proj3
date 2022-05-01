package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private final int width;
    private final int height;
    private final Random rand;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(60, 60, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(CHARACTERS[rand.nextInt(CHARACTERS.length)]);
        }
        return sb.toString();
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text((double) width / 2, (double) height / 2, String.valueOf(s));
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            drawFrame(String.valueOf(letters.charAt(i)));
            StdDraw.pause(1000);
            drawFrame("");
            StdDraw.pause(500);
        }
        playerTurn = true;
    }

    public String solicitNCharsInput(int n) {
        StringBuilder sb = new StringBuilder();
        if (playerTurn) {
            for (int i = 0; i < n; i++) {
                if (StdDraw.hasNextKeyTyped()) {
                    sb.append(StdDraw.nextKeyTyped());
                    drawFrame(sb.toString());
                    StdDraw.pause(200);
                }
            }
        }
        playerTurn = false;
        return sb.toString();
    }

    public void startGame() {
        playerTurn = false;
        boolean gameOver = false;
        int round = 1;

        drawFrame("Complete 5 rounds to win.");
        StdDraw.pause(2000);
        drawFrame(ENCOURAGEMENT[rand.nextInt(ENCOURAGEMENT.length)]);
        StdDraw.pause(2000);
        while (!gameOver && round < 6) {
            StdDraw.pause(1000);
            drawFrame("Round: " + round);
            StdDraw.pause(1000);
            String expected = generateRandomString(round);
            flashSequence(expected);
            StdDraw.pause(3000);

            if (playerTurn) {
                String inputString = solicitNCharsInput(expected.length());
                if (inputString.equals(expected)) { round++; }
                else { gameOver = true; }
            }
        }
        if (round == 6) {
            drawFrame("All rounds complete! You won!");
        } else {
            drawFrame("Game over. You will get it next time.");
        }
        StdDraw.pause(1000);
    }
}
