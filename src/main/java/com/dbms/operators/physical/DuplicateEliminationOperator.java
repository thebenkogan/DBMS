package com.dbms.operators.physical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.utils.Tuple;
import java.io.PrintWriter;

/** An operator that assumes its child returns Tuples in sorted order and filters out any
 * duplicates. */
public class DuplicateEliminationOperator extends PhysicalOperator {
    public PhysicalOperator child;

    /** previous Tuple returned */
    Tuple prev = null;

    /** @param child child operator; must be a sort operator */
    public DuplicateEliminationOperator(PhysicalOperator child) {
        super(child.schema);
        this.child = child;
    }

    /** @return next unique Tuple */
    @Override
    public Tuple getNextTuple() {
        Tuple next;
        while ((next = child.getNextTuple()) != null && next.equals(prev)) {}
        prev = next;
        return next;
    }

    /** resets the child operator and sets prev to null */
    @Override
    public void reset() {
        child.reset();
        prev = null;
    }

    @Override
    public void write(PrintWriter pw, int level) {
        pw.println(writeLevel("DupElim", level));
        child.write(pw, level + 1);
    }
}
