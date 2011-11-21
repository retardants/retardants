package org.retardants.diffusion;

import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Tile;

import javax.swing.border.TitledBorder;
import java.util.*;

/**
 * Represents a 2D array of diffusion values representing the state of the
 * scents at some point in time.
 *
 * ADT = map(i,j) - A map of i rows and j columns such that map(i,j) is the diffusion
 *                  value of cell (i,j)
 *
 * @URL www.cs.colorado.edu/~ralex/papers/PDF/OOPSLA06antiobjects.pdf
 */
public class DiffusionMap {

    private Double[][] map;
    private Ants antContext;
    public final static Double FOOD_COST = 100.0;
    public final static Double DIFF_VALUE = 0.25;


    public DiffusionMap(int cols, int rows, Ants antContext) {
        this.map = new Double[rows][cols];
        this.antContext = antContext;

    }

    /**
     * Adds d to the diffusion value at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be added to the diffusion value at t
     */
    private void addValue(Tile t, Double d) {
        map[t.getRow()][t.getCol()] += d;
    }

    /**
     * Sets d as the diffusion value at the position represented by t
     *
     * @param t The position to be modified
     * @param d The value to be set as the diffusion value at t
     */
    private void setValue(Tile t, Double d) {
        map[t.getRow()][t.getCol()] = d;
    }


    /**
     * Return the diffusion value at the position represented by t
     *
     * @param t The position for which we want the diffusion value.
     * @return The diffusion value
     */
    private Double getValue(Tile t) {
        return map[t.getRow()][t.getCol()];
    }


    /*
     * To each cell that now has a candy, add a scent of +FOOD_COST.
     */
    private void placeCandies(Set<Tile> candies) {
        for (Tile candy : candies)
            addValue(candy, FOOD_COST);
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
            /* TODO(jmunizn) Set to zero if unoccupied but not food */
            //setValue(t, 0.0);
        }
        else {
            for (Aim aim : Aim.values()) {
                Tile neighbor = antContext.getTile(t, aim);
                addValue(t, DIFF_VALUE * (getValue(neighbor)) - getValue(t));
            }
        }

        /* Place a value of zero if there's a non-food object blocking us */


    }
    /**
     * Perform a single diffusion time step
     */
    private void timeStep() {

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                diffuse(new Tile(i,j));
            }
        }


    }

    /**
     * Perform n time steps
     * @param n The number of time steps to perform
     * @param candies The set of candies visible to our ants during this turn.
     *                This set may vary across time steps for  these reasons:
     *                - A candy was consumed by us or the opponent.
     *                - A candy has left our visible range
     */
    public void timeStep(int n, Set<Tile> candies) {
       this.placeCandies(candies);

       for (int i = 0; i < n; i++) {
           timeStep();
       }

    }

}
