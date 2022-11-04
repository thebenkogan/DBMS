package com.dbms.visitors;

import com.dbms.index.Index;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

/** A visitor that takes the JsqlParser expressions (AndExpression, Column, LongValue, EqualsTo,
 * NotEqualsTo, GreaterThan, GreaterThanEquals, MinorThan, MinorThanEquals) and checks whether the
 * provided index can be used to evaluate them */
public class IndexExpressionVisitor extends ExpressionVisitorBase {

    /** The highest integer (inclusive) that our index scans up to, null if no bound found */
    public Integer high = null;

    /** The lowest integer (inclusive) that our index scans up to, null if no bound found */
    public Integer low = null;

    /** index to use */
    public Index index;

    /** Boolean which stores whether the current expression has a long */
    public boolean longSatisfied = false;

    /** Long which stores the long value of the current expression */
    /** Invariant: null if the expression has no long value. We only update value at the same time
     * as longSatisfied */
    public Long value = null;

    /** Boolean which stores whether the current expression has a column name for which an index
     * exists */
    public boolean columnSatisfied = false;

    /** List of all subexpressions which we cannot use the index for */
    public List<Expression> nonIndexedExps = new ArrayList<>();

    /** Here, booleanResult refers to whether the expression can use an index or not */
    public boolean booleanResult;

    /** @param exp The expression which the visitor evaluates
     * @return the boolean result of evaluating exp */
    private boolean evaluateBoolean(Expression exp) {
        exp.accept(this);
        return booleanResult;
    }

    /** @param exp The expression for which we check if we can use an index (assumed to be
     *            BinaryExpression)
     * @return whether or not we can use an index for the expression */
    private boolean indexInBinary(BinaryExpression exp) {
        longSatisfied = false;
        columnSatisfied = false;
        value = null;
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
        // in order for longSatisfied and columnSatisfied to be true,
        // exactly one of the left and right expressions must be a LongValue, and the other one must
        // be a Column. Furthermore, there must be an index which supports that Column.
        return longSatisfied && columnSatisfied;
    }

    /** @param num      number to use in update
     * @param isLowerBound true if this is an update on the lower bound, false for upper. Null if
     *                     this should update both.
     * @param isInclusive  true if the update number is an inclusive bound */
    private void updateHighAndLow(Long num, Boolean isLowerBound, boolean isInclusive) {
        int numInt = num.intValue();
        int val = isInclusive || isLowerBound == null ? numInt : isLowerBound ? numInt + 1 : numInt - 1;
        if (isLowerBound == null || isLowerBound) {
            low = low != null ? Math.min(low, val) : val;
        }
        if (isLowerBound == null || !isLowerBound) {
            high = high != null ? Math.max(high, val) : val;
        }
    }

    /** evaluates A AND B */
    @Override
    public void visit(AndExpression exp) {
        boolean leftBool = evaluateBoolean(exp.getLeftExpression());
        boolean rightBool = evaluateBoolean(exp.getRightExpression());
        // AndExpression can use index if either of its subexpressions can use index
        booleanResult = leftBool || rightBool;
    }

    // the only expressions for which we can use indexes are of the form
    // R.A < 42, R.A <= 42, R.A > 42, R.A >= 42 and R.A = 42,
    // which correspond to MinorThan, MinorThanEquals, GreaterThan, GreaterThanEquals, and
    // EqualsTo

    /** evaluates A == B */
    @Override
    public void visit(EqualsTo exp) {
        booleanResult = indexInBinary(exp);
        if (booleanResult) {
            updateHighAndLow(value, null, true);
        } else {
            nonIndexedExps.add(exp);
        }
    }

    /** Evaluates A strictly greater than B */
    @Override
    public void visit(GreaterThan exp) {
        booleanResult = indexInBinary(exp);
        if (booleanResult) {
            boolean rightValue = exp.getRightExpression() instanceof LongValue;
            updateHighAndLow(value, rightValue, false);
        } else {
            nonIndexedExps.add(exp);
        }
    }

    /** Evaluates A greater than or equal to B */
    @Override
    public void visit(GreaterThanEquals exp) {
        booleanResult = indexInBinary(exp);
        if (booleanResult) {
            boolean rightValue = exp.getRightExpression() instanceof LongValue;
            updateHighAndLow(value, rightValue, true);
        } else {
            nonIndexedExps.add(exp);
        }
    }

    /** Evaluates A strictly less than B */
    @Override
    public void visit(MinorThan exp) {
        booleanResult = indexInBinary(exp);
        if (booleanResult) {
            boolean rightValue = exp.getRightExpression() instanceof LongValue;
            updateHighAndLow(value, !rightValue, false);
        } else {
            nonIndexedExps.add(exp);
        }
    }

    /** Evaluates A less than or equal to B */
    @Override
    public void visit(MinorThanEquals exp) {
        booleanResult = indexInBinary(exp);
        if (booleanResult) {
            boolean rightValue = exp.getRightExpression() instanceof LongValue;
            updateHighAndLow(value, !rightValue, true);
        } else {
            nonIndexedExps.add(exp);
        }
    }

    // the remaining Expression types cannot use an index

    /** Evaluates A not equal to B */
    @Override
    public void visit(NotEqualsTo exp) {
        booleanResult = false;
        nonIndexedExps.add(exp);
    }

    @Override
    public void visit(LongValue longValue) {
        longSatisfied = true;
        value = longValue.getValue();
        booleanResult = false;
    }

    @Override
    public void visit(Column col) {
        columnSatisfied = index.name.COLUMN.equals(col.getColumnName());
        booleanResult = false;
    }
}
