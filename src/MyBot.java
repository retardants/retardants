import java.io.IOException;
import java.util.*; 
import org.retardants.adt.*;
import org.retardants.diffusion.DiffusionMap;

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
    private Set<Tile> enemyHills = new HashSet<Tile>();
    
    private Strategy explorationStrategy = Strategy.EXPLORATION_LEAST_VISITED;
    private int turn = 0;



    DiffusionMap diffusionMap;


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

        super.setup(
            loadTime,
            turnTime,
            rows,
            cols,
            turns,
            viewRadius2,
            attackRadius2,
            spawnRadius2);

        Ants ants = getAnts();
        if (ants == null)
            throw new RuntimeException("Null value for ants");

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

    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move
     * it if the tile is passable.
     */
    @Override
    public void doTurn() {
        System.err.println("=====  TURN " + turn++ + " =====");

        Ants ants = getAnts();
        Set<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        allOrders.clear(); 

        // Initialize unseen tiles set.
        if (unseenTiles == null) {
            unseenTiles = new HashSet<Tile>();
            for (int row = 0; row < ants.getRows(); row++)
                for (int col = 0; col < ants.getCols(); col++)
                    unseenTiles.add(new Tile(row, col));
        }

        // Remove tiles that we're seeing this turn.
        for (Iterator<Tile> iter = unseenTiles.iterator(); iter.hasNext();) {
            Tile next = iter.next();
            if (ants.isVisible(next)) {
                iter.remove();
            }	
        }
        
        // Initialize visited tile map.
        if (visitedTiles == null) {
            visitedTiles = new HashMap<Tile, Integer>();
            for (int row = 0; row < ants.getRows(); row++) {
                for (int col = 0; col < ants.getCols(); col++) {
                    visitedTiles.put(new Tile(row, col), 0);
                }
            }
        }
        
        // Update visited tile map
        for (Tile antLoc : sortedAnts) {
            visitedTiles.put(antLoc, visitedTiles.get(antLoc) + 1);
        }

        // Don't step on own hill.
        for (Tile myHill : ants.getMyHills()) {
            allOrders.put(myHill, null);
        }

        // Add newly seen enemy hill.
        // TODO(ipince): remove hills we've already killed.
        for (Tile enemyHill : ants.getEnemyHills()) {
            if (!enemyHills.contains(enemyHill)) {
                enemyHills.add(enemyHill);
            }
        }
        
        // === BATTLE ===

        System.err.println("BATTLE");
        // Build routes between every enemy hill and every ant.
        List<Route> hillRoutes = new ArrayList<Route>();
        for (Tile hillLoc : enemyHills) { 
            for (Tile antLoc : sortedAnts) { 
                if (! allOrders.containsValue(antLoc)) {
                    int distance = ants.getDistance(antLoc, hillLoc);
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
        
        // === FOOD ===

        Map<Tile, Tile> foodOrders = new HashMap<Tile, Tile>();
        List<Route> foodRoutes = new ArrayList<Route>();
        Set<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());

        // Build routes between every food and every ant.

        diffusionMap.timeStep(100, ants.getFoodTiles());
        /* For each ant, move in the direction of increasing diffusion value */

        for (Tile antLoc : sortedAnts) {
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

            if (! allOrders.containsValue(antLoc) && bestAim != null)
                doMoveDirection(antLoc, bestAim);
        }


  /*      // TODO(ipince): why do we care that the food/ants be sorted?
        for (Tile foodLoc : sortedFood) {
            for (Tile antLoc : sortedAnts) {
                if (! allOrders.containsValue(antLoc)) {
                    int distance = ants.getDistance(antLoc, foodLoc);
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
             */


        // === EXPLORATION ===

        System.err.println("EXPLORATION");
        switch (explorationStrategy) {
        case EXPLORATION_LEAST_VISITED:
            exploreLeastVisited(sortedAnts);
            break;
        case EXPLORATION_NEAREST_UNSEEN:
            exploreNearestUnseen(sortedAnts);
        }

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
    }
    
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
}
