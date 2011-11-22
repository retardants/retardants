package org.retardants.path;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The PathFinder class simply has 1 method that is used to calculate the lowest
 * cost path between two sets of points.
 */
public class PathFinder<N, P extends Path<N,P>> {
	
	// No fields
	
	public PathFinder() {
	}
	
	public Set<P> pathPaths(Graph<N> graph, P origin, Set<N> goals) {
	    // Queue of paths to 
	    PriorityQueue<P> active = new PriorityQueue<P>();
	    active.add(origin);
	    
	    
	    return null;
	}
	
	/**
	 * Returns the shortest path between the ending node of any path 
	 * in <code>starts</code> to any node in <code>goals</code>.
	 * 
	 * @requires graph,starts,goals != null
	 * @return the Path with the lowest cost between a node in <code>starts</code>
	 * and a node in <code>goals</code>. If no path exists, throws an error
	 */
	public P findPath(Graph<N> graph, Set<P> starts, Set<N> goals) throws NoSuchElementException {
	      // The priority queue contains nodes with priority equal to the cost
	      // of the shortest path to reach that node.  Initially it contains 
	      // the start nodes.
	      PriorityQueue<P> active = new PriorityQueue<P>(starts);

	      // The set of finished nodes are those for which we know the shortest paths
	      // from starts and whose children we have already examined.
	      Set<N> finished = new HashSet<N>();

	      while (!active.isEmpty()) {
	        // queueMin is the element of active with shortest path
	        P queueMin = active.remove();

	        if (goals.contains(queueMin.end())) {
	            return queueMin;
	        }

	        // iterate over edges (queueMin, c) in queueMin.edges
	        for (N node: graph.listChildren(queueMin.end())) {
	        	P cpath = queueMin.extend(node);
	        	if (!finished.contains(node) && !active.contains(node)) {
	        		active.add(cpath); // should have priority cpath's cost
	        	}
	        }
	        
	        finished.add(queueMin.end());
	      }
	      // execution reaches this point only if active becomes empty
	      throw new NoSuchElementException("No path was found");
	}
}
