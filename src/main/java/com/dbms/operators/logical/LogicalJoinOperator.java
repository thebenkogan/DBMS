package com.dbms.operators.logical;

import com.dbms.visitors.PhysicalPlanBuilder;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

/**
 * The logical representation of the join operator, which contains the expression, left child
 * operator, and right child operator which we need to construct the physical operator
 */
public class LogicalJoinOperator extends LogicalOperator {

    public LogicalOperator left;
    public LogicalOperator right;
    public Expression exp;
    public String innerTableName;
    public List<LogicalOperator> children;

    /**
     * @param left       left child operator
     * @param right          right child operator
     * @param innerTableName aliased inner table name
     * @param exp            join condition, null if none
     */
    public LogicalJoinOperator(LogicalOperator left, LogicalOperator right, String innerTableName, Expression exp) {
        this.left = left;
        this.right = right;
        this.exp = exp;
        this.innerTableName = innerTableName;
    }

    /**
     * Creates a {@code LogicalJoinOperator}
     * @param children list of children after {@code FROM}
     * @param exp join condition, null if none
     * TODO add inner table name
     */
    public LogicalJoinOperator(List<LogicalOperator> children, Expression exp) {
        this.children = children;
        this.exp = exp;
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
