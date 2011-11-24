package org.retardants.path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Graph models a set of nodes and the connections (edges) between them. Graph 
 * represents a directed graph, which means that edges have a direction. For
 * example, if there is an edge connecting node A to node B, that does not imply
 * that node B is connected to node A. 
 * 
 * Graphs are mutable objects; nodes of any type can be added to them, but only
 * one copy of each can be in the Graph, ie. a Graph has no two equal nodes.
 * 
 * Edges in a Graph must be between nodes contained in it. A particular node can
 * be connected by an edge to at most one copy of each node in the Graph, ie. no
 * two edges connect the same nodes together.
 * 
 * Note: Great care must be exercised if mutable objects are used as nodes. For
 * example, if two different mutable objects o1 and o2 are added to a Graph as
 * nodes, o2 could be mutated in such a way that o1 and o2 were equal, thus
 * violating the properties of Graph.
 * 
 * @specfield nodes : set // nodes in this graph
 * @specfield edges : set // (directed) edges in this graph, of form (A->B)
 *
 */
public class Graph<N> {
	
	private final Map<N,HashSet<N>> nodes;
	private final boolean check = false;

	// Abstraction Function:
	//   AF(c) = a graph G such that
	//			nodes = nodes.keySet()
	//    		edges = for each node n in nodes, for each of its children x in
	//					nodes.get(n), make a pair (n->x). The union of all these
	//					pairs are the edges of this
	//
	// Representation Invariant:
	//  * nodes != null && any node n != null
	//  * no two nodes are the same
	//  * no two children are the same
	//  * if n is a child of some node, then n is a node itself (in this)

	
	/**
	 * Constructs a new empty Graph.
	 * 
	 * @effects constructs a new Graph with no nodes.
	 */
	public Graph() {
		nodes = new HashMap<N,HashSet<N>>();
		checkRep();
	}
	
	/**
	 * Constructs a new Graph.
	 * 
	 * @requires node != null
	 * @effects constructs a new Graph with the node <code>node</code>.
	 */
	public Graph(N node){
		nodes = new HashMap<N,HashSet<N>>();
		nodes.put(node, new HashSet<N>());
		checkRep();
	}
	
	/**
	 * Ensures that <code>node</code> is a node in this Graph.
	 * 
	 * @requires node != null
	 * @modifies nodes
	 * @effects adds the node <code>node</code> to the set of nodes if it is not
	 * there already
	 */
	public void addNode(N node) {
		checkRep();
		if (node != null && !(nodes.containsKey(node))) {
			nodes.put(node, new HashSet<N>());
		}
		checkRep();
	}
	
	/**
	 * Ensures a directed edge exists between the nodes <code>parent</code>
	 * and <code>child</code>.
	 * 
	 * @requires <code>parent</code> and <code>child</code> to be nodes in this
	 * @modified nodes
	 * @effects adds the node <code>child</code> to the set of nodes of the 
	 * node <code>parent</code> if it is not there already
	 */
	public void addEdge(N parent, N child) {
		checkRep();
		if (nodes.containsKey(parent) && nodes.containsKey(child)) {
			nodes.get(parent).add(child);
		}
		checkRep();
	}
	
	/**
	 * Returns the set of nodes in this.
	 * 
	 * Note: Be careful with mutable elements in this set, because changes 
	 * to them will affect the nodes in this Graph.
	 * 
	 * @return sequence of nodes in this
	 */
	public Set<N> listNodes() {
		checkRep();
		Set<N> temp = nodes.keySet();
		// Make defensive copy
		Set<N> out = new HashSet<N>();
		out.addAll(temp);
		checkRep();
		return out;
	}
	
	/**
	 * Returns the set of nodes which form an edge with <code>node</code>.
	 * 
	 * Note: Be careful with mutable elements in this set, because changes
	 * to them will affect the edges in this Graph.
	 * 
	 * @requires <code>node</code> to be a node in this.
	 * @return sequence of nodes to which <code>node</code> is connected to.
	 */
	public Set<N> listChildren(N node) {
		checkRep();
		Set<N> temp = nodes.get(node);
		// Make defensive copy
		Set<N> out = new HashSet<N>();
		out.addAll(temp);
		checkRep();
		return out;
	}
	
	/**
	 * Checks that the representation invariant holds
	 */
	private void checkRep() {
		if (check) {
			// Checks that nodes is not null
			if (nodes==null) {
				throw new RuntimeException("nodes cannot be null!");
			}
			// Checks that all nodes are not null
			for (N node:nodes.keySet())
				if (node==null) {
					throw new RuntimeException("Graph cannot have null nodes.");
				}
			// Checks that if n is a child of some node, then n is a node of this
			for (N node:nodes.keySet()) {
				for (N child:nodes.get(node))
					if (!nodes.containsKey(child)) {
						throw new RuntimeException("All children must be nodes");
					}
			}
			// Checks that no two nodes are the same
			for (N node: nodes.keySet()) {
				// Compare all nodes
				for (N n: nodes.keySet())
					if (node.equals(n) && node!=n) // notice that we dont want referential equality
						throw new RuntimeException("Two equal nodes were found in Graph!");
				// Compare all children in each nodes with themselves
				for (N child: nodes.get(node))
					for (N ch: nodes.get(node))
						if (child.equals(ch) && child != ch)
							throw new RuntimeException("Two equal nodes were found in Graph!");
			}
		}
	}
}