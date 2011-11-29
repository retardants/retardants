package org.retardants.bot;

import org.junit.Test;
import org.retardants.adt.Tile;

import static org.junit.Assert.assertEquals;

/**
 * Basic tests for the Task Manager.
 */
public class TaskManagerTest {

    @Test public void testOneAnt() {
        /* Ant hill three steps ahead */
        BotTask battleCmd = new BotTask(
                BotTask.CommandType.BATTLE_COMMAND,
                new Tile(0,0),
                3);
        /* Unknown territory two steps ahead */
        BotTask exploreCmd = new BotTask(
                BotTask.CommandType.EXPLORATION_COMMAND,
                new Tile(1,0),
                2);
        /* Food three steps ahead */
        BotTask foodCmd = new BotTask(
                BotTask.CommandType.FOOD_COMMAND,
                new Tile(0,0),
                3);

        Tile ant = new Tile(1,1);

        /* Add all tasks to the task manager */
        TaskManager taskMgr = new TaskManager();
        taskMgr.newTurn();
        taskMgr.addTask(ant, exploreCmd);
        taskMgr.addTask(ant, foodCmd);
        taskMgr.addTask(ant, battleCmd);


        /* Get all the tasks in order.
         * Expect order:
         * BATTLE, FOOD, EXPLORATION
         */
        BotTask cmd1 = taskMgr.pollCommand(ant);
        BotTask cmd2 = taskMgr.pollCommand(ant);
        BotTask cmd3 = taskMgr.pollCommand(ant);

        assertEquals(foodCmd, cmd1);
        assertEquals(battleCmd, cmd2);
        assertEquals(exploreCmd, cmd3);


    }

}
