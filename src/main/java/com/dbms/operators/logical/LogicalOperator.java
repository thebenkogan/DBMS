package com.dbms.operators.logical;

import com.dbms.queryplan.PhysicalPlanBuilder;
import java.io.IOException;

/** Abstract interface for logical operators */
public abstract class LogicalOperator {

    /** @param physicalPlan visitor which converts logical to physical operator
     */
    public abstract void accept(PhysicalPlanBuilder physicalPlan) throws IOException;
}
