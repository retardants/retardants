package org.retardants.bot;

import org.retardants.adt.Aim;
import org.retardants.adt.Ants;
import org.retardants.adt.Tile;
import org.retardants.diffusion.DiffusionMap;

import java.util.Set;

/**
 * Proposes a set of commands for every ant to collect food based on the idea of
 * gradient descent over a diffusion map of scents, where each food source
 * generates a scent that is propagated along the map.
 *
 * @see org.retardants.diffusion.DiffusionMap
 */
public class FoodCollectionTaskCreator implements TaskCreator {
    private DiffusionMap diffusionMap;


    @Override
    public void init(Ants ants) {
        diffusionMap = new DiffusionMap(ants.getCols(), ants.getRows(), ants);

    }


    @Override
    public void createTasks(TaskManager task, Ants ants) {
        foodDiffusionAllAnts(ants, task);
    }



    // JOSE: THIS IS YOUR ORIGINAL IMPLEMENTATION (minor details changed)
    private void foodDiffusionAllAnts(Ants ants, TaskManager taskManager) {

        Set<Tile> sortedAnts = ants.getMyAnts();
        Set<Tile> candies = ants.getFoodTiles();

        // Use at most 50% of the turn time.
        diffusionMap.timeStep(100, candies, ants.getTimeRemaining() / 2);

        // For each ant, move in the direction of increasing diffusion value
        for (Tile antLoc : sortedAnts) {
            /* TODO(jmunizn) We may want to skip calculating a value for ants
               that already have high-priority orders registered in the
               taskManager. However, we have to make sure our food/exploration
               order doesn't turn out to be more important in the end.
             */
            // No need to calculate value for ants who already have orders.

            Double bestValue = Double.MIN_VALUE;
            Aim bestAim  = null;

            for (Aim aim : Aim.values()) {
                Tile neighbor = ants.getTile(antLoc, aim);
                Double neighborValue = diffusionMap.getValue(neighbor);
                if (neighborValue > bestValue) {
                    bestValue = neighborValue;
                    bestAim = aim;
                }


                if (bestAim != null) {
                    int numSteps =
                            (int) Math.round(Math.log(bestValue / 100.00) / Math.log(0.50));

                    taskManager.addTask(
                            antLoc,
                            new BotTask(
                                    (numSteps > 7 ? BotTask.CommandType.EXPLORATION_COMMAND :
                                            BotTask.CommandType.FOOD_COMMAND),
                                    ants.getTile(antLoc, bestAim),
                                    /* TODO: This needs to be moved to DiffusionMap */
                                    /* if k steps away from a source, then
                                        the diffusion value will be:
                                         (SOURCE) * (DIFF_COEFF)^k = value
                                       Therefore, estimate k as
                                          log_{DIFF_COEFF} (bestValue / SOURCE)
                                     */
                                    numSteps));

                }
            }
        }
    }

}
