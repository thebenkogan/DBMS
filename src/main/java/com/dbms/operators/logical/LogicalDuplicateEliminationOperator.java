package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import java.io.IOException;

/**
 * The logical representation of the Duplicate Elimination Operator, which only contains the child
 * operator that we need to construct the physical operator
 */
public class LogicalDuplicateEliminationOperator extends LogicalOperator {

    public LogicalOperator child;

    /** @param child child operator; must be a sort operator */
    public LogicalDuplicateEliminationOperator(LogicalOperator child) {
        this.child = child;
    }

    /** @param physicalPlan visitor which converts logical to physical operator
     * @throws IOException
     */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) throws IOException {
        physicalPlan.visit(this);
    }
}
