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
        BotCommand battleCmd = new BotCommand(
                BotCommand.CommandType.BATTLE_COMMAND,
                new Tile(0,0),
                3);
        /* Unknown territory two steps ahead */
        BotCommand exploreCmd = new BotCommand(
                BotCommand.CommandType.EXPLORATION_COMMAND,
                new Tile(1,0),
                2);
        /* Food three steps ahead */
        BotCommand foodCmd = new BotCommand(
                BotCommand.CommandType.FOOD_COMMAND,
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
        BotCommand cmd1 = taskMgr.pollCommand(ant);
        BotCommand cmd2 = taskMgr.pollCommand(ant);
        BotCommand cmd3 = taskMgr.pollCommand(ant);

        assertEquals(foodCmd, cmd1);
        assertEquals(battleCmd, cmd2);
        assertEquals(exploreCmd, cmd3);


    }

}
