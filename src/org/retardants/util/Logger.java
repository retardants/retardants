package org.retardants.util;

import org.retardants.adt.Ants;

/**
 * Utility for logging the different ant activities along with its remaining
 * class.
 *
 * This class will ultimately allow us to log the actions into text files for
 * easy reading.
 */
public class Logger {
    public static void log(String message, Ants ants) {
        System.err.print("Remaining after " + message);
        System.err.println(": " + ants.getTimeRemaining());
    }
}
