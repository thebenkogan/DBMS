package com.dbms.operators.physical;

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
public class TupleNestedLoopJoinOperator extends PhysicalOperator {

    /** {@code left} is the left element of a join condition */
    private PhysicalOperator left;

    /** {@code right} is the right element of a join condition */
    private PhysicalOperator right;

    /** {@code joinCondition} is the join condition as an {@code Expression} type */
    private Expression joinCondition;

    /** {@code visitor} helps convert join conditions into {@code Expression} type */
    private ExpressionParseVisitor visitor = new ExpressionParseVisitor();

    /** {@code leftTuple} is the left tuple in the join relation */
    private Tuple leftTuple;

    /**
     * @param left left child operator
     * @param right right child operator
     * @param exp   join condition, null if none
     */
    public TupleNestedLoopJoinOperator(PhysicalOperator left, PhysicalOperator right, Expression exp) {
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
        leftTuple = left.getNextTuple();
    }

    /** @return the next merged tuple that satisfies the join condition, null if none left */
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
