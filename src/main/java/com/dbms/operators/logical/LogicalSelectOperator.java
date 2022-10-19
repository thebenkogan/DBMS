package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical representation of the select operator, which contains the child operator and
 * expression that we use to construct the physical operator
 */
public class LogicalSelectOperator extends LogicalOperator {

    /**
     * {@code child} is the {@code LogicalOperator} for the child operation of the {@code LogicalSelectOperator}
     */
    public LogicalOperator child;

    /**
     * {@code exp} is the expression containing columns to select from
     */
    public Expression exp;

    /**
     * @param logicalScan child operator of SelectOperator
     * @param exp the WHERE expression which we select for; is not null
     */
    public LogicalSelectOperator(LogicalOperator logicalScan, Expression exp) {
        child = logicalScan;
        this.exp = exp;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        physicalPlan.visit(this);
    }
}
