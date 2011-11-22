package org.retardants.diffusion;

import org.junit.Test;
import static org.junit.Assert.*;

import org.retardants.adt.Ants;
import org.retardants.adt.Tile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Various tests of the diffusion formula and its performance.
 */
public class DiffusionMapTest {
    @Test
    public void testOneSource() {

        /*
            Test the following transition

            0  0  0  0  0         0  0   0   0  0
            0  0  0  0  0         0  0   5   0  0
            0  0  10 0  0     =>  0  5  -10  5  0
            0  0  0  0  0         0  0   5   0  0
            0  0  0  0  0         0  0   0   0  0

         */
        int rows = 5;
        int cols = 5;
        Ants testAnts = new Ants(0, 0, rows, cols, 0, 0, 0, 0);
        Set<Tile> candies = new HashSet<Tile>(Arrays.asList(new Tile(2, 2)));
        DiffusionMap map = new DiffusionMap(cols, rows, testAnts, 10, 0.5);
        map.timeStep(1, candies);


        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++)
                System.out.printf("%10.2f", map.getValue(new Tile(i, j)));
            System.out.println("");
        }


        double delta = 0.01;
        assertEquals(map.getValue(new Tile(2,2)), -10, delta);
        for (int i = -1; i < 2; i += 2) {
            assertEquals(5, map.getValue(new Tile(2 + i, 2)),  delta);
            assertEquals(5, map.getValue(new Tile(2 , 2 + i)), delta);

        }

    }



    @Test
    public void performanceCheck() {
        int rows = 500;
        int cols = 300;
        int numIters = 100;
        Ants testAnts = new Ants(0, 0, rows, cols, 0, 0, 0, 0);
        Set<Tile> candies = new HashSet<Tile>();
        DiffusionMap map = new DiffusionMap(cols, rows, testAnts, 10, 0.5);
        Random r = new Random();

        /* Add 100 candies */
        for (int i = 0; i < 100; i++) {
            candies.add(new Tile(r.nextInt(rows - 1), r.nextInt(cols - 1)));
        }


        /* Perform iterations */
        double timeInitial = System.currentTimeMillis();
        map.timeStep(numIters, candies);
        double timeFinal = System.currentTimeMillis();

        double deltaTime = (timeFinal - timeInitial);
        assertTrue("Performance time of " + deltaTime, deltaTime < 500);

    }
}
