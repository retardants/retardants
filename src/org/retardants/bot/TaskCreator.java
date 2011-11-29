package org.retardants.bot;

import org.retardants.adt.Ants;

/**
 * A TaskCreator object creates sensible tasks during the execution of a turn.
 * These tasks will normally represent all the feasible commands all our ants
 * may be able to execute in order to satisfy some command type.
 *
 * For example, in order to eat food, a TaskCreator may be subclassed to identify
 * the food sources and assign a Task to each ant so that they can obtain food.
 * These commands may or may not be executed by the TaskManager, depending on
 * the priorities of other TaskCreators.
 *
 */
public interface TaskCreator {

    /**
     * Executed once for each game, this method allows the TaskCreator to be
     * initialized.
     *
     * @param ants The global ants context
     */
    public void init(Ants ants);


    /**
     * Executed at the beginning of the turn, insert into task all the feasible
     * commands that all our ants may execute in this turn to achieve a
     * particular purpose.
     *
     * @param task The task manager
     * @param ants The global ants context
     */
    public void createTasks(TaskManager task, Ants ants);
}
