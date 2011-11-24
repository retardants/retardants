package org.retardants.path;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A NodeCountingPath characterizes a path of WeightedNodes.  The cost
 * for a path is the number of WeightedNodes it contains; the cost of the
 * WeightedNodes are ignored.<p>
 *
 * A NodeCountingPath is immutable. A new NodeCountingPath is returned through
 * the extend path operation. <p>
 *
 * <p>The main purpose of this class is to illustrate that there can be multiple
 * implementations of Paths of WeightedNodes.</p>
 *
 * Specfields inherited from Path:
 *
 * @specfield  cost :     double       // cost of traversing this path.
 * @specfield  elements : sequence     // sequence of nodes in this path.
 * 
 **/

public class NodeCountingPath implements Path<WeightedNode, NodeCountingPath> {

    // The representation invariant describes what invariants hold for
    // the concrete representation. 
    // 
    // RepInvariant:
    //  RI(c) = 
    //   (c.node != null) &&
    //   (c.path == null) ==> (c.cost == 1) &&
    //   (c.path != null) ==> (c.cost == 1 + c.path.cost)
    //

    //
    // Abstraction Function:
    //
    //  The abstract state is given in terms of the specfields of the
    //  Path interface, namely the cost and elements of a path. 
    //
    //  The AF uses two helper functions, which map a concrete state
    //  'c' to the abstract state.
    //
    //  AF(c) = < ncpcost(c), ncpelms(c) > 
    //  (Maps c to ncp cost and ncp elements abstract fields recursively)
    //
    //    ncpcost(c) = c.cost
    //    ncpelms(c) = / [c.node]                    if (c.path == null)
    //                  \ ncpelms(c.path) + [c.node] if (c.path != null)
    //
    //  (Note that [c.node] appears at the *end* not the *start* of the
    //   path sequence.)
    //  
    //  To make the AF(c) clearer, we could also write the following:
    //  AF(c) = < cost, elements >  where
    //   cost     = c.cost
    //   elements = [c.node]                     if c.path == null 
    //              [c.node] + c.path.elements   if c.path != null
    //  

    /** The WeightedNode at the end of the path. */
    private final WeightedNode node;
    /** A NodeCountingPath which, when extended with 'node' at the end,
     *  is equal to this.  May be null iff this path has only 1 element. */
    private final NodeCountingPath path;
    /** The cost of this NodeCountingPath (that is, its length). */
    private final int cost;
  

    /**
     * Constructs a NodeCountingPath containing one node.
     * 
     * @requires node != null
     * @effects Creates a new NodeCountingPath which originates at
     * <code>node</code>.
     **/
    public NodeCountingPath(WeightedNode node) {
        this(node, null);
    }

    /**
     * @requires node != null
     * @effects Creates a new NodeCountingPath 'res' such that
     * res.elements = path.elements + [ node ]
     **/
    private NodeCountingPath(WeightedNode node, NodeCountingPath path) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        this.node = node;
        this.path = path;
        if (path != null) {
            this.cost = 1 + path.cost;
        } else {
            this.cost = 1;
        }
    }



    // Specified by Path interface.
    public NodeCountingPath extend(WeightedNode node) {
        return new NodeCountingPath(node, this);
    }

    // Specified by Path interface.
    public double cost() {
        return cost;
    }

    // Specified by Path interface (which extends Iterable)
    public Iterator<WeightedNode> iterator() {
        // reverse the linked list, so that elements are returned in order
        // from start to end of the path.
        List<WeightedNode> accumulator = new LinkedList<WeightedNode>();
        for (NodeCountingPath cur = this; cur!=null; cur = cur.path) {
            accumulator.add(0, cur.end());
        }
        return accumulator.iterator();
    }

    /**
     * @return a string representation of this. 
     **/
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[NodeCountingPath: ");
        boolean first=true;
        for (WeightedNode wn : this) {
            if (first) first = false;
            else sb.append(", ");
            sb.append(wn);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * @return true iff o is a NodeCountingPath and o.elements is the
     * same sequence as this.elements
     **/
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeCountingPath))
            return false;
        return this.equals((NodeCountingPath) o);
    }

    /**
     * @return true iff wnp.elements is the same sequence as this.elements
     **/
    public boolean equals(NodeCountingPath wnp) {
        return (wnp != null) &&
            this.node.equals(wnp.node) && 
            (this.path == null ? wnp.path==null : this.path.equals(wnp.path));
    }

    /**
     * @return a valid hashcode for this. 
     **/
    public int hashCode() {
        return node.hashCode() + (this.path==null ? 0 : 13 * path.hashCode());
    }


    /**
     * Compares the cost of this path to the given path.
     * @return the value 0 if the cost of this path is equal to the
     *         cost of the given path; a value less than 0 if this.cost is
     *         less than p.cost; and a value greater than 0 if this.cost
     *         is greater than p.cost.
     * @see java.lang.Comparable#compareTo
     */
    public int compareTo(Path<?,?> p){
        return Double.compare(this.cost(), p.cost());
    }
    
    /**
     * Return the end of this path
     */
    public WeightedNode end(){
        return node;
    }
}    
