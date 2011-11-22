package org.retardants.diffusion;

import org.retardants.adt.Tile;

/**
 * This class represents a wrapper for a bi-dimensional array of doubles to be
 * accessed via Tile objects rather than directly through its coordinates.
 *
 */
public class TileArray {
    private double[][] map;

    public TileArray(int rows, int cols) {
        map = new double[rows][cols];
    }

    /**
     * Adds d to the diffusion value at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be added to the diffusion value at t
     */
    public void addValue(Tile t, double d) {
        map[t.getRow()][t.getCol()] += d;
    }


    /**
     * Return the diffusion value at the position represented by t
     *
     * @param t The position for which we want the diffusion value.
     * @return The diffusion value
     */
    public double getValue(Tile t) {
        return map[t.getRow()][t.getCol()];
    }


    /**
     * Sets d as the diffusion value at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be set as the diffusion value at t
     */
    public void setValue(Tile t, double d) {
        map[t.getRow()][t.getCol()] = d;
    }


}

