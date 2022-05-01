package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Engine {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final int xBuffer = 0;
    public static final int yBuffer = 3;
    public static final int xOff = 0;
    public static final int yOff = 0;
    private static final double terminationRatio = 0.75;
    private static final int roomWidthMin = 4;
    private static final int roomWidthMax = 10;
    private static final int roomHeightMin = 4;
    private static final int roomHeightMax = 8;
    private static final int hallwayWidth = 3;
    private static final int hallwayHeightMin = 3;
    private static final int hallwayHeightMax = 12;
    private static final int welcomePageWidth = 60;
    private static final int welcomePageHeight = 60;
    private static final List<Character> allowedMovementsA = Arrays.asList('A','W','S','D');
    private static final List<Character> allowedMovementsB = Arrays.asList('J','I','K','L');
    private static final int hintCount = 20;
    private static final int nonExitDoorCount = 5;
    private static final int magicCount = 10;
    private static final double coinProb = 0.5;
    private static final int xVision = 8;
    private static final int yVision = 8;
    private static final List<String> playerList = Arrays.asList("playerRed","playerBlue");
    private static final int coinValue = 10;
    private static final int exitValue = 200;


    TERenderer ter = new TERenderer();
    public final TETile[][] myWorld = new TETile[WIDTH][HEIGHT];

    private int filledArea = 0;
    private double filledRatio = 0.0;
    private Random rd;
    private List<Edge> connectableEdges = new LinkedList<>();
    private List<Point> floorPointList = new LinkedList<>();
    private List<Point> doorPointList = new LinkedList<>();
    private String seedTracker = "";
    private String activityTracker = "";
    private Point avatarA;
    private Point avatarB;
    private Point exitDoor;
    private List<Point> exitDoorPointList = new LinkedList<>();
    private List<Point> hintPointList = new LinkedList<>();
    private List<Point> magicPointList = new LinkedList<>();
    private List<Point> coinPointList = new LinkedList<>();

    private String messageHUDPlayerA = "";
    private String messageHUDPlayerB = "";
    private int playerRedScore = 0;
    private int playerBlueScore = 0;


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        displayWelcomePage();

        InputSource inputSource = new KeyboardInputSource();
        while (inputSource.possibleNextInput()) {
            char firstKey = inputSource.getNextKey();
            if (firstKey == 'Q') { System.exit(0); }
            else if (firstKey == 'N') {
                /*get seed and generate a new world*/
                StdDraw.clear(StdDraw.BLACK);
                StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2 + 5, "Please enter a random seed and press 'S' to generate a new world.");
                StdDraw.show();
                StringBuilder seedSB = new StringBuilder();
                while (inputSource.possibleNextInput()) {
                    char c = inputSource.getNextKey();
                    StdDraw.clear(StdDraw.BLACK);
                    if (Character.isDigit(c)) { seedSB.append(c); }
                    Font seedFont = new Font("Monaco", Font.PLAIN, 30);
                    StdDraw.setFont(seedFont);
                    StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2, seedSB.toString());
                    StdDraw.show();
                    if (Character.toUpperCase(c) == 'S') { break; }
                }
                String wholeSeed = 'N' + seedSB.toString() + 'S';
                interactWithInputString(wholeSeed);
                break;
            }
            else if (firstKey =='L') {
                /*load the saved game from txt file */
                interactWithInputString(readGameState());
                break;
            }
            else {
                Font warningFont = new Font("Monaco", Font.PLAIN, 20);
                StdDraw.setFont(warningFont);
                StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2 - 20, "Please press 'N', 'L' or 'Q'");
                StdDraw.show();
            }
        }
    }

    private String readGameState() {
        String gameString = null;
        try {
            URL path = Engine.class.getResource("gameState.txt");
            assert path != null;
            File gameState = new File(path.getFile());
            Scanner myReader = new Scanner(gameState);
            if (!myReader.hasNextLine()) {
                System.exit(0);
            } else {
                gameString = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Game State File Not Found.");
            e.printStackTrace();
        }
        return gameString;
    }

    private void displayWelcomePage() {
        setCanvas();
        Font headerFont = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(headerFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight - 15, "MAN & BRICK");
        Font menuFont = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(menuFont);
        StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2, "New Game (N)");
        StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2 - 5, "Load Game (L)");
        StdDraw.text((double) welcomePageWidth / 2, (double) welcomePageHeight / 2 - 10, "Quit (Q)");
        StdDraw.show();
    }

    /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
     * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
     */
    private void setCanvas() {
        StdDraw.setCanvasSize(welcomePageWidth * 16, welcomePageHeight * 16);
        StdDraw.setXscale(0, welcomePageWidth);
        StdDraw.setYscale(0, welcomePageHeight);
        StdDraw.enableDoubleBuffering();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        InputSource stringInput = new StringInputDevice(input);
        char firstChar = stringInput.getNextKey();
        StringBuilder seedSB = new StringBuilder();
        StringBuilder activitySB = new StringBuilder();

        if (Character.toUpperCase(firstChar) == 'N') {
            seedSB.append('N');
            while (stringInput.possibleNextInput()) {
                char c = Character.toUpperCase(stringInput.getNextKey());
                seedSB.append(c);
                if (c == 'S') { break; }
            }

            while (stringInput.possibleNextInput()) {
                char c = Character.toUpperCase(stringInput.getNextKey());
                activitySB.append(c);
            }

            int seed = getSeed(seedSB.toString());
            rd = new Random(seed);
            ter.initialize(WIDTH + xBuffer, HEIGHT + yBuffer, xOff, yOff);
            initializeMyWorld();

            addFirstRoom();
            while (filledRatio < terminationRatio) { addSpace(); }

            initializeLoopWay();
            addExitDoor();
            addNonExitDoor();

            initializeAvatar();
            initializeMagic(magicCount);
            initializeHint(hintCount);
            initializeCoin(coinProb);

            } else {
            String loadedStringInput = readGameState();
            interactWithInputString(loadedStringInput);
            while (stringInput.possibleNextInput()) {
                char c = Character.toUpperCase(stringInput.getNextKey());
                activitySB.append(c);
            }
        }

        String activity = activitySB.toString();

        for (int i = 0; i < activity.length(); i++) {
            char c = activity.charAt(i);
            if (allowedMovementsA.contains(c)) {
                move(c, avatarA, 0);
            } else if (allowedMovementsB.contains(c)) {
                move(c, avatarB, 1);
            }
        }

        return myWorld;
    }

    private void saveThenQuit() {
        String text = seedTracker + activityTracker;
        try {
            URL path = Engine.class.getResource("gameState.txt");
            assert path != null;
            File gameState = new File(path.getFile());
            FileWriter fw = new FileWriter(gameState.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void addLockedDoor(boolean isExitDoor) {
        while(true) {
            Edge exitEdge = getEdge(rd.nextInt(connectableEdges.size()));
            String direction = exitEdge.getDirection();
            int x = getDoorX(exitEdge);
            int y = getDoorY(exitEdge);
            Point p = new Point(x, y);
            if (!getTile(p).description().equals(Tileset.WALL.description())) { continue; }
            boolean validExitDoor = true;
            while (p != null && p.getNeighbor(direction) != null) {
                if (!getTile(p.getNeighbor(direction)).description().equals(Tileset.NOTHING.description())) {
                    validExitDoor = false;
                    break;
                }
                p = p.getNeighbor(direction);
            }
            if (!exitDoorPointList.isEmpty()) {
                for (Point exitDoor: exitDoorPointList) {
                    if (distance(p, exitDoor) < 10.0) {
                        if (rd.nextInt(Math.round(distance(p, exitDoor))) == 0) {
                            validExitDoor = false;
                            break;
                        }
                    }
                }
            }
            if (validExitDoor) {
                if (isExitDoor) {
                    exitDoor = new Point(x, y);
                    exitDoorPointList.add(new Point(x, y));
                    myWorld[x][y] = Tileset.LOCKED_DOOR;
                } else {
                    exitDoorPointList.add(new Point(x, y));
                    myWorld[x][y] = Tileset.LOCKED_DOOR;
                }
                return;
            }
        }
    }

    private void addExitDoor() {
        addLockedDoor(true);
    }

    private void addNonExitDoor() {
        for(int i = 0; i < nonExitDoorCount; i++) {
            addLockedDoor(false);
        }
    }


    /* Steps to add a random space to myWorld:
     * 1. get a connectable edge from the list
     * 2. get the direction of the edge and randomly get a wall to be the connecting door
     *  (the connecting door cannot be the four corners.)
     * 3. explore its outer space and return a list of the four corners of the space
     *  (if unable to add any space onto this edge, start over from step 1.)
     * 4. add a space
     * 5. connect two spaces by marking their doors Tileset.FLOOR
     * 6. add the three non-connecting edges of the new space to connectableEdges
     * 7. remove the connected edge from connectableEdges
     */
    private void addSpace() {
        int index = rd.nextInt(connectableEdges.size());
        Edge edge = getEdge(index);

        int doorX = getDoorX(edge);
        int doorY = getDoorY(edge);
        Point door = new Point(doorX, doorY);

        List<Point> outerSpace = exploreOuterSpace(edge, door);
        if (outerSpace != null && addable(outerSpace)) {
            List<Point> newSpacePoints;
            if (hallwayOnly(outerSpace)) {
                newSpacePoints = addHallway(outerSpace, edge, door);
            } else {
                int hallwayOrRoom = rd.nextInt(3);
                if (hallwayOrRoom == 0) {
                    newSpacePoints = addHallway(outerSpace, edge, door);
                } else {
                    newSpacePoints = addRoom(outerSpace, edge, door);
                }
            }
            fillSpaceAddNonConnectingEdges(newSpacePoints, edge);
            connectDoor(edge, door);
            removeConnectedEdge(index);
        }
    }

    private boolean addable(List<Point> outerSpace) {
        Point lowerLeft = outerSpace.get(0);
        Point upperLeft = outerSpace.get(2);
        int verticalDistance = distance(lowerLeft, upperLeft);
        return verticalDistance >= hallwayHeightMin - 1;
    }

    /* Removes the connected edge of the old space from connectableEdges. */
    private void removeConnectedEdge(int index) { connectableEdges.remove(index); }

    /* Constructs the three non-connecting edges of the new space and add them to connectableEdges. */
    private void fillSpaceAddNonConnectingEdges(List<Point> newSpacePoints, Edge edge) {
        String direction = edge.getDirection();
        Point ll = newSpacePoints.get(0);
        Point ur = newSpacePoints.get(3);
        int minX = Math.min(ll.getX(), ur.getX());
        int minY = Math.min(ll.getY(), ur.getY());
        int maxX = Math.max(ll.getX(), ur.getX());
        int maxY = Math.max(ll.getY(), ur.getY());

        fillSpace(minX, minY, maxX, maxY);

        List<Edge> fourEdges = createFourEdges(minX, minY, maxX, maxY);
        for (Edge e : fourEdges) {
            if (!e.getDirection().equals(opposite(direction))) { connectableEdges.add(e); }
        }
    }

    /* Connects two spaces by marking their doors Tileset.FLOOR. */
    private void connectDoor(Edge edge, Point door) {
        String direction = edge.getDirection();
        Point newDoor = door.getNeighbor(direction);
        myWorld[door.getX()][door.getY()] = Tileset.FLOOR;
        myWorld[newDoor.getX()][newDoor.getY()] = Tileset.FLOOR;
        floorPointList.add(door);
        floorPointList.add(newDoor);
    }

    /* Calculates the space of a room. */
    private List<Point> addRoom(List<Point> outerSpace, Edge edge, Point door) {
        List<Point> newSpacePoints = new ArrayList<>();
        Point ll, lr, ul, ur;
        String direction = edge.getDirection();
        Point newDoor = door.getNeighbor(direction);
        Point lowerLeft = outerSpace.get(0);
        Point lowerRight = outerSpace.get(1);
        Point upperLeft = outerSpace.get(2);

        int widthUpperBound = Math.min(roomWidthMax, distance(lowerLeft, lowerRight) +  1);
        int width = randomInt(roomWidthMin, widthUpperBound);
        int goLeft = Math.min(width - 2, distance(newDoor, lowerLeft));
        int goRight = width - goLeft - 1;

        int heightUpperBound = Math.min(roomHeightMax, distance(lowerLeft, upperLeft) + 1);
        int height = randomInt(roomHeightMin, heightUpperBound);

        Point lowerLeftTracker = door.getNeighbor(direction);
        for (int i = 0; i < goLeft; i++) { lowerLeftTracker = lowerLeftTracker.getNeighbor(left(direction)); }
        ll = new Point(lowerLeftTracker.getX(), lowerLeftTracker.getY());

        Point lowerRightTracker = door.getNeighbor(direction);
        for (int i = 0; i < goRight; i++) { lowerRightTracker = lowerRightTracker.getNeighbor(right(direction)); }
        lr = new Point(lowerRightTracker.getX(), lowerRightTracker.getY());

        Point verticalTracker = new Point(door.getX(), door.getY());
        for (int i = 0; i < height; i++) { verticalTracker = verticalTracker.getNeighbor(direction); }

        Point upperLeftTracker = new Point(verticalTracker.getX(), verticalTracker.getY());
        for (int i = 0; i < goLeft; i++) { upperLeftTracker = upperLeftTracker.getNeighbor(left(direction)); }
        ul = new Point(upperLeftTracker.getX(), upperLeftTracker.getY());

        Point upperRightTracker = new Point(verticalTracker.getX(), verticalTracker.getY());
        for (int i = 0; i < goRight; i++) { upperRightTracker = upperRightTracker.getNeighbor(right(direction)); }
        ur = new Point(upperRightTracker.getX(), upperRightTracker.getY());

        newSpacePoints.add(ll);
        newSpacePoints.add(lr);
        newSpacePoints.add(ul);
        newSpacePoints.add(ur);

        updateFilledArea(width, height);
        updateFilledRatio();

        return newSpacePoints;
    }

    /* Calculates the space of a hallway. */
    private List<Point> addHallway(List<Point> outerSpace, Edge edge, Point door) {
        List<Point> newSpacePoints = new ArrayList<>();
        Point ll, lr, ul, ur;
        String direction = edge.getDirection();
        Point lowerLeft = outerSpace.get(0);
        Point upperLeft = outerSpace.get(2);
        Point newDoor = door.getNeighbor(direction);

        ll = newDoor.getNeighbor(left(direction));
        lr = newDoor.getNeighbor(right(direction));

        int upperBound = Math.min(hallwayHeightMax, distance(lowerLeft, upperLeft) + 1);
        int hallwayHeight = randomInt(hallwayHeightMin, upperBound);

        Point verticalTracker = new Point(door.getX(), door.getY());
        for (int i = 0; i < hallwayHeight; i++) { verticalTracker = verticalTracker.getNeighbor(direction); }
        ul = verticalTracker.getNeighbor(left(direction));
        ur = verticalTracker.getNeighbor(right(direction));

        newSpacePoints.add(ll);
        newSpacePoints.add(lr);
        newSpacePoints.add(ul);
        newSpacePoints.add(ur);

        updateFilledArea(hallwayWidth, hallwayHeight);
        updateFilledRatio();

        return newSpacePoints;
    }

    /* Returns true if there is only enough space to add a hallway. */
    private boolean hallwayOnly(List<Point> outerSpace) {
        Point lowerLeft = outerSpace.get(0);
        Point lowerRight = outerSpace.get(1);
        Point upperLeft = outerSpace.get(2);
        return distance(lowerLeft, lowerRight) == hallwayWidth - 1
                || distance(lowerLeft, upperLeft) == hallwayHeightMin - 1;
    }

    /**
     * @param edge a connectable edge randomly picked from connectableEdges
     * @param door a tile on the edge
     * @return a list of four points that define the feasible outer space off the door
     *         (lowerLeft, lowerRight, upperLeft, upperRight)
     */
    private List<Point> exploreOuterSpace(Edge edge, Point door) {
        List<Point> outerSpace = new ArrayList<>();
        String direction = edge.getDirection();

        if (!explorable(edge, door)) {
            outerSpace = null;
        } else {
            Point newDoor = door.getNeighbor(direction);
            Point leftTracker = door.getNeighbor(direction);
            Point rightTracker = door.getNeighbor(direction);
            // Searches the lowerLeft point
            while (explorableLeft(newDoor, leftTracker, direction)) { leftTracker = leftTracker.getNeighbor(left(direction)); }
            outerSpace.add(new Point(leftTracker.getX(), leftTracker.getY()));

            // Searches the lowerRight point
            while (explorableRight(newDoor, rightTracker, direction)) { rightTracker = rightTracker.getNeighbor((right(direction))); }
            outerSpace.add(new Point(rightTracker.getX(), rightTracker.getY()));

            // Searches the upperLeft and upperRight point
            Point verticalTracker = new Point(rightTracker.getX(), rightTracker.getY());
            Point lowerLeft = outerSpace.get(0);
            Point lowerRight = outerSpace.get(1);

            Point sideBackTracker = new Point(rightTracker.getX(), rightTracker.getY());
            while (explorableUp(rightTracker, verticalTracker, direction)) {
                sideBackTracker = new Point(verticalTracker.getX(), verticalTracker.getY());

                while (explorableBackLeft(verticalTracker, sideBackTracker, direction, lowerLeft, lowerRight)) {
                    sideBackTracker = sideBackTracker.getNeighbor(left(direction));
                }

                // Break condition for north or south edge
                if (lowerLeft.getY() == lowerRight.getY() && sideBackTracker.getX() != lowerLeft.getX()) { break; }

                // Break condition for west or east edge
                if (lowerLeft.getX() == lowerRight.getX() && sideBackTracker.getY() != lowerLeft.getY()) { break; }

                verticalTracker = verticalTracker.getNeighbor(direction);
            }

            // Creates the upperLeft and upperRight point for north or south edge.
            if (lowerLeft.getY() == lowerRight.getY()) {
                Point upperLeft, upperRight;
                if (sideBackTracker.getX() != lowerLeft.getX()) {
                    upperLeft = new Point(lowerLeft.getX(), verticalTracker.getNeighbor(opposite(direction)).getY());
                    upperRight = new Point(lowerRight.getX(), verticalTracker.getNeighbor(opposite(direction)).getY());
                } else {
                    upperLeft = new Point(lowerLeft.getX(), verticalTracker.getY());
                    upperRight = new Point(lowerRight.getX(), verticalTracker.getY());
                }
                outerSpace.add(upperLeft);
                outerSpace.add(upperRight);
            }

            // Creates the upperLeft and upperRight point for west or east edge.
            if (lowerLeft.getX() == lowerRight.getX()) {
                Point upperLeft, upperRight;
                if (sideBackTracker.getY() != lowerLeft.getY()) {
                    upperLeft = new Point(verticalTracker.getNeighbor(opposite(direction)).getX(), lowerLeft.getY());
                    upperRight = new Point(verticalTracker.getNeighbor(opposite(direction)).getX(), lowerRight.getY());
                } else {
                    upperLeft = new Point(verticalTracker.getX(), lowerLeft.getY());
                    upperRight = new Point(verticalTracker.getX(), lowerRight.getY());
                }
                outerSpace.add(upperLeft);
                outerSpace.add(upperRight);
            }
        }
        return outerSpace;
    }

    private int distance(Point a, Point b) {
        int distanceX = Math.abs(a.getX() - b.getX());
        int distanceY = Math.abs(a.getY() - b.getY());
        return Math.max(distanceX, distanceY);
    }

    private boolean explorableBackLeft(Point verticalTracker, Point sideBackTracker, String direction, Point lowerLeft, Point lowerRight) {
        return sideBackTracker.getNeighbor(left(direction)) != null
                && getTile(sideBackTracker.getNeighbor(left(direction))).description().equals(Tileset.NOTHING.description())
                && Math.abs(sideBackTracker.getX() - verticalTracker.getX()) < distance(lowerLeft, lowerRight)
                && Math.abs(sideBackTracker.getY() - verticalTracker.getY()) < distance(lowerLeft, lowerRight);
    }

    private boolean explorableUp(Point rightTracker, Point verticalTracker, String direction) {
        return verticalTracker.getNeighbor(direction) != null
                && getTile(verticalTracker.getNeighbor(direction)).description().equals(Tileset.NOTHING.description())
                && Math.abs(verticalTracker.getX() - rightTracker.getX()) <= hallwayHeightMax - 2
                && Math.abs(verticalTracker.getY() - rightTracker.getY()) <= hallwayHeightMax - 2;
    }

    private boolean explorableLeft(Point newDoor, Point leftTracker, String direction) {
        return leftTracker.getNeighbor(left(direction)) != null
                && getTile(leftTracker.getNeighbor(left(direction))).description().equals(Tileset.NOTHING.description())
                && Math.abs(leftTracker.getX() - newDoor.getX()) < hallwayHeightMax - 2
                && Math.abs(leftTracker.getY() - newDoor.getY()) < hallwayHeightMax - 2;
    }

    private boolean explorableRight(Point newDoor, Point rightTracker, String direction) {
        return rightTracker.getNeighbor(right(direction)) != null
                && getTile(rightTracker.getNeighbor(right(direction))).description().equals(Tileset.NOTHING.description())
                && Math.abs(rightTracker.getX() - newDoor.getX()) < hallwayHeightMax - 2
                && Math.abs(rightTracker.getY() - newDoor.getY()) < hallwayHeightMax - 2;
    }

    /* Given a door on the edge, the edge is explorable when it's
     * 1. has at least 3 × 3 space outside the door
     * 2. has at least one column to the left of the door
     * 3. has at least one column to the right of the door
     */
    private boolean explorable(Edge edge, Point door) {
        String direction = edge.getDirection();
        Point verticalTracker = new Point(door.getX(), door.getY());

        for(int i = 0; i < hallwayWidth; i++) {
            if (explorableUp(door, verticalTracker, direction)) {
                verticalTracker = verticalTracker.getNeighbor(direction);
                if (!explorableLeft(verticalTracker, verticalTracker, direction)) { return false; }
                if (!explorableRight(verticalTracker, verticalTracker, direction)) { return false; }
            } else { return false; }
        }
        return true;
    }

    private String opposite(String direction) {
        return switch (direction) {
            case "north" -> "south";
            case "south" -> "north";
            case "west" -> "east";
            case "east" -> "west";
            default -> throw new IllegalArgumentException("The input direction is invalid");
        };
    }

    private String left(String direction) {
        return switch (direction) {
            case "north"-> "west";
            case "south" -> "east";
            case "west" -> "south";
            case "east" -> "north";
            default -> throw new IllegalArgumentException("The input direction is invalid");
        };
    }

    private String right(String direction) {
        return switch (direction) {
            case "north" -> "east";
            case "south" -> "west";
            case "west" -> "north";
            case "east" -> "south";
            default -> throw new IllegalArgumentException("The input direction is invalid");
        };
    }

    private int getDoorX(Edge edge) {
        String direction = edge.getDirection();
        int minX = edge.getStartX();
        int maxX = edge.getEndX();
        if (direction.equals("north") || direction.equals("south")) { return randomInt(minX, maxX); }
        else { return minX; }
    }

    private int getDoorY(Edge edge) {
        String direction = edge.getDirection();
        int minY = edge.getStartY();
        int maxY = edge.getEndY();
        if (direction.equals("west") || direction.equals("east")) { return randomInt(minY, maxY); }
        else { return minY; }
    }

    private Edge getEdge(int index) { return connectableEdges.get(index); }
    private Point getFloorPoint(int index) { return floorPointList.get(index); }

    /* Adds the first room,
     * Stores its four edge to the Edges HashMap.
     */
    private void addFirstRoom() {
        int startX = randomInt(10, 70);
        int startY = randomInt(10, 20);

        int width = randomInt(roomWidthMin, roomWidthMax);
        int height = randomInt(roomHeightMin, roomHeightMax);

        int endX = startX + width - 1;
        int endY = startY + height - 1;

        fillSpace(startX, startY, endX, endY);

        // Updates the filledArea and filledRatio
        updateFilledArea(width, height);
        updateFilledRatio();

        // Creates the four edges and add them to the edges map.
        List<Edge> fourEdges = createFourEdges(startX, startY, endX, endY);
        connectableEdges.addAll(fourEdges);
    }

    /* Creates the four edges of a space. */
    private List<Edge> createFourEdges(int minX, int minY, int maxX, int maxY) {
        List<Edge> fourEdges = new ArrayList<>();
        Edge north = new Edge(minX + 1, maxY, maxX - 1, maxY, "north");
        Edge south = new Edge(minX + 1, minY, maxX - 1, minY, "south");
        Edge west = new Edge(minX, minY + 1, minX, maxY - 1, "west");
        Edge east = new Edge(maxX, minY + 1, maxX, maxY - 1, "east");
        fourEdges.add(north);
        fourEdges.add(south);
        fourEdges.add(west);
        fourEdges.add(east);
        return fourEdges;
    }

    /* Fills the walls and floor of a space. */
    private void fillSpace(int minX, int minY, int maxX, int maxY) {
        // Fills the North and South walls
        for (int x = minX; x <= maxX; x++) {
            myWorld[x][minY] = Tileset.WALL;
            myWorld[x][maxY] = Tileset.WALL;
        }

        // Fills the West and East walls
        for (int y = minY; y <= maxY; y++) {
            myWorld[minX][y] = Tileset.WALL;
            myWorld[maxX][y] = Tileset.WALL;
        }

        // Fills the floors
        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                myWorld[x][y] = Tileset.FLOOR;
                floorPointList.add(new Point(x, y));
            }
        }
    }

    private TETile getTile(Point point) { return this.myWorld[point.getX()][point.getY()]; }

    private static class Point {
        int x, y;

        public Point(int x, int y) {
            if (x < 0 || x >= WIDTH) { throw new IllegalArgumentException("The x coordinate must be 0 - 79 inclusively."); }
            if (y < 0 || y >= HEIGHT) { throw new IllegalArgumentException("The y coordinate must be 0 - 29 inclusively."); }
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }

        public int getY() { return y; }

        public Point getNeighbor(String direction) {
            int x = getX();
            int y = getY();
            switch (direction) {
                case "north" -> {
                    if (y == HEIGHT - 1) { return null; }
                    return new Point(x, y + 1);
                }
                case "south" -> {
                    if (y == 0) { return null; }
                    return new Point(x, y - 1);
                }
                case "west" -> {
                    if (x == 0) { return null; }
                    return new Point(x - 1, y);
                }
                case "east" -> {
                    if (x == WIDTH - 1) { return null; }
                    return new Point(x + 1, y);
                }
                default -> throw new IllegalArgumentException("The input direction is invalid.");
            }
        }
    }

    private static class Edge {
        int startX, startY, endX, endY;
        String direction;

        public Edge(int startX, int startY, int endX, int endY, String direction) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.direction = direction;
        }

        public int getStartX() { return startX; }

        public int getStartY() { return startY; }

        public int getEndX() { return endX; }

        public int getEndY() { return endY; }

        public String getDirection() { return direction; }
    }

    private void updateFilledArea(int width, int height) { filledArea += width * height; }

    private void updateFilledRatio() { filledRatio = (double) filledArea / (WIDTH * HEIGHT); }

    /* Creates a random integer generator that will be used for the entire process. */
    private int randomInt(int min, int max) { return min + rd.nextInt(max - min + 1); }

    /* Initializes myWorld with default NOTHING tiles. */
    private void initializeMyWorld() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                myWorld[i][j] = Tileset.NOTHING;
            }
        }
    }

    /**
     * @param input is in the format "N#######S" where each # is a digit and there can be an arbitrary number of #s.
     * @return the integer digits from the input as the random seed.
     */
    private int getSeed(String input) {

        char firstChar = input.charAt(0);
        if (Character.toUpperCase(firstChar) != 'N') { throw new IllegalArgumentException("The input must start with 'N', case insensitive."); }
        else { seedTracker += 'N'; }

        StringBuilder seed = new StringBuilder();
        for (int index = 1; index < input.length() - 1; index++) {
            char c = input.charAt(index);
            if (!Character.isDigit(c) && Character.toUpperCase(c) != 'S') {
                throw new IllegalArgumentException ("The input must end with 'S' (case insensitive) and contain only digits between 'n' and 's'.");
            }
            if (Character.toUpperCase(c) == 'S') { break ; }
            if (Character.isDigit(c)) { seed.append(c); }
        }
        seedTracker += seed.toString();
        seedTracker += 'S';
        return Integer.parseInt(seed.toString());
    }

    private void displayHUD() {
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(50, HEIGHT + yBuffer * 0.5, 50, yBuffer * 0.5);
        int x = (int) Math.floor(StdDraw.mouseX());
        int y = (int) Math.floor(StdDraw.mouseY());
        String description;
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            description = "";
        } else if (((Math.abs(x-avatarA.getX()) > xVision) || (Math.abs(y-avatarA.getY()) > yVision)) && ((Math.abs(x-avatarB.getX()) > xVision) || (Math.abs(y-avatarB.getY()) > yVision))) {
            description = "unknown";
        } else {
            description = myWorld[x][y].description();
        }
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(40, HEIGHT + yBuffer*0.8, description);
        StdDraw.text(20, HEIGHT + yBuffer*0.2, messageHUDPlayerA);
        StdDraw.text(60, HEIGHT + yBuffer*0.2, messageHUDPlayerB);
        StdDraw.text(10, HEIGHT + yBuffer*0.8, "playerRed Control: " + allowedMovementsA.toString());
        StdDraw.text(10, HEIGHT + yBuffer*0.2, "playerRed Score: " + playerRedScore);
        StdDraw.text(70, HEIGHT + yBuffer*0.8, "playerBlue Control: " + allowedMovementsB.toString());
        StdDraw.text(70, HEIGHT + yBuffer*0.2, "playerBlue Score: " + playerBlueScore);

        StdDraw.show();
    }

    private void initializeAvatar() {
        avatarA = getFloorPoint(rd.nextInt(floorPointList.size()));
        myWorld[avatarA.getX()][avatarA.getY()] = Tileset.AVATARA;
        floorPointList.remove(avatarA);
        avatarB = getFloorPoint(rd.nextInt(floorPointList.size()));
        myWorld[avatarB.getX()][avatarB.getY()] = Tileset.AVATARB;
        floorPointList.remove(avatarB);
    }

    private void initializeMagic(int magicCount) {
        int initializeMagicCount = magicCount;
        while(initializeMagicCount > 0) {
            Point p = getFloorPoint(rd.nextInt(floorPointList.size()));
            int i = p.getX();
            int j = p.getY();
            boolean validHint = true;
            if (!myWorld[i][j].description().equals(Tileset.FLOOR.description())) { continue; }
            if (!magicPointList.isEmpty()) {
                for (Point magic: magicPointList) {
                    if (distance(p, magic) < 10.0) {
                        if (rd.nextInt(2) == 0) {
                            validHint = false;
                            break;
                        }
                    }
                }
            }
            if (validHint) {
                myWorld[i][j] = Tileset.MAGIC;
                magicPointList.add(p);
                floorPointList.remove(p);
                initializeMagicCount -= 1;
            }
            if (initializeMagicCount == 0) { return; }
        }
    }

    private void initializeHint(int hintCount) {
        int initializeHintCount = hintCount;
        while(initializeHintCount > 0) {
            Point p = getFloorPoint(rd.nextInt(floorPointList.size()));
            int i = p.getX();
            int j = p.getY();
            boolean validHint = true;
            if (!myWorld[i][j].description().equals(Tileset.FLOOR.description())) { continue; }
            if ((p.getX() == exitDoor.getX()) || p.getY() == exitDoor.getY()) { continue; }
            for (Point door : exitDoorPointList) {
                if ((p.getX() == door.getX()) || p.getY() == door.getY()) {
                    validHint = false;
                    break;
                }
            }
            if (!validHint) { continue; }
            if (!hintPointList.isEmpty()) {
                for (Point hint: hintPointList) {
                    if (distance(p, hint) < 10.0) {
                        if (rd.nextInt(2) == 0) {
                            validHint = false;
                            break;
                        }
                    }
                }
            }
            if (!validHint) { continue; }
            myWorld[i][j] = Tileset.HINT;
            hintPointList.add(p);
            floorPointList.remove(p);
            initializeHintCount -= 1;
            if (initializeHintCount == 0) { return; }
        }
    }

    private void initializeCoin(double coinProb) {
        for (Point p : floorPointList) {
            if (rd.nextDouble() < coinProb) {
                myWorld[p.getX()][p.getY()] = Tileset.COIN;
                coinPointList.add(p);
            }
        }
        floorPointList.removeAll(coinPointList);
    }

    private void initializeLoopWay() {
        List<Edge> connectedEdgesInLoop = new ArrayList<>();
        for (Edge edge : connectableEdges) {
            if (connectedEdgesInLoop.contains(edge)) { continue; }
            String direction = edge.getDirection();
            int x = getDoorX(edge);
            int y = getDoorY(edge);
            Point p = new Point(x, y);
            if ((p.getNeighbor(direction) != null) &&
                    ((getTile(p.getNeighbor(direction))).description().equals(Tileset.WALL.description()))) {
                Point q = p.getNeighbor(direction);
                Edge edgeConnect = findConnectableEdge(q);
                if (edgeConnect == null) { continue; }
                Point a = q.getNeighbor(left(direction));
                Point b = q.getNeighbor(right(direction));
                if ((a != null) && (getTile(a).description().equals(Tileset.WALL.description())) &&
                        (b != null) && (getTile(b).description().equals(Tileset.WALL.description()))) {
                    if (rd.nextInt(2) == 0) {
                        myWorld[p.getX()][p.getY()] = Tileset.FLOOR;
                        myWorld[q.getX()][q.getY()] = Tileset.FLOOR;
                        floorPointList.add(p);
                        floorPointList.add(q);
                        connectedEdgesInLoop.add(edge);
                        connectedEdgesInLoop.add(edgeConnect);
                    }
                }
            }
        }
    }

    private Edge findConnectableEdge(Point p) {
        Edge returnEdge = null;
        for (Edge edge : connectableEdges) {
            if ((edge.getStartX() <= p.getX()) && (edge.getEndX() >= p.getX()) &&
                    (edge.getStartY() <= p.getY()) && (edge.getEndY() >= p.getY())) {
                returnEdge = edge;
            }
        }
        return returnEdge;
    }




    private void interactWithHint(int playerIndex) {
        Point avatar;
        if (playerIndex == 0) {
            avatar = avatarA;
        } else {
            avatar = avatarB;
        }

        String messageHUD;
        messageHUD = "You get a Hint: ";
        if (exitDoor.getX() < avatar.getX()) {
            messageHUD += " W ";
        } else if (exitDoor.getX() > avatar.getX()) {
            messageHUD += " E ";
        }

        if (exitDoor.getY() < avatar.getY()) {
            messageHUD += " S ";
        } else if (exitDoor.getY() > avatar.getY()) {
            messageHUD += " N ";
        }

        if (playerIndex == 0) {
            messageHUDPlayerA = messageHUD;
        } else {
            messageHUDPlayerB = messageHUD;
        }
    }

    private void interactWithMagic(int playerIndex) {
        int avatarAX = avatarA.getX();
        int avatarAY = avatarA.getY();
        int avatarBX = avatarB.getX();
        int avatarBY = avatarB.getY();
        avatarA = new Point(avatarBX, avatarBY);
        avatarB = new Point(avatarAX, avatarAY);
        myWorld[avatarA.getX()][avatarA.getY()] = Tileset.AVATARA;
        myWorld[avatarB.getX()][avatarB.getY()] = Tileset.AVATARB;
        displayFrame();
    }

    private void interactWithCoin(int playerIndex) {
        if (playerIndex == 0) {
            playerRedScore += coinValue;
        } else {
            playerBlueScore += coinValue;
        }
    }



    public void move(char moveDirection, Point avatar, int playerIndex) {
        String moveDirectionString = moveDirectionFinder(moveDirection, playerIndex);
        if (avatar.getNeighbor(moveDirectionString) == null) {
        } else if (getTile(avatar.getNeighbor(moveDirectionString)).description().equals(Tileset.FLOOR.description())) {
            moveAvatar(moveDirectionString, playerIndex);
            activityTracker += moveDirection;
        } else if (getTile(avatar.getNeighbor(moveDirectionString)).description().equals(Tileset.HINT.description())) {
            moveAvatar(moveDirectionString, playerIndex);
            activityTracker += moveDirection;
            interactWithHint(playerIndex);
        } else if (getTile(avatar.getNeighbor(moveDirectionString)).description().equals(Tileset.MAGIC.description())) {
            moveAvatar(moveDirectionString, playerIndex);
            activityTracker += moveDirection;
            interactWithMagic(playerIndex);
        }  else if (getTile(avatar.getNeighbor(moveDirectionString)).description().equals(Tileset.COIN.description())) {
            moveAvatar(moveDirectionString, playerIndex);
            activityTracker += moveDirection;
            interactWithCoin(playerIndex);
        } else if (getTile(avatar.getNeighbor(moveDirectionString)).description().equals(Tileset.LOCKED_DOOR.description())) {
            moveAvatar(moveDirectionString, playerIndex);
            activityTracker += moveDirection;
            endGame(playerIndex);
        }
    }

    private void moveAvatar(String moveDirectionString, int playerIndex) {
        if (playerIndex == 0) {
            myWorld[avatarA.getX()][avatarA.getY()] = Tileset.FLOOR;
            avatarA = avatarA.getNeighbor(moveDirectionString);
            myWorld[avatarA.getX()][avatarA.getY()] = Tileset.AVATARA;
        } else {
            myWorld[avatarB.getX()][avatarB.getY()] = Tileset.FLOOR;
            avatarB = avatarB.getNeighbor(moveDirectionString);
            myWorld[avatarB.getX()][avatarB.getY()] = Tileset.AVATARB;
        }
    }

    private String moveDirectionFinder(char moveDirection, int playerIndex){
        if (playerIndex == 0) {
            return switch (moveDirection) {
                case 'A' -> "west";
                case 'W' -> "north";
                case 'S' -> "south";
                case 'D' -> "east";
                default -> throw new IllegalArgumentException("Only accept key 'A', 'W', 'S' and 'D'.");
            };
        } else {
            return switch (moveDirection) {
                case 'J' -> "west";
                case 'I' -> "north";
                case 'K' -> "south";
                case 'L' -> "east";
                default -> throw new IllegalArgumentException("Only accept key 'J', 'I', 'K' and 'L'.");
            };
        }
    }

    public void displayFrame() {
        //ter.renderFrame(myWorld);
        ter.renderNearFrameTwoPlayer(myWorld, avatarA.getX(), avatarA.getY(), avatarB.getX(), avatarB.getY(), xVision, yVision);
    }

    public void playGame() {
        displayFrame();
        boolean stageForExit = false;
        while (true) {
            displayHUD();
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (allowedMovementsA.contains(c)) {
                    move(c, avatarA, 0);
                    displayFrame();
                    stageForExit = false;
                } else if (allowedMovementsB.contains(c)) {
                    move(c, avatarB, 1);
                    displayFrame();
                    stageForExit = false;
                } else if (c == ':') {
                    stageForExit = true;
                } else if (c == 'Q' && stageForExit) {
                    saveThenQuit();
                } else {
                    stageForExit = false;
                }
            }
        }
    }


    public void endGame(int playerIndex) {
        String endGameMessage = "";
        String winnerMessage = "";
        if (playerIndex == 0) {
            if ((avatarA.getX() == exitDoor.getX()) && (avatarA.getY() == exitDoor.getY())) {
                playerRedScore += exitValue;
                endGameMessage = "YEAH! " + playerList.get(playerIndex) + " found the right exit!";
            } else {
                playerRedScore -= exitValue;
                endGameMessage = "OOPS! " + playerList.get(playerIndex) +  " found the wrong exit!";
            }
        }

        if (playerIndex == 1) {
            if ((avatarB.getX() == exitDoor.getX()) && (avatarB.getY() == exitDoor.getY())) {
                playerBlueScore += exitValue;
                endGameMessage = "YEAH! " + playerList.get(playerIndex) + " found the right exit!";
            } else {
                playerBlueScore -= exitValue;
                endGameMessage = "OOPS! " + playerList.get(playerIndex) +  " found the wrong exit!";
            }
        }

        Color winnerColor = StdDraw.WHITE;
        if (playerRedScore > playerBlueScore) {
            winnerMessage = playerList.get(0) + " WON! ";
            winnerColor = StdDraw.RED;
        } else if (playerRedScore < playerBlueScore) {
            winnerMessage = playerList.get(1) + " WON! ";
            winnerColor = StdDraw.BLUE;
        } else {
            winnerMessage = "Draw Game!";
        }

        Font headerFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(headerFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 + 8, endGameMessage);
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 8, "Press 'r' to restart. Press 'n' to play new game. Press any other key to exit.");

        Font headerFontLarge = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(headerFontLarge);
        StdDraw.setPenColor(winnerColor);
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 + 4, winnerMessage);

        StdDraw.setFont(headerFont);
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.text((double) WIDTH / 2 - 20, (double) HEIGHT / 2, "playerRed Score: " + playerRedScore);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.text((double) WIDTH / 2 + 20, (double) HEIGHT / 2, "playerBlueScore: " + playerBlueScore);

        StdDraw.show(200);
        InputSource inputSource = new KeyboardInputSource();
        char c = inputSource.getNextKey();
        if (c == 'R') {
            String seed = seedTracker;
            resetGame();
            interactWithInputString(seed);
            playGame();
        }
        if (c == 'N') {
            String seed = seedTracker;
            int seedNumber = getSeed(seedTracker);
            seedNumber += 1;
            resetGame();
            interactWithInputString("N" + seedNumber + "S");
            playGame();
        }
        System.exit(0);
    }

    public void resetGame() {
        filledArea = 0;
        filledRatio = 0.0;
        connectableEdges = new LinkedList<>();
        floorPointList = new LinkedList<>();
        doorPointList = new LinkedList<>();
        seedTracker = "";
        activityTracker = "";
        exitDoorPointList = new LinkedList<>();
        hintPointList = new LinkedList<>();
        magicPointList = new LinkedList<>();
        coinPointList = new LinkedList<>();
        messageHUDPlayerA = "";
        messageHUDPlayerB = "";
        playerRedScore = 0;
        playerBlueScore = 0;
    }

}
