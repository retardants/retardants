package org.retardants.diffusion;

import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Ilk;
import org.retardants.adt.Tile;
import java.util.*;

/**
 * Represents a 2D array of diffusion values storing the state of all scents
 * at some point in time.
 *
 * ADT = map(i,j) - A map of i rows and j columns such that map(i,j) is the
 *                  diffusion value of cell (i,j)
 *
 * @URL www.cs.colorado.edu/~ralex/papers/PDF/OOPSLA06antiobjects.pdf
 */
public class DiffusionMap {


    /* Static default values */
    public final static double DEFAULT_FOOD_COST = 100.00;
    public final static double  DEFAULT_DIFF_VALUE = 0.25;

    private VersionedTileArray array;
    private Ants antContext;
    private double diffValue;
    private double foodCost;



    /* TODO(jmunizn): Change order as rows, cols */
    /**
     * Construct a DiffusionMap by specifying all its parameters, including
     * the amount of scent emitted by its sources, along with the diffusion
     * coefficient.
     *
     * @param cols The number of columns in the grid
     * @param rows The number of rows in the grid
     * @param antContext The ants object corresponding to this bot. This object
     *                   is used to get information about the Ilks corresponding
     *                   to each Tile.
     */
    protected DiffusionMap(
        int cols,
        int rows,
        Ants antContext,
        double diffValue,
        double foodCost) {

        this.array = new VersionedTileArray(rows, cols);
        this.antContext = antContext;
        this.diffValue =  diffValue;
        this.foodCost = foodCost;
    }

    public DiffusionMap(int cols, int rows, Ants antContext) {
        this(cols, rows, antContext, DEFAULT_DIFF_VALUE, DEFAULT_FOOD_COST);

    }

    /**
     * Return the diffusion value at the position represented by t
     *
     * @param t The position for which we want the diffusion value.
     * @return The diffusion value
     */
    public Double getValue(Tile t) {
        return array.getCommittedValue(t);
    }

    /*
     * To each cell that now has a candy, add a scent of +FOOD_COST.
     */
    private void placeCandies(Set<Tile> candies) {
        for (Tile candy : candies) {
            array.setCommittedValue(candy, foodCost);
        }
    }

    /*
     * Perform diffusion of a single cell as:
     *
     * map(i,j) = map(i,j) + D * sum(map[(i',j') - map(i,j))
     *
     * @param i row number
     * @param j col number
     */
    private void diffuse(Tile t) {
        if (! antContext.getIlk(t).isUnoccupied()) {
            /* TODO(jmunizn) Set to zero if unoccupied but not food or enemies */
            if (! antContext.getIlk(t).equals(Ilk.FOOD)
                  && ! antContext.getIlk(t).equals(Ilk.ENEMY_ANT))
                array.setValue(t, 0.0);
        }
        else {
            array.setValue(t, array.getCommittedValue(t));


            double delta = 0.0;
            /* Set delta as:
             * DIFF_VALUE *
             *  \sum(array.committedValue(neighbor) - array.committedValue(t))
             *
             */
            for (Aim aim : Aim.values()) {
                Tile neighbor = antContext.getTile(t, aim);
                delta += array.getCommittedValue(neighbor);
            }

            delta -= (Aim.values().length * array.getCommittedValue(t));
            delta *= diffValue;
            array.addValue(t, delta);

        }

        /* Place a value of zero if there's a non-food object blocking us */


    }
    /**
     * Perform a single diffusion time step
     */
    private void timeStep() {
        for (int i = 0; i < antContext.getRows(); i++) {
            for (int j = 0; j < antContext.getCols(); j++) {
                diffuse(new Tile(i,j));
            }
        }
        array.commit();
    }

    /**
     * Perform n time steps
     * @param n The number of time steps to perform
     * @param candies The set of candies visible to our ants during this turn.
     *                This set may vary across time steps for these reasons:
     *                - A candy was consumed by us or the opponent.
     *                - A candy has left our visible range
     */
    public void timeStep(int n, Set<Tile> candies, long maxTime) {
         /* Note: Even though we have 'ants' from which we could get the
                  candy information, we enforce this contract and promise
                  not to use antContext to get information about candies or
                  enemy hills.
          */

        System.err.println("Diffusion map has maxTime of " + maxTime);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            if ((System.currentTimeMillis() - start) > maxTime) {
                System.err.println("Time elapsed for diffusion map! Exiting after " + i + " iterations.");
                break;
            }
            this.placeCandies(candies);
            timeStep();
        }
    }

}
