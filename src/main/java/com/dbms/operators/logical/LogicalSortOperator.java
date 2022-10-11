package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** The logical representation of the sort operator, which contains the child operator and list of
 * OrderByElements that we use to construct the physical operator */
public class LogicalSortOperator extends LogicalOperator {

    public LogicalOperator child;

    public List<OrderByElement> orderBys;

    /** Reads all Tuples from child into table, then sorts in the order specified by orderBys.
     *
     * @param child    child operator
     * @param orderBys list of orderBys, null if none */
    public LogicalSortOperator(LogicalOperator child, List<OrderByElement> orderBys) {
        this.child = child;
        this.orderBys = orderBys;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        try {
            physicalPlan.visit(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
