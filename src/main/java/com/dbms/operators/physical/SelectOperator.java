package com.dbms.operators.physical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.utils.ExpressionParseVisitor;
import com.dbms.utils.Tuple;
import java.io.PrintWriter;
import net.sf.jsqlparser.expression.Expression;

/** An operator that returns only those child Tuples that satisfy a specified expression. */
public class SelectOperator extends PhysicalOperator {
    /** {@code scanOperator} is the child {@code ScanOperator} of the {@code SelectOperator} */
    public PhysicalOperator scanOp;

    /** {@code visitor} helps convert the select conditions to programmatic types */
    private ExpressionParseVisitor epv = new ExpressionParseVisitor();

    /** select expression; Tuple is returned if this evaluates to true */
    private Expression exp;

    /** @param scanOperator child operator of SelectOperator
     * @param expression   the WHERE expression which we select for; is not null */
    public SelectOperator(PhysicalOperator scanOperator, Expression expression) {
        super(scanOperator.schema);
        scanOp = scanOperator;
        exp = expression;
    }

    /** resets underlying scan operator */
    @Override
    public void reset() {
        scanOp.reset();
    }

    /** @return the next tuple that passes the select expression */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            Tuple nextTuple = scanOp.getNextTuple();
            if (nextTuple == null) return null;
            epv.currentTuple = nextTuple;
            exp.accept(epv);
            if (epv.booleanResult) return nextTuple;
        }
    }

    @Override
    public void write(PrintWriter pw, int level) {
        String s = String.format("Select[%s]", exp.toString());
        pw.println(writeLevel(s, level));
        scanOp.write(pw, level + 1);
    }
}
