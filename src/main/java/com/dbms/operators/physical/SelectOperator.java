package com.dbms.operators.physical;

import com.dbms.utils.Tuple;
import com.dbms.visitors.ExpressionParseVisitor;
import net.sf.jsqlparser.expression.Expression;

/** An operator that returns only those child Tuples that satisfy a specified expression. */
public class SelectOperator extends PhysicalOperator {

    private PhysicalOperator scanOperator;
    private ExpressionParseVisitor visitor = new ExpressionParseVisitor();

    /** select expression; Tuple is returned if this evaluates to true */
    private Expression exp;

    /**
     * @param scanOperator child operator of SelectOperator
     * @param expression the WHERE expression which we select for; is not null
     */
    public SelectOperator(PhysicalOperator scanOperator, Expression expression) {
        this.scanOperator = scanOperator;
        this.exp = expression;
    }

    /** resets underlying scan operator */
    @Override
    public void reset() {
        scanOperator.reset();
    }

    /**
     * @return the next tuple that passes the select expression
     */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = scanOperator.getNextTuple();
        if (nextTuple == null) return null;
        visitor.currentTuple = nextTuple;
        exp.accept(visitor);
        return visitor.booleanResult ? nextTuple : getNextTuple();
    }
}
