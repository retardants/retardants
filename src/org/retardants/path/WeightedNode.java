package org.retardants.path;

/**
 * A WeightedNode class is a simple record type which contains a name
 * and a cost 
 **/
public class WeightedNode implements Comparable<WeightedNode> {
    public final String name;
    public final int cost;

    /**
     * Constructs a new WeightedNode.
     *
     * @effects constructs a new WeightedNode with the name
     * <code>name</code> and the cost <code>cost</code>.
     *
     **/
    public WeightedNode(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
  
    /**
     * @return this.name
     **/
    public String name() {
        return name;
    }

    /**
     * @return this.cost
     **/
    public int cost() {
        return cost;
    }

    /**
     * WeightedNodes are ordered lexicographically by their name.
     * When two nodes share a name, their ordering is determined
     * by the numeric ordering of their costs.
     **/
    @Override
    public int compareTo(WeightedNode o) {
        int c = name.compareTo(o.name());
        if (c == 0) {
            return cost - o.cost;
        } else {
            return c;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof WeightedNode) {
            WeightedNode other = (WeightedNode) o;
            return this.name.equals(other.name) &&
                (this.cost == other.cost);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "[" + name.toString() + ": " + cost + "]";
    }
    
}
