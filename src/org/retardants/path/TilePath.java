package org.retardants.path;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.retardants.adt.Tile;

public class TilePath implements Path<Tile, TilePath> {
    
    private final Tile node;
    private final TilePath path;
    private final int cost;
    
    public TilePath(Tile node) {
        this(node, null);
    }
    
    private TilePath(Tile node, TilePath path) {
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
    
    @Override
    public TilePath extend(Tile n) {
        return new TilePath(n, this);
    }

    @Override
    public double cost() {
        return cost;
    }
    
    public Tile start() {
        if (path == null) {
            return node;
        } else {
            return path.start();
        }
    }

    @Override
    public Tile end() {
        return node;
    }
    
    @Override
    public int compareTo(Path<?, ?> path) {
        return Double.compare(this.cost(), path.cost());
    }

    @Override
    public Iterator<Tile> iterator() {
        // reverse the linked list, so that elements are returned in order
        // from start to end of the path.
        List<Tile> accumulator = new LinkedList<Tile>();
        for (TilePath cur = this; cur!=null; cur = cur.path) {
            accumulator.add(0, cur.end());
        }
        return accumulator.iterator();
    }
    
    public Iterator<Tile> reverseIterator() {
        List<Tile> accumulator = new LinkedList<Tile>();
        for (TilePath cur = this; cur!=null; cur = cur.path) {
            accumulator.add(cur.end());
        }
        return accumulator.iterator();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : this) {
            sb.append(tile + " -> ");
        }
        return sb.toString();
    }

}
