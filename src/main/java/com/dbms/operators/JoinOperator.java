package com.dbms.operators;

import com.dbms.utils.Tuple;
import com.dbms.visitors.ExpressionParseVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * An operator that joins two children together using the Tuple nested loop join algorithm based on
 * a join condition. If the join condition is null, this is a cross product. This algorithm grabs
 * the next left Tuple, then joins it with all Tuples from the right child that satisfy the join
 * condition. We then reset the right and get the next left Tuple until there are no more left
 * Tuples.
 */
public class JoinOperator extends Operator {

    private Operator left;
    private Operator right;
    private Expression joinCondition;
    private ExpressionParseVisitor visitor = new ExpressionParseVisitor();
    private Tuple leftTuple;

    /**
     * @param left left child operator
     * @param right right child operator
     * @param exp join condition, null if none
     */
    public JoinOperator(Operator left, Operator right, Expression exp) {
        this.left = left;
        this.right = right;
        joinCondition = exp;
        leftTuple = left.getNextTuple();
    }

    /** resets the left and right child */
    @Override
    public void reset() {
        left.reset();
        right.reset();
    }

    /**
     * @return the next merged tuple that satisfies the join condition, null if none left
     */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            if (leftTuple == null) return null;
            Tuple rightTuple = right.getNextTuple();
            if (rightTuple == null) {
                leftTuple = left.getNextTuple();
                right.reset();
                continue;
            }

            Tuple combinedTuple = Tuple.mergeTuples(leftTuple, rightTuple);

            if (joinCondition == null) return combinedTuple;

            visitor.currentTuple = combinedTuple;
            joinCondition.accept(visitor);
            if (visitor.booleanResult) return combinedTuple;
        }
    }
}
