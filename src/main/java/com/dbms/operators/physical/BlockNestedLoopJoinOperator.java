package com.dbms.operators.physical;

import com.dbms.utils.Tuple;
import com.dbms.visitors.ExpressionParseVisitor;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

/**
 * An operator that joins two tables together using the block nested loop join algorithm. It loads
 * B buffer pages from the outer relation, then performs a nested loop join between all inner tuples
 * and the loaded outer tuples, joining them if satisfying a condition.
 */
public class BlockNestedLoopJoinOperator extends PhysicalOperator {

    /** Table on the left of the condition */
    private PhysicalOperator left;

    /** Table on the right of the condition */
    private PhysicalOperator right;

    /** This buffer reads the outer relation one block at a time */
    private List<Tuple> buffer;

    /** The number of tuples which this buffer can store, according to the Catalog */
    private int maxTuples;

    /** True if buffer contains tuples to join */
    private boolean blockRemaining;

    /** visitor for evaluating tuples on join conditions */
    private ExpressionParseVisitor epv = new ExpressionParseVisitor();

    /**
     * Expression for the join. Note: "most real-world joins are equijoins, but your BNLJ algorithm
     * should support all join conditions as specified in the Project 1 description"
     */
    private Expression joinCondition;

    /** Current inner tuple */
    private Tuple innerTuple;

    /** Id of current outer tuple (in block) that we are comparing to current inner tuple */
    private int outerTupleId = 0;

    /**
     * @param left left child operator
     * @param right right child operator
     * @param exp   join condition, null if none
     * @param pages number of pages per block
     */
    public BlockNestedLoopJoinOperator(PhysicalOperator left, PhysicalOperator right, Expression exp, int pages) {
        this.left = left;
        this.right = right;
        joinCondition = exp;
        innerTuple = right.getNextTuple();
        Tuple repOuterTuple = left.getNextTuple();
        left.reset();
        maxTuples = pages * 4096 / (4 * repOuterTuple.size());
        buffer = new ArrayList<>(maxTuples);
        readBlockIntoBuffer();
    }

    /**
     * Fill the buffer with tuples, ending when the buffer is full. Sets blockRemaining to true if
     * tuples read into buffer, otherwise false.
     */
    private void readBlockIntoBuffer() {
        // reset buffer and associated variables
        buffer.clear();

        // add tuples until we run out or the buffer is full
        Tuple outerTuple;
        blockRemaining = false;
        while (buffer.size() < maxTuples && (outerTuple = left.getNextTuple()) != null) {
            blockRemaining = true;
            buffer.add(outerTuple);
        }
    }

    /** @return {@code Tuple} for the next tuple in the resulting relation returned by BNLJ */
    @Override
    public Tuple getNextTuple() {
        // nested loop: for each block in outer, for each tuple in inner, for each tuple in outer
        while (true) {
            if (!blockRemaining) return null;
            if (innerTuple != null) {
                if (outerTupleId < buffer.size()) {
                    // check if inner and outer tuples satisfy the join condition, and return
                    // combined tuple if they do
                    Tuple combinedTuple = Tuple.mergeTuples(buffer.get(outerTupleId), innerTuple);
                    outerTupleId++;

                    if (joinCondition == null) return combinedTuple;
                    epv.currentTuple = combinedTuple;
                    joinCondition.accept(epv);
                    if (epv.booleanResult) return combinedTuple;
                } else {
                    // we have run out of tuples in outer (bottom level of nested loop). increment
                    // inner tuple and reset to first outer tuple
                    outerTupleId = 0;
                    innerTuple = right.getNextTuple();
                }
            } else {
                // we have run out of inner tuples (second level of nested loop),
                // so increment outer block and reset inner
                right.reset();
                innerTuple = right.getNextTuple();
                readBlockIntoBuffer();
            }
        }
    }

    /** Resets {@code left} and {@code right} operators to the first tuple in the relation */
    @Override
    public void reset() {
        left.reset();
        right.reset();
        innerTuple = right.getNextTuple();
        outerTupleId = 0;
        readBlockIntoBuffer();
    }
}
