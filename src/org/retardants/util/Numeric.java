package org.retardants.util;

/**
 * For now, a wrapper to the logN function
 */
public class Numeric {

    /**
     * Computes the logarithm base n of value x
     * @param n The base
     * @param x Value whose logarithm we want to compute
     * @return \log_n(x)
     */
    public static double logn(double n, double x) {
        return java.lang.Math.log10(x) / java.lang.Math.log10(n);


    }

    /**
     * Round and return as the closest Integer value
     */
    public static int toInt(double n) {
        return (int) Math.round(n);
    }

}
