package org.retardants.bot;

import org.retardants.bot.BotCommand;
import org.retardants.adt.Tile;

import java.util.*;

/**
 * Provides a mapping of ants to tasks allocated to them in this round. During
 * the round, all tasks that can be feasibly performed by every ant should be
 * inserted into the manager. At the end of the round, the TaskManager decides
 * which command each ant should perform.
 *
 * For example, consider that during a turn, the TaskManager receives these
 * commands:
 *
 * 1) For FOOD, move south to pick up a candy 10 steps away.
 * 2) For BATTLE, move north to capture an anthill 5 steps away.
 * 3) For EXPLORATION, move east to go to an unknown territory 2 steps away.
 *
 * The TaskManager may decide, for example, that although the unknown territory
 * is closest than any other target, attacking the anthill should get a higher
 * priority. Deciding between commands depends on at least the following factors:
 *
 * a) How far we're from achieving the target.
 * b) The relative importance of each command.
 *
 * Although in general it may be enough for a single command per type to ever
 * be set for each ant, it may be a good idea to set a few commands per type
 * in case the first command cannot be executed. For example, suppose there are
 * two candies two steps away. One is two steps north and another one is two
 * steps south. It may be a good idea to insert both commands, in case some
 * other ant needs to go over the first ant, thereby blocking the possibility
 * of the first ant to go north.
 *
 *
 */
public class TaskManager {
    /* TODO(jmunizn): It may be a good idea to make the taskmanager perform
        the tasks itself
     */

    /* Implementation:
        As an initial implementation, we include a weight for each task type.
        For now, we sort all tasks (ascending) using:
                c.type.priority / c.cost

     */
    private Comparator<BotCommand> comparator = new Comparator<BotCommand>() {
        @Override
        public int compare(BotCommand o1, BotCommand o2) {
            Double weight1 = (o1.getType().getPriority() + 0.0) / o1.getCost();
            Double weight2 = (o2.getType().getPriority() + 0.0) / o2.getCost();

            /* Note we use a negative sign (-) because we want to sort highest
                priority as SMALLER values in our priority queue
             */
            return -weight1.compareTo(weight2);
        }
    };

    private static int PRIORITY_QUEUE_INITIAL_CAPACITY = 10;

    private Map<Tile, PriorityQueue<BotCommand>> tasks =
            new HashMap<Tile, PriorityQueue<BotCommand>>();


    public void addTask(Tile ant, BotCommand command) {
        PriorityQueue<BotCommand> taskSet = tasks.get(ant);
        if (taskSet == null)
            taskSet = new PriorityQueue<BotCommand>(
                    PRIORITY_QUEUE_INITIAL_CAPACITY,
                    comparator);


        taskSet.add(command);
        tasks.put(ant, taskSet);


    }

    /**
     * Choose the best command for this ant based on all the commands issued
     * on this turn and poll (retrieve and remove it) out of the command queue
     * for this ant.
     *
     * @param ant The ant
     * @return The command with the highest weight that hasn't been popped
     *          this turn.
     */
    public BotCommand pollCommand(Tile ant) {
        PriorityQueue<BotCommand> taskSet = tasks.get(ant);
        if (taskSet == null)
            throw new IllegalStateException("Ant has no corresponding tasks");


        return taskSet.poll();
    }


    /**
     * Signal that a new turn has started, so all previous commands should
     * be forgotten.
     */
    public void newTurn() {
        tasks.clear();

    }

}
