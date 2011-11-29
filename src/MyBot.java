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
import org.retardants.bot.BotTask;
import org.retardants.bot.TaskManager;
import org.retardants.diffusion.DiffusionMap;
import org.retardants.path.PathMap;
import org.retardants.path.TilePath;

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

    DiffusionMap diffusionMap;
    TaskManager taskManager = new TaskManager();



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
        diffusionMap = new DiffusionMap(ants.getCols(), ants.getRows(), ants);
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

    private void printTime(String msg) {
        System.err.println("Remaining after " + msg + ": " + getAnts().getTimeRemaining());
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
        printTime("unseen");

        // Remove tiles that we're seeing this turn.
        for (Iterator<Tile> iter = unseenTiles.iterator(); iter.hasNext();) {
            Tile next = iter.next();
            if (ants.isVisible(next)) {
                iter.remove();
            }
        }
        printTime("seen");

        // Initialize visited tile map.
        if (visitedTiles == null) {
            visitedTiles = new HashMap<Tile, Integer>();
            for (int row = 0; row < ants.getRows(); row++) {
                for (int col = 0; col < ants.getCols(); col++) {
                    visitedTiles.put(new Tile(row, col), 0);
                }
            }
        }
        printTime("visited");

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
        // === FOOD ===

        // TODO(ipince): fix timings. We should probably calculate TilePaths first, but issue
        // orders later.
        Set<Tile> candies = ants.getFoodTiles();
        System.err.println("FOOD (" + candies.size() + " candies)");
        foodDiffusionAllAnts(sortedAnts);
        printTime("food and exploration");

        // === BATTLE ===
        System.err.println("BATTLE (" + ants.getEnemyHills().size() + " known hills)");
        battleDijkstrasTilePath(sortedAnts);
        printTime("battle");


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

        printTime("turn is done");
    }


    public void processCommands(Ants ants) {

         for (Tile ant : ants.getMyAnts()) {
             BotTask task = null;
             do {
                task = taskManager.pollCommand(ant);
                 System.err.println("Inspecitng task " + task);
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
        printTime("nothing");

        Ants ants = getAnts();


        turnInit(ants);
        generateCommands(ants);
        processCommands(ants);


    }

    private void battleDijkstrasTilePath(Set<Tile> sortedAnts) {
        Map<Tile, Set<TilePath>> paths = null;
        for (Tile hillLoc : getAnts().getEnemyHills()) {
            // For each enemy hill, find the shortest tile-path to each ant.
            // Use at most 50% of the time.
            paths = PathMap.findBestPaths(
                    getAnts(),
                    hillLoc,
                    sortedAnts,
                    getAnts().getTimeRemaining() / 2);

            // Send each ant to the hill using (one of) the shortest path found.
            for (Tile antLoc : sortedAnts) {
                if (! allOrders.containsValue(antLoc)) {
                    if (paths.containsKey(antLoc)) {
                        for (TilePath path : paths.get(antLoc)) {
                            // Print the path
//                            Iterator<Tile> printIter = path.reverseIterator();
//                            StringBuilder sb = new StringBuilder();
//                            while (printIter.hasNext()) {
//                                sb.append(printIter.next() + " -> ");
//                            }
//                            System.err.println("Path: " + sb.toString());
                            Iterator<Tile> iter = path.reverseIterator();
                            assert iter.hasNext(); iter.next();
                            assert iter.hasNext();


                            taskManager.addTask(
                                    antLoc,
                                    new BotTask(
                                            BotTask.CommandType.BATTLE_COMMAND,
                                            iter.next(),
                                            (int) Math.round(path.cost()))
                            );
                            /*    if (doMoveLocation(antLoc, iter.next())) {
                 System.err.println("; killing HILL at " + path.start() +
                         "; " + (path.cost()-1) + " steps away");
                 break; // from Set iteration
             }               */


                        }
                    }
                }
            }
        }
    }

    /*

    private void battleShortestEuclideanRoute(Set<Tile> sortedAnts) {
        // Build routes between every enemy hill and every ant.
        List<Route> hillRoutes = new ArrayList<Route>();
        for (Tile hillLoc : getAnts().getEnemyHills()) {
            for (Tile antLoc : sortedAnts) {
                if (! allOrders.containsValue(antLoc)) {
                    int distance = getAnts().getDistance(antLoc, hillLoc);
                    Route route = new Route(antLoc, hillLoc, distance);
                    hillRoutes.add(route);
                }
            }
        }

        // Assign all ants to go to the enemy hill, starting with min routes first.
        Collections.sort(hillRoutes);
        for (Route route : hillRoutes) {
            if (doMoveLocation(route.getStart(), route.getEnd())) {
                System.err.println("; killing HILL at " + route.getEnd());
            }
        }
    }


    */



    // JOSE: THIS IS YOUR ORIGINAL IMPLEMENTATION (minor details changed)
    private void foodDiffusionAllAnts(Set<Tile> sortedAnts) {
        Ants ants = getAnts();
        Set<Tile> candies = ants.getFoodTiles();
        // Use at most 50% of the turn time.
        diffusionMap.timeStep(100, candies, ants.getTimeRemaining()/2);

        // For each ant, move in the direction of increasing diffusion value
        for (Tile antLoc : sortedAnts) {
            // No need to calculate value for ants who already have orders.
            if (! allOrders.containsValue(antLoc)) {
                Double bestValue = Double.MIN_VALUE;
                Aim bestAim  = null;

                for (Aim aim : Aim.values()) {
                    Tile neighbor = ants.getTile(antLoc, aim);
                    Double neighborValue = diffusionMap.getValue(neighbor);
                    if (neighborValue > bestValue) {
                        bestValue = neighborValue;
                        bestAim = aim;
                    }
                }

                if (bestAim != null) {
                    int numSteps =
                            (int) Math.round(Math.log(bestValue / 100.00) / Math.log(0.50));

                    taskManager.addTask(
                            antLoc,
                            new BotTask(
                                    (numSteps > 7 ? BotTask.CommandType.EXPLORATION_COMMAND :
                                                    BotTask.CommandType.FOOD_COMMAND),
                                    ants.getTile(antLoc, bestAim),
                                    /* TODO: This needs to be moved to DiffusionMap */
                                    /* if k steps away from a source, then
                                        the diffusion value will be:
                                         (SOURCE) * (DIFF_COEFF)^k = value
                                       Therefore, estimate k as
                                          log_{DIFF_COEFF} (bestValue / SOURCE)
                                     */
                                    numSteps));
                    /* if (doMoveDirection(antLoc, bestAim)) {
                      System.err.println("; moving uphill to value " + bestValue);
                  }  */

                }
            }
        }
    }


    /*
    // TODO(ipince): fix this. Using Routes is hacky.
    private void foodDiffusionOneAntPerFood(Set<Tile> sortedAnts) {
        // TODO(ipince): we probably want to send more ants to food. too many are exploring
        // when there are no enemy hills visible.
        Ants ants = getAnts();
        Set<Tile> candies = ants.getFoodTiles();
        // Use at most 50% of the turn time.
        diffusionMap.timeStep(100, candies, ants.getTimeRemaining()/2);

        // Populate "routes" for each ant going to its neighboring cells, by setting the "distance"
        // to the negative of the diffusion value. (higher value -> smaller distance).
        List<Route> foodRoutes = new ArrayList<Route>();
        for (Tile antLoc : sortedAnts) {
            // No need to calculate value for ants who already have orders.
            if (! allOrders.containsValue(antLoc)) {
                for (Aim aim : Aim.values()) {
                    Tile neighbor = ants.getTile(antLoc, aim);
                    foodRoutes.add(new Route(antLoc, neighbor, (int)Math.round(-diffusionMap.getValue(neighbor))));
                }
            }
        }

        // Sort routes by "distance" (so that routes that go most uphill come first).
        Collections.sort(foodRoutes);

        // If there are n candies, send n ants to towards their nearest candy (indicated
        // by the diffusion scent).
        // TODO(ipince): In an ideal situation, we'd like each ant going to a different candy.
        // In practice, this doesn't seem to happen. Is it because diffusionMap doesn't treat
        // ants as sinks?? Ask Jose (too lazy to read code now).
        int targetedCandies = 0;
        Map<Tile, Tile> foodOrders = new HashMap<Tile, Tile>(); // avoid repeating an ant's orders
        // TODO(ipince): i think the foodOrders map is unnecessary; double-check.
        for (Route route : foodRoutes) {
            // Once we've successfully sent as many ants as there are candies, we're done.
            if (targetedCandies >= candies.size()) {
                break;
            }
            if (! foodOrders.containsValue(route.getStart()) &&
                    doMoveLocation(route.getStart(), route.getEnd())) {
                foodOrders.put(route.getEnd(), route.getStart());
                targetedCandies++;
                System.err.println("; moving uphill to value " + diffusionMap.getValue(route.getEnd()));
            }
        }
    }

    */


    /*

    private void foodShortestEuclideanRoute(Set<Tile> sortedAnts) {
        // Build routes between every food and every ant.
        Map<Tile, Tile> foodOrders = new HashMap<Tile, Tile>();
        List<Route> foodRoutes = new ArrayList<Route>();
        Set<Tile> sortedFood = new TreeSet<Tile>(getAnts().getFoodTiles());

        for (Tile foodLoc : sortedFood) {
            for (Tile antLoc : sortedAnts) {
                if (! allOrders.containsValue(antLoc)) {
                    int distance = getAnts().getDistance(antLoc, foodLoc);
                    Route route = new Route(antLoc, foodLoc, distance);
                    foodRoutes.add(route);
                }
            }
        }

        // Assign one food target to each ant, starting with min routes first.
        Collections.sort(foodRoutes);
        for (Route route : foodRoutes) {
            if (! foodOrders.containsKey(route.getEnd())
                    && ! foodOrders.containsValue(route.getStart())
                    && doMoveLocation(route.getStart(), route.getEnd())) {
                foodOrders.put(route.getEnd(), route.getStart());
                System.err.println("; fetching FOOD at " + route.getEnd());
            }
        }
    }


    */


    /*
    private void exploreNearestUnseen(Set<Tile> sortedAnts) {
        // For each ant that doens't have an order yet, make it go to the closest
        // unseen tile.
        for (Tile antLoc : sortedAnts) {
            if (! allOrders.containsValue(antLoc)) {
                List<Route> unseenRoutes = new ArrayList<Route>();
                for (Tile unseenLoc : unseenTiles) {
                    int distance = getAnts().getDistance(antLoc, unseenLoc);
                    Route route = new Route(antLoc, unseenLoc, distance);
                    unseenRoutes.add(route);
                }
                Collections.sort(unseenRoutes);
                for (Route route : unseenRoutes) {
                    if (doMoveLocation(route.getStart(), route.getEnd())) {
                        System.err.println(
                                "; going to EXPLORE " + route.getEnd());
                        break;
                    }
                }
            }
        }
    }


    private void exploreLeastVisited(Set<Tile> sortedAnts) {
        // For each order-less ant, send it to the least-visited neighboring
        // tile.
        for (final Tile antLoc : sortedAnts) {
            if (! allOrders.containsValue(antLoc)) {
//              System.err.println("Finding EXPLORE target for ant at " + antLoc);
                // Sort directions by num-times-visited.
                List<Aim> directions =
                        new ArrayList<Aim>(Arrays.asList(Aim.values()));
                Collections.shuffle(directions);
                Collections.sort(directions, new Comparator<Aim>() {
                    public int compare(Aim o1, Aim o2) {
                        return visitedTiles.get(getAnts().getTile(antLoc, o1)) -
                                visitedTiles.get(getAnts().getTile(antLoc, o2));
                    }
                });
                for (Aim direction : directions) {
//                    System.err.println("Trying to move " + direction + ",
//                      which has been visited " +
//                      visitedTiles.get(getAnts().getTile(antLoc, direction))
//                      + " times.");
                    if (doMoveDirection(antLoc, direction)) {
                        System.err.println(
                                "; going to EXPLORE "
                                        + getAnts().getTile(antLoc, direction));
                        break;
                    }
                }
            }
        }
    }


    */
}
