package DBMS.visitors;

import DBMS.utils.Tuple;
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

//"For this project, you only need to worry about AndExpression,
// Column, LongValue, EqualsTo, NotEqualsTo, GreaterThan, GreaterThanEquals,
// MinorThan and MinorThanEquals."

public class ExpressionParseVisitor extends ExpressionVisitorBase {

    private long longResult;
    private boolean booleanResult;

    public Tuple currentTuple;

    public long getLongResult() {
        return longResult;
    }

    public boolean getBooleanResult() {
        return booleanResult;
    }

    /** @param exp The expression which the visitor evaluates
     * @return the boolean result of evaluating exp */
    private boolean evaluateBoolean(Expression exp) {
        exp.accept(this);
        return booleanResult;
    }

    /** @param exp The expression which the visitor evaluates
     * @return the long result of evaluating exp */
    private long evaluateLong(Expression exp) {
        exp.accept(this);
        return longResult;
    }

    @Override
    public void visit(AndExpression exp) {
        boolean leftBool= evaluateBoolean(exp.getLeftExpression());
        boolean rightBool= evaluateBoolean(exp.getRightExpression());
        booleanResult= leftBool && rightBool;
    }

    @Override
    public void visit(EqualsTo exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong == rightLong;
    }

    @Override
    public void visit(GreaterThan exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong > rightLong;
    }

    @Override
    public void visit(GreaterThanEquals exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong >= rightLong;
    }

    @Override
    public void visit(MinorThan exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong < rightLong;
    }

    @Override
    public void visit(MinorThanEquals exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong <= rightLong;
    }

    @Override
    public void visit(NotEqualsTo exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong != rightLong;
    }

    @Override
    public void visit(LongValue longValue) {
        longResult= longValue.toLong();
    }

    @Override
    public void visit(Column col) {
        String tableName= col.getTable().getAlias();
        if (tableName == null) tableName= col.getTable().getName();
        longResult= currentTuple.get(tableName, col.getColumnName());
    }

}
