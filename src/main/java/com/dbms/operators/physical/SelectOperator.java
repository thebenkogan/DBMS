package com.dbms.operators.physical;

import com.dbms.utils.Tuple;
import com.dbms.visitors.ExpressionParseVisitor;
import net.sf.jsqlparser.expression.Expression;

/** An operator that returns only those child Tuples that satisfy a specified expression. */
public class SelectOperator extends PhysicalOperator {
    /** {@code scanOperator} is the child {@code ScanOperator} of the {@code SelectOperator} */
    private PhysicalOperator scanOperator;

    /** {@code visitor} helps convert the select conditions to programmatic types */
    private ExpressionParseVisitor epv = new ExpressionParseVisitor();

    /** select expression; Tuple is returned if this evaluates to true */
    private Expression exp;

    /** @param scanOperator child operator of SelectOperator
     * @param expression   the WHERE expression which we select for; is not null */
    public SelectOperator(PhysicalOperator scanOperator, Expression expression) {
        super(scanOperator.schema);
        this.scanOperator = scanOperator;
        exp = expression;
    }

    /** resets underlying scan operator */
    @Override
    public void reset() {
        scanOperator.reset();
    }

    /** @return the next tuple that passes the select expression */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            Tuple nextTuple = scanOperator.getNextTuple();
            if (nextTuple == null) return null;
            epv.currentTuple = nextTuple;
            exp.accept(epv);
            if (epv.booleanResult) return nextTuple;
        }
    }
}
