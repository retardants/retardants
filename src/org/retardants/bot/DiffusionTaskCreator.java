package org.retardants.bot;

import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Tile;
import org.retardants.diffusion.DiffusionMap;
import org.retardants.util.Numeric;

import java.util.*;

/**
 * Proposes a set of commands for every ant to collect food based on the idea of
 * gradient descent over a diffusion map of scents, where each food source
 * generates a scent that is propagated along the map.
 *
 * @see org.retardants.diffusion.DiffusionMap
 */
public class DiffusionTaskCreator implements TaskCreator {
    private DiffusionMap diffusionMap;


    @Override
    public void init(Ants ants) {
        diffusionMap = new DiffusionMap(ants.getCols(), ants.getRows(), ants);

    }

    private BotTask createTask(Aim aim, Ants ants, Tile antLoc) {
        Double diffValue = diffusionMap.getValue(ants.getTile(antLoc, aim));

        /* TODO(jmunizn): This needs to be moved to DiffusionMap */
        /* if k steps away from a source, then the diffusion value will be:
            (SOURCE) * (DIFF_COEFF)^k = value
          Therefore, estimate k as
             log_{DIFF_COEFF} (bestValue / SOURCE)
        */
        int numSteps = Numeric.toInt(Numeric.logn(0.50, diffValue / 100.00));
        BotTask.CommandType commandType = null;
        if (numSteps > 7)
            commandType = BotTask.CommandType.EXPLORATION_COMMAND;
        else
            commandType = BotTask.CommandType.FOOD_COMMAND;

        return new BotTask(commandType, ants.getTile(antLoc, aim), numSteps);
    }


    /**
     * Creates the ant task for a single ant.
     *
     * @param task The task manager
     * @param ants The ant context
     * @param antLoc The location of the ant for which we want to generate
     *                commands
     */
    private void createAntTask(TaskManager task, final Ants ants, final Tile antLoc) {

        /* Sort aims by increasing diffusion value. We want to move our ant
         * in the direction of the Aim that takes us to the highest tile in
         * our diffusion map.
         *
         * TODO(jmunizn): Change to PriorityQueue
         */
        TreeSet<Aim> sortedAims = new TreeSet<Aim>(new Comparator<Aim>() {
            @Override
            public int compare(Aim o1, Aim o2) {
                Double o1Value = diffusionMap.getValue(ants.getTile(antLoc, o1));
                Double o2Value = diffusionMap.getValue(ants.getTile(antLoc, o2));
                if (o1Value == null || o2Value == null )
                    throw new RuntimeException("Null diffusion value");

                return -o1Value.compareTo(o2Value);
            }
        });

        sortedAims.addAll(Arrays.asList(Aim.values()));
        task.addTask(antLoc, createTask(sortedAims.first(), ants, antLoc));


    }

    @Override
    public void createTasks(TaskManager task, Ants ants) {
        Set<Tile> antLocs = ants.getMyAnts();
        Set<Tile> candies = ants.getFoodTiles();

        // Use at most 50% of the turn time.
        diffusionMap.timeStep(100, candies, ants.getTimeRemaining() / 2);

        // For each ant, move in the direction of increasing diffusion value
        for (Tile antLoc : antLocs) {
            /* TODO(jmunizn) We may want to skip calculating a value for ants
              that already have high-priority orders registered in the
              taskManager. However, we have to make sure our food/exploration
              order doesn't turn out to be more important in the end.
            */
            createAntTask(task, ants, antLoc);
        }
    }
}


