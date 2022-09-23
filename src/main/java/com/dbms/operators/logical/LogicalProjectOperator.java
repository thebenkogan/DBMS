package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import java.util.List;
import net.sf.jsqlparser.statement.select.SelectItem;

/** The logical representation of the project operator, which contains the child operator and list
 * of select items which we need to construct the physical operator */
public class LogicalProjectOperator extends LogicalOperator {

    public LogicalOperator child;

    public List<SelectItem> selectItems;

    /** @param child   child operator to project
     * @param selectItems columns to project; does not contain AllColumns */
    public LogicalProjectOperator(LogicalOperator child, List<SelectItem> selectItems) {
        this.child = child;
        this.selectItems = selectItems;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        physicalPlan.visit(this);
    }
}
