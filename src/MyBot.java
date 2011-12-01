import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Bot;
import org.retardants.adt.Tile;
import org.retardants.bot.BattleTaskCreator;
import org.retardants.bot.BotTask;
import org.retardants.bot.DiffusionTaskCreator;
import org.retardants.bot.TaskManager;
import org.retardants.diffusion.DiffusionMap;
import org.retardants.path.PathMap;
import org.retardants.path.TilePath;
import org.retardants.util.Logger;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

    /**
     * Main method executed by the game engine for starting the bot.
     *
     * @param args command line arguments
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }

    // Key: location to move to.
    // Value: location of ant moving to its key.
    private Map<Tile, Tile> allOrders = new HashMap<Tile, Tile>();
    private Map<Tile, Integer> visitedTiles;
    private Set<Tile> unseenTiles;

    private int turn = 0;

    //DiffusionMap diffusionMap;
    TaskManager taskManager = new TaskManager();
    DiffusionTaskCreator diffusionTaskCreator = new DiffusionTaskCreator();
    BattleTaskCreator battleTaskCreator = new BattleTaskCreator();


    @Override
    public void setup(
            int loadTime,
            int turnTime,
            int rows,
            int cols,
            int turns,
            int viewRadius2,
            int attackRadius2,
            int spawnRadius2) {

        super.setup(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2, spawnRadius2);

        Ants ants = getAnts();
        diffusionTaskCreator.init(ants);
        battleTaskCreator.init(ants);
    }

    /**
     * Attempts to move an ant in the given direction. Fails iff
     * the Tile is occupied or if we've already issued an order for
     * another ant to occupy the destination tile in this turn.
     * TODO(ipince): revise definition of isOccupied()
     */
    private boolean doMoveDirection(Tile antLoc, Aim direction) {

        Ants ants = getAnts();
        Tile newLoc = ants.getTile(antLoc, direction);

        if (ants.getIlk(newLoc).isUnoccupied()
                && ! allOrders.containsKey(newLoc)) {
            ants.issueOrder(antLoc, direction);
            allOrders.put(newLoc, antLoc);
            System.err.print("\tAnt at " + antLoc + " moving " + direction);
            return true;
        } else {
            return false;
        }
    }

    private boolean doMoveLocation(Tile antLoc, Tile destLoc) {
        Ants ants = getAnts();
        List<Aim> directions =
                new ArrayList<Aim>(ants.getDirections(antLoc, destLoc));
        Collections.shuffle(directions);
        for (Aim direction : directions) {
            if (doMoveDirection(antLoc, direction))
                return true;
        }
        return false;
    }




    /**
     * Called at the beginning of the turn, use this method to (re)-initialize
     * class fields to sane values (such as keeping tracks of tiles to skip)
     *
     * @param ants The ant context
     */
    public void turnInit(Ants ants) {

        allOrders.clear();
        taskManager.newTurn();

        Set<Tile> allAnts = new TreeSet<Tile>(ants.getMyAnts());
        // Initialize unseen tiles set.
        if (unseenTiles == null) {
            unseenTiles = new HashSet<Tile>();
            for (int row = 0; row < ants.getRows(); row++)
                for (int col = 0; col < ants.getCols(); col++)
                    unseenTiles.add(new Tile(row, col));
        }
        Logger.log("unseen", ants);

        // Remove tiles that we're seeing this turn.
        for (Iterator<Tile> iter = unseenTiles.iterator(); iter.hasNext();) {
            Tile next = iter.next();
            if (ants.isVisible(next)) {
                iter.remove();
            }
        }
        Logger.log("seen", ants);

        // Initialize visited tile map.
        if (visitedTiles == null) {
            visitedTiles = new HashMap<Tile, Integer>();
            for (int row = 0; row < ants.getRows(); row++) {
                for (int col = 0; col < ants.getCols(); col++) {
                    visitedTiles.put(new Tile(row, col), 0);
                }
            }
        }
        Logger.log("visited", ants);

        // Update visited tile map and enemy hills set.
        for (Tile antLoc : allAnts) {
            visitedTiles.put(antLoc, visitedTiles.get(antLoc) + 1);
        }

        // Don't step on own hill.
        for (Tile myHill : ants.getMyHills()) {
            allOrders.put(myHill, null);
        }

    }


    /**
     * Add a set of all feasible commands for each ant for this turn.
     *
     * During the execution of this method, no movement should actually be
     * performed (with doMoveDirection). Instead, this method should insert
     * the commands into the TaskManager.
     *
     * @param ants The ant context.
     */
    public void generateCommands(Ants ants) {

        Set<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        // Food and exploration
        diffusionTaskCreator.createTasks(taskManager, ants);
        Logger.log("food", ants);

        // Battle
        System.err.println("BATTLE (" + ants.getEnemyHills().size() + " known hills)");
        battleTaskCreator.createTasks(taskManager, ants);
        Logger.log("battle", ants);


        // Move ants off our hills in a random direction.
        System.err.println("ANTS OFF HILL");
        for (Tile myHill : getAnts().getMyHills()) {
            if (sortedAnts.contains(myHill)
                    && ! allOrders.containsValue(myHill)) {
                List<Aim> directions =
                        new ArrayList<Aim>(Arrays.asList(Aim.values()));
                Collections.shuffle(directions);
                for (Aim direction : directions) {
                    if (doMoveDirection(myHill, direction)) {
                        System.err.println();
                        break;
                    }
                }
            }
        }

        Logger.log("turn is done", ants);
    }


    public void processCommands(Ants ants) {

         for (Tile ant : ants.getMyAnts()) {
             BotTask task = null;
             do {
                task = taskManager.pollCommand(ant);
             }
             while (task != null && ! doMoveLocation(ant, task.getDestination()));
         }
    }

    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move
     * it if the tile is passable.
     */
    @Override
    public void doTurn() {
        System.err.println("=====  TURN " + ++turn + " =====");
        Logger.log("nothing", getAnts());

        Ants ants = getAnts();

        turnInit(ants);
        generateCommands(ants);
        processCommands(ants);


    }
}
