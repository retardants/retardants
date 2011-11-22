package org.retardants.diffusion;

import org.retardants.adt.Tile;

/**
 * Represents a TileArray isntance with two √ersions: a current (uncommitted)
 * version and an old (committed) version.
 *
 * The uncommitted version can be modified, whereas the committed verson should
 * not be modified except by committing the uncommitted version.
 *
 * @see DiffusionMap which uses VersionedTileArray to compute a new TileArray
 *      representing the ith iteration as a function of the old TileArray
 *      version.
 */
public class VersionedTileArray {

    private TileArray[] arrays = new TileArray[2];
    private int committedVersion = 0;
    private int uncommittedVersion = 1;

    public VersionedTileArray (int rows, int cols) {
        arrays[0] = new TileArray(rows, cols);
        arrays[1] = new TileArray(rows, cols);
    }

    /*
     * Set the uncommitted TileArray to be the latest (current) TileArray.
     *
     * Note that commit does not initialize the uncommitted array, so you must
     * set every Tile to its correct value before committing again.
     */
    public void commit() {
        this.committedVersion = (committedVersion + 1)  % 2;
        this.uncommittedVersion = (uncommittedVersion + 1)  % 2;
    }

    /**
     * Sets d as the diffusion value into the uncommitted version of this array
     * at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be set as the diffusion value at t
     */
    public void setValue(Tile t, double d) {
        arrays[uncommittedVersion].setValue(t, d);
    }

    /**
     * Sets d as the diffusion value into the committed version of this array
     * at the position represented by t
     *
     * Notice that, in general, you will only want to set values into the
     * uncommitted version.
     *
     * @param t The position to be modified
     * @param d The value to be set as the diffusion value at t
     */
    public void setCommittedValue(Tile t, double d) {
        arrays[committedVersion].setValue(t, d);
    }


    /**
     * Adds d to the diffusion value into the uncommitted version of this array
     * at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be added to the diffusion value at t
     */
    public void addValue(Tile t, double d) {
        arrays[uncommittedVersion].addValue(t, d);
    }



    /**
     * Return the committed diffusion value at the position represented by t
     *
     * @param t The position for which we want the diffusion value.
     * @return The diffusion value
     */
    public double getCommittedValue(Tile t) {
        return arrays[committedVersion].getValue(t);
    }


}

