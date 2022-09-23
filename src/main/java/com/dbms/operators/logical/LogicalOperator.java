package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;

/** Abstract interface for logical operators */
public abstract class LogicalOperator {

    /** @param physicalPlan visitor which converts logical to physical operator */
    public abstract void accept(PhysicalPlanBuilder physicalPlan);
}
