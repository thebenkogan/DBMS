package com.dbms.operators.logical;

import com.dbms.queryplan.PhysicalPlanBuilder;
import java.io.IOException;
import java.io.PrintWriter;

/** Abstract interface for logical operators */
public abstract class LogicalOperator {

    /** @param physicalPlan visitor which converts logical to physical operator */
    public abstract void accept(PhysicalPlanBuilder physicalPlan) throws IOException;

    /** Writes this operator at the corresponding level and writes each child operator on the next
     * level.
     *
     * @param pw    the writer that is accumulating the printed plan
     * @param level 0-based level in the tree */
    public abstract void write(PrintWriter pw, int level);
}
