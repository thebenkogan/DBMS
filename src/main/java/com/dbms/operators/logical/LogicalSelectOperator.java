package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;

/** The logical representation of the select operator, which contains the child operator and
 * expression that we use to construct the physical operator */
public class LogicalSelectOperator extends LogicalOperator {

    public LogicalOperator child;

    public Expression exp;

    /** @param scanOperator child operator of SelectOperator
     * @param expression   the WHERE expression which we select for; is not null */
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
