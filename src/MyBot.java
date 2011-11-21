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
    private Set<Tile> unseenTiles;
    private Set<Tile> enemyHills = new HashSet<Tile>();



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
            return true;
        } else {
            return false;
        }
    }

    private boolean doMoveLocation(Tile antLoc, Tile destLoc) {
        Ants ants = getAnts();
        List<Aim> directions = new ArrayList<Aim>(ants.getDirections(antLoc, destLoc)); 
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
        for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext();) {
            Tile next = locIter.next();
            if (ants.isVisible(next)) {
                locIter.remove(); 
            }	
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
            doMoveLocation(route.getStart(), route.getEnd());
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
            }
        }
             */


        // === EXPLORATION ===

        // For each ant that doens't have an order yet, make it go to the closest
        // unseen tile. 
        for (Tile antLoc : sortedAnts) {
            if (! allOrders.containsValue(antLoc)) {
                List<Route> unseenRoutes = new ArrayList<Route>();
                for (Tile unseenLoc : unseenTiles) {
                    int distance = ants.getDistance(antLoc, unseenLoc);
                    Route route = new Route(antLoc, unseenLoc, distance);
                    unseenRoutes.add(route);
                }
                Collections.sort(unseenRoutes);
                for (Route route : unseenRoutes) {
                    if (doMoveLocation(route.getStart(), route.getEnd()))
                        break;
                }
            }
        }

        // Move ants off our hills in a random direction. 
        for (Tile myHill : ants.getMyHills()) {
            if (ants.getMyAnts().contains(myHill) 
                    && ! allOrders.containsValue(myHill)) {
                List<Aim> aims = new ArrayList<Aim>(Arrays.asList(Aim.values()));
                Collections.shuffle(aims);
                for (Aim direction : aims) {
                    if (doMoveDirection(myHill, direction))
                        break;
                }
            }
        }
    }
}
