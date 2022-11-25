package com.dbms.operators.physical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import java.io.PrintWriter;

/** An operator that projects the Tuples from its child to a specified list of columns. */
public class ProjectOperator extends PhysicalOperator {

    /** {@code child} is the child operator for projection */
    public PhysicalOperator child;

    /** True if this operator should be visible in the printed query plan. */
    private boolean shouldWrite;

    /** @param child child operator to project
     * @param schema the schema to which this projects child tuples */
    public ProjectOperator(PhysicalOperator child, Schema s, boolean shouldWrite) {
        super(s);
        this.child = child;
        this.shouldWrite = shouldWrite;
    }

    /** resets child operator */
    @Override
    public void reset() {
        child.reset();
    }

    /** @return next projected Tuple */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = child.getNextTuple();
        if (nextTuple == null) return null;
        return nextTuple.project(schema);
    }

    @Override
    public void write(PrintWriter pw, int level) {
        if (shouldWrite) {
            String s = "Project" + schema.get().toString();
            pw.println(writeLevel(s, level));
            child.write(pw, level + 1);
        } else {
            child.write(pw, level);
        }
    }
}
