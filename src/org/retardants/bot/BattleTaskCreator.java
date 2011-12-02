package org.retardants.bot;

import static org.retardants.bot.BotTask.CommandType.*;
import org.retardants.adt.Ants;
import org.retardants.adt.Tile;
import org.retardants.path.PathMap;
import org.retardants.path.TilePath;
import org.retardants.util.Numeric;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generates battle-related tasks by directing all the ants to attack the visible
 * anthills by assigning each ant to its closest anthill and directing them
 * one step closer to this anthill using Dijkstra's shortest path algorithm.
 */
public class BattleTaskCreator implements TaskCreator {
    @Override
    public void init(Ants ants) {}


    /**
     * Creates all tasks that involve any of our ants moving to a given anthill.
     *
     * @param task The task manager
     * @param ants The ant context
     * @param hillLoc The location of the hill for which we want to generate
     *                commands
     */
    private void createHillTasks(TaskManager task, Ants ants, Tile hillLoc) {
        Map<Tile, Set<TilePath>> paths = null;
        Set<Tile> allAnts = ants.getMyAnts();
        long timeToUse = ants.getTimeRemaining() / 2;
        // For each enemy hill, find the shortest tile-path to each ant.
        // Use at most 50% of the time.
        paths = PathMap.findBestPaths(ants, hillLoc, allAnts, timeToUse);

        // Send each ant to the hill using (one of) the shortest path found.
        for (Tile antLoc : ants.getMyAnts()) {
            if (paths.containsKey(antLoc)) {
                for (TilePath path : paths.get(antLoc)) {
                    Iterator<Tile> iter = path.reverseIterator();

                    assert iter.hasNext();
                    iter.next();
                    assert iter.hasNext();

                    Tile nextStep = iter.next();
                    int distanceToHill = Numeric.toInt(path.cost());

                    task.addTask(
                            antLoc,
                            new BotTask(BATTLE_COMMAND, nextStep, distanceToHill));

                }
            }
        }

    }


    @Override
    public void createTasks(TaskManager task, Ants ants) {

        for (Tile hillLoc : ants.getEnemyHills()) {
            createHillTasks(task, ants, hillLoc);
        }
    }

}
