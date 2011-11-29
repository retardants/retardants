package org.retardants.bot;

import org.retardants.adt.Tile;

/**
 * This class represents a command issued to a bot for a particular purpose
 * (such as exploration, food collecting, battle, etc).
 *
 * Associated with each command are the following attributes:
 * - Destination: The next tile to move to in order to perform this command
 * - Route cost: The number of steps needed to perform the command. For example,
 *               this could be the number of turns it will take the ant to reach
 *               a food source or an unexplored tile. This value may be an
 *               approximation.
 *
 * At the end of each turn, each ant is associated with a collection of these
 * commands, one of which is picked based on the relative weighting of the
 * command type and the cost of the command.
 *
 *
 *
 */
public class BotTask {

    /**
     * Describes the type of command represented by a BotTask object.
     * This command type is defined depending on the purpose why an ant is
     * performing a move, along with the priority we attach to this action.
     *
     */
    public enum CommandType {
        /* Design detail:

           We include a centralized location for specifying all command types
           because this serves as a central place to compare the priorities we
           attach to each of the different commands.

         */
        EXPLORATION_COMMAND(1),
        BATTLE_COMMAND(8),
        FOOD_COMMAND(10);

        private int priority;

        CommandType(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    };



    private CommandType type;
    /* The immediate destination after moving one step */
    private Tile destination;
    private int cost;

    public BotTask(CommandType type, Tile destination, int cost) {
        this.type = type;
        this.destination = destination;
        this.cost = cost;
    }


    public CommandType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public Tile getDestination() {
        return destination;
    }
    public String toString() {
        String cmd = "";
        cmd += "[";
        cmd += "Type: " + type;
        cmd += " cost: " + cost;
        cmd += "]";


        return cmd;

    }
}
