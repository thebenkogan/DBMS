package com.dbms.operators.physical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.utils.Attribute;
import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;

/** An operator specialized in sorted equi-join conditions */
public class SortMergeJoinOperator extends PhysicalOperator {

    /** Sorted table on the left of the equal condition. */
    public ExternalSortOperator left;

    /** Sorted table on the right of the equal condition. */
    public ExternalSortOperator right;

    /** {@code} leftTuple} is an iterator to keep track of tuples in the left (outer) operator */
    private Tuple leftTuple;

    /** {@code} rightTuple} is an iterator to keep track of tuples in the right (inner) operator */
    private Tuple rightTuple;

    /** {@code rightIndex} keeps track of the index (row number) of the sorted right table. */
    private int rightIndex;

    /** {@code lastEqual} as a marker for the last time {@code leftTuple} and {@code rightTuple}
     * satisfied the equality join condition. It follows the right SortOperator when we need to find
     * the next equality. It will be -1 after a merge has been completed. */
    private int lastEqual;

    /** Constructs a {@code SortMergeJoinOperator} using 2 {@code SortOperator} instances
     *
     * @param left  outer sorted iterator of tuples
     * @param right inner sorted iterator of tuples */
    public SortMergeJoinOperator(ExternalSortOperator left, ExternalSortOperator right) {
        super(Schema.join(left.schema, right.schema));
        this.left = left;
        this.right = right;
        leftTuple = left.getNextTuple();
        rightTuple = right.getNextTuple();
        rightIndex = 0;
        lastEqual = -1;
    }

    /** Performs the merge of 2 tuples when they satisfy the equality and returns it. It uses the
     * left sorted table as a basis for the cross product (i.e. left x right).
     *
     * @return merged Tuple that satisfies the equality condition */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            if (leftTuple == null || rightTuple == null && lastEqual == -1) return null;

            if (lastEqual == -1) {
                while (leftTuple != null && mergeCondition(leftTuple, rightTuple) < 0) {
                    // advance left until >= right
                    leftTuple = left.getNextTuple();
                }
                if (leftTuple == null) return null;
                while (rightTuple != null && mergeCondition(leftTuple, rightTuple) > 0) {
                    // advance right until >= left
                    rightTuple = right.getNextTuple();
                    rightIndex++;
                }
                if (rightTuple == null) return null;

                lastEqual = rightIndex;
            }

            if (rightTuple != null && mergeCondition(leftTuple, rightTuple) == 0) {
                Tuple result = Tuple.mergeTuples(leftTuple, rightTuple);
                rightTuple = right.getNextTuple();
                return result;
            } else {
                right.reset(lastEqual);
                leftTuple = left.getNextTuple();
                rightTuple = right.getNextTuple();
                lastEqual = -1;
            }
        }
    }

    /** Resets both of the SortOperator objects. Resets rightIndex to 0 and lastEqual to -1. */
    @Override
    public void reset() {
        left.reset();
        right.reset();
        leftTuple = left.getNextTuple();
        rightTuple = right.getNextTuple();
        rightIndex = 0;
        lastEqual = -1;
    }

    /** Acts as a compare function between 2 tuples. Uses the orderBys of underlying sort operator
     * for the equality condition ordering.
     *
     * @param leftTuple  the row referenced on the left of an EqualsTo expression (e.g. A.B = C.D,
     *                   leftTuple would be in the A table)
     * @param rightTuple the row referenced on the right of an EqualsTo expression (e.g. A.B = C.D,
     *                   rightTuple would be in the C table)
     * @return 1 if leftTuple has a greater value than rightTuple, -1 if leftTuple has a smaller
     *         value than rightTuple, 0 if they're equal */
    private int mergeCondition(Tuple leftTuple, Tuple rightTuple) {
        for (int i = 0; i < right.orderBys.size(); i++) {
            Column leftCol = (Column) left.orderBys.get(i).getExpression();
            Column rightCol = (Column) right.orderBys.get(i).getExpression();
            int leftVal = leftTuple.get(Attribute.fromColumn(leftCol));
            int rightVal = rightTuple.get(Attribute.fromColumn(rightCol));
            int comp = Integer.compare(leftVal, rightVal);
            if (comp != 0) return comp;
        }

        return 0; // We have no tiebreakers. The tuples are equal.
    }

    @Override
    public void write(PrintWriter pw, int level) {
        List<String> equals = new LinkedList<>();
        for (int i = 0; i < right.orderBys.size(); i++) {
            equals.add(left.orderBys.get(i).getExpression().toString() + " = "
                    + right.orderBys.get(i).getExpression().toString());
        }
        String s = String.format("SMJ[%s]", String.join("AND", equals));
        pw.println(writeLevel(s, level));
        left.write(pw, level + 1);
        right.write(pw, level + 1);
    }
}
