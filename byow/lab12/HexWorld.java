package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int numHexagon = 19;
    private static final long SEED = 5566;
    private static final Random RANDOM = new Random(SEED);

    /* Instance variables for an individual hexagon in the HexWorld. */
    private final int length, longestRow, highestColumn;
    private TETile[][] hexWorld;

    /* Instance variable for the entire HexWorld.*/
    private final int width, height;

    /* Constructor */
    public HexWorld(int s) {
        if (s < 2) {
            throw new IllegalArgumentException("The length of an individual hexagon must be greater than or equal to 2");
        }
        this.length = s;
        this.longestRow = longestRow();
        this.highestColumn = highestColumn();
        this.width = widthHexWorld();
        this.height = heightHexWorld();
        this.hexWorld = new TETile[width][height];
    }

    public void initialize() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                hexWorld[i][j] = Tileset.NOTHING;
            }
        }
    }

    public void fillHexWorld() {
        for (int i = 0; i < numHexagon; i ++) {
            addHexagon(i);
        }
    }

    /* Creates the ith hexagon by filling it in with random tiles.
     * First fills in the lower triangle and the the upper triangle. */
    private void addHexagon(int i) {
        int x = ithStartX(i);
        int y = ithStartY(i);
        TETile tile = randomTile();

        for (int j = y; j < y + highestColumn / 2; j++) {
            for (int k = x  - (j - y); k < x + length + (j - y); k++) {
                hexWorld[k][j] = tile;
            }
        }

        for (int j = y + highestColumn / 2; j < y + highestColumn; j++) {
            for (int k = x - (length - 1) + j - y  - highestColumn / 2; k <= x + 2 * (length - 1) - (j - y) + highestColumn / 2; k++) {
                hexWorld[k][j] = tile;
            }
        }
    }

    /* Calculates the width of the HexWorld.
     * The width is comprised of 2 lengths and 3 longest rows (the middle) of a hexagon. */
    private int widthHexWorld() { return length * 2 + longestRow * 3; }

    /* Calculates the height of the HexWorld.
     * The height is comprised of 5 heights of a hexagon. */
    private int heightHexWorld() { return highestColumn * 5; }

    /* Calculates the longest (widest) row of an individual hexagon. */
    private int longestRow() { return length * 3 - 2; }

    /* Calculates the highest column of an individual hexagon. */
    private int highestColumn() { return length * 2; }

    /* Calculates the x-coordinate of the lower-left tile of the ith individual hexagon. */
    private int ithStartX(int i) { return length - 1 + column(i) * (2 * length - 1); }

    /* Calculates which column a hexagon is at.
     * The leftmost is column 0 and right up to column 4.
     * The ordering of hexagons is the level order, from bottom to top, left to right. */
    private int column(int i) {
        validate(i);
        if (i == 0 || i == 18) { return 2; }
        else if (Math.floorMod(i, 5) == 3) { return 0; }
        else if (Math.floorMod(i, 5) == 1) { return 1; }
        else if (Math.floorMod(i, 5) == 4) { return 2; }
        else if (Math.floorMod(i, 5) == 2) { return 3; }
        else { return 4; }
    }

    /* Calculates the y-coordinate of the lower-left tile of the ith individual hexagon. */
    private int ithStartY(int i) { return level(i) * length; }

    /* Calculates which level a hexagon is at.
     * The bottom is level 0 and top up to level 8. */
    private int level(int i) {
        validate(i);
        if (i == 0) { return 0; }
        else if (i <= 2) { return 1; }
        else if (i <= 5) { return 2; }
        else if (i <= 7) { return 3; }
        else if (i <= 10) { return 4; }
        else if (i <= 12) { return 5; }
        else if (i <= 15) { return 6; }
        else if (i <= 17) { return 7; }
        else { return 8;}
    }

    /* Selects the tile to be filled in a hexagon randomly. */
    private TETile randomTile() {
        int tileNum = RANDOM.nextInt(4);
        switch (tileNum) {
            case 0: return Tileset.MOUNTAIN;
            case 1: return Tileset.TREE;
            case 2: return Tileset.SAND;
            default: return Tileset.WATER;
        }
    }

    private void validate(int i) {
        if (i < 0 || i > 18) { throw new IllegalArgumentException("The numbering of individual hexagons is 0 - 18 inclusively."); }
    }

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    public TETile[][] getTETile() { return this.hexWorld; }

    public static void main(String[] args) {
        HexWorld hw = new HexWorld(4);
        /* Initializes the tile rendering engine with a window of size width x height. */
        TERenderer ter = new TERenderer();
        ter.initialize(hw.getWidth(), hw.getHeight());

        hw.initialize();
        /* Fills the HexWorld with random hexagons. */
        hw.fillHexWorld();

        /* Draws out the HexWorld. */
        ter.renderFrame(hw.getTETile());
    }
}