package DBMS.operators;

import DBMS.utils.Tuple;
import DBMS.visitors.ExpressionParseVisitor;
import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends Operator {

    private Operator scanOperator;
    private Expression expression;
    private ExpressionParseVisitor visitor= new ExpressionParseVisitor();

    /** Requires expression is not null.
     *
     * @param scanOperator child operator of SelectOperator
     * @param expression   the WHERE expression which we select for */
    public SelectOperator(Operator scanOperator, Expression expression) {
        this.scanOperator= scanOperator;
        this.expression= expression;
    }

    @Override
    public void reset() {
        scanOperator.reset();
    }

    /** @return the next tuple which we select for the given expression */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple= scanOperator.getNextTuple();
        if (nextTuple == null) return null;
        visitor.currentTuple= nextTuple;
        expression.accept(visitor);
        return visitor.getBooleanResult() ? nextTuple : getNextTuple();
    }

}
