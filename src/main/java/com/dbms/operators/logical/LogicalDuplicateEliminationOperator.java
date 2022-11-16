package com.dbms.operators.logical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.queryplan.PhysicalPlanBuilder;
import java.io.IOException;
import java.io.PrintWriter;

/** The logical representation of the Duplicate Elimination Operator, which only contains the child
 * operator that we need to construct the physical operator */
public class LogicalDuplicateEliminationOperator extends LogicalOperator {

    public LogicalOperator child;

    /** @param child child operator; must be a sort operator */
    public LogicalDuplicateEliminationOperator(LogicalOperator child) {
        this.child = child;
    }

    /** @param physicalPlan visitor which converts logical to physical operator
     * @throws IOException */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) throws IOException {
        physicalPlan.visit(this);
    }

    @Override
    public void write(PrintWriter pw, int level) {
        pw.println(writeLevel("DupElim", level));
        child.write(pw, level + 1);
    }
}
