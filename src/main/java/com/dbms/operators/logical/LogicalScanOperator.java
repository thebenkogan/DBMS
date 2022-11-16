package com.dbms.operators.logical;

import com.dbms.queryplan.PhysicalPlanBuilder;

/** The logical representation of the scan operator, which contains only the table name which we
 * need to construct the physical operator */
public class LogicalScanOperator extends LogicalOperator {

    /** {@code tableName} is the (aliased) name of the table to scan from */
    public String tableName;

    /** @param tableName name (aliased) of underlying table */
    public LogicalScanOperator(String tableName) {
        this.tableName = tableName;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        physicalPlan.visit(this);
    }
}
