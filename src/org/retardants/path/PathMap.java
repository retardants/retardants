package org.retardants.path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Tile;

// TODO(ipince): CLEAN THIS SHIT UP. (Sorry jose).
public class PathMap {
    
    public static void test(Ants ants) {
        Graph<Tile> map = new Graph<Tile>();
        
        // Add nodes
        for (int row = 0; row < ants.getRows(); row++) {
            for (int col = 0; col < ants.getCols(); col++) {
                map.addNode(new Tile(row, col));
            }
        }
        
        // Add edges
        for (int row = 0; row < ants.getRows(); row++) {
            for (int col = 0; col < ants.getCols(); col++) {
                Tile tile = new Tile(row, col);
                for (Aim aim : Aim.values()) {
                    map.addEdge(tile, ants.getTile(tile, aim));
                }
            }
        }
        
        // Want a map of origin -> path(origin, goal)
    }
    
    // Returns a path from origin->goal for each goal provided.
    public static Map<Tile, Set<TilePath>> findBestPaths(Ants ants, Tile origin, Set<Tile> goals,
            long maxTime) {
        long start = System.currentTimeMillis();
        // Make goal be 0.
        // neighbors are 0+1
        // water is infinite.
        
//        final TileArray array = new TileArray(ants.getRows(), ants.getCols());
        
        // Make a queue with shortest paths to each tile.
        Queue<TilePath> active = new PriorityQueue<TilePath>();
        active.add(new TilePath(origin));
        
        // Set of tiles with finalized paths
        Set<Tile> finished = new HashSet<Tile>();
        
        // Extend shortest paths until we reach all the goals
        Map<Tile, Set<TilePath>> results = new HashMap<Tile, Set<TilePath>>();
        while (!active.isEmpty()) {
            // Check time and return if necessary
            if ((System.currentTimeMillis() - start) > maxTime) {
                System.err.println("Time elapsed for path-finding!");
                return results;
            }
            
            TilePath path = active.remove();
            
            // We only want 1 path for each goal
            // TODO(ipince): maybe 2 is better.
            if (finished.contains(path.end())) {
                continue;
            }
            
            if (goals.contains(path.end())) {
                if (!results.containsKey(path.end())) {
                    results.put(path.end(), new HashSet<TilePath>());
                }
                results.get(path.end()).add(path);
            }
            
            if (results.size() == goals.size()) {
                // Found path for each goal!
                break;
            }
            
            // Extend path to neighbors, provided they're not water
            // TODO(ipince): go through aims randomly so there's variety.
            for (Aim aim : Aim.values()) {
                Tile neighbor = ants.getTile(path.end(), aim);
                if (!finished.contains(neighbor) &&
                        ants.getIlk(neighbor).isPassable()) {
                    active.add(path.extend(neighbor));
                }
            }
            
            finished.add(path.end());
        }
        
        if (goals.size() < results.size()) {
            System.err.println("Failed to find paths for some goals");
        }
        
        return results;
        
//        Set<Tile> unvisitedTiles = new TreeSet<Tile>(new Comparator<Tile>() {
//            @Override
//            public int compare(Tile o1, Tile o2) {
//                return (int) Math.round(array.getValue(o1) - array.getValue(o2));
//            }
//        });
        
//        // Set initial values (INFINITY everywhere except our goal).
//        for (int row = 0; row < ants.getRows(); row++) {
//            for (int col = 0; col < ants.getCols(); col++) {
//                Tile tile = new Tile(row, col);
//                array.setValue(tile, Double.MAX_VALUE);
//                active.add(tile);
//            }
//        }
//        array.setValue(goal, 0.0);
//        
//        active.add(goal);
//        
//        while (!active.isEmpty()) {
//            Tile currentTile = active.remove();
//            for (Aim aim : Aim.values()) {
//                Tile neighbor = ants.getTile(currentTile, aim);
//                if (active.contains(neighbor)) {
//                }
//            }
//            
////            visitedTiles.
//            break;
//        }
//        
//        Set<Tile> achievedOrigins = new HashSet<Tile>();
    }

}
