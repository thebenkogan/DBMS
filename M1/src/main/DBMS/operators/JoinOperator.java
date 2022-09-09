package DBMS.operators;

import DBMS.utils.Tuple;
import DBMS.visitors.ExpressionParseVisitor;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {

    private Operator left;
    private Operator right;
    private Expression joinCondition;
    private ExpressionParseVisitor visitor= new ExpressionParseVisitor();
    private Tuple leftTuple;

    public JoinOperator(Operator left, Operator right, Expression exp) {
        this.left= left;
        this.right= right;
        joinCondition= exp;
        leftTuple= left.getNextTuple();
    }

    @Override
    public void reset() {
        left.reset();
        right.reset();
    }

    /** @return the next tuple which we return for the given join condition */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            if (leftTuple == null) return null;
            Tuple rightTuple= right.getNextTuple();
            if (rightTuple == null) {
                leftTuple= left.getNextTuple();
                right.reset();
                continue;
            }

            Tuple combinedTuple= Tuple.mergeTuples(leftTuple, rightTuple);

            if (joinCondition == null) return combinedTuple;

            visitor.currentTuple= combinedTuple;
            joinCondition.accept(visitor);
            if (visitor.getBooleanResult()) return combinedTuple;
        }
    }

}
