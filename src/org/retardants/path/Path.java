package org.retardants.path;

import java.util.Iterator;

/**
 * <p>
 * A Path models a sequence of nodes and the cost for travelling along such a
 * sequence.
 * </p>
 * 
 * <p>
 * Paths are immutable.
 * </p>
 * 
 * @specfield cost : double // cost of traversing this Path
 * @specfield elements : sequence // the nodes in this Path
 * 
 * 
 * <p> The cost of traversing a path must not decrease as the path is
 * extended with new nodes.  Additionally, if <tt>p1.cost() &lt;
 * p2.cost()</tt>, then for all nodes <tt>n</tt>, we must have that
 * <tt>p1.extend(n).cost() &lt; p2.extend(n).cost()</tt> </p>
 * 
 * <p>
 * The first generic argument (<tt>N</tt>) is the type of nodes in the path.
 * The second generic argument (<tt>P</tt>) should be the name of the
 * implementing class itself (see WeightedNodePath for an example). Why is this
 * second argument necessary? Imagine that this interface was defined as
 * <tt>public interface Path&lt;N&gt;</tt>. Then the <tt>extend</tt>
 * function would be defined as returning a <tt>Path&lt;N&gt;</tt>. But this
 * is not specific enough; for example, the extend method on WeightedNodePath
 * could return a NodeCountingPath, or vice versa! The second generic
 * argument lets us force the implementing class to define an extend method that
 * returns an element of the same type.
 * </p>
 */
public interface Path<N, P extends Path<N,P>> 
	extends Iterable<N>, Comparable<Path<?, ?>> {

    // Producers

    /**
     * Creates an extended path by adding a new node to its end.
     * @requires n != null &&
                 n is a valid node type for this particular path 
                 implementation
     * @return a new Path p such that
     *       p.elements = this.elements + [ n ]
     *    && p.cost >= this.cost
     **/
    P extend(N n);

    // Observers

    /** @return this.cost **/
    double cost();

    /** @return the end of the path **/
    N end();

    /**
     * @return an Iterator over the elements in the path in order from
     *         start to end.
     */
    public Iterator<N> iterator();

} // Path
