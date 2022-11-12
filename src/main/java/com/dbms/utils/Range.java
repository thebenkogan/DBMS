package com.dbms.utils;

/** Wrapper object for ranges. */
public class Range {
    /** inclusive lower bound */
    public final int min;

    /** inclusive upper bound */
    public final int max;

    Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /** @return number of keys represented by these bounds */
    public int extent() {
        return max - min + 1;
    }
}
