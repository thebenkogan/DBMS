package com.dbms.visitors;

import com.dbms.utils.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

/** A visitor that evaluates the following types of JsqlParser expressions: AndExpression, Column,
 * LongValue, EqualsTo, NotEqualsTo, GreaterThan, GreaterThanEquals, MinorThan and
 * MinorThanEquals. */
public class ExpressionParseVisitor extends ExpressionVisitorBase {

    /** The most recent result of any numerical evaluation. */
    public long longResult;

    /** The most recent result of any boolean evaluation. */
    public boolean booleanResult;

    /** The current Tuple for which to evaluate the expression */
    public Tuple currentTuple;

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

    /** evaluates A AND B */
    @Override
    public void visit(AndExpression exp) {
        boolean leftBool= evaluateBoolean(exp.getLeftExpression());
        boolean rightBool= evaluateBoolean(exp.getRightExpression());
        booleanResult= leftBool && rightBool;
    }

    /** evaluates A == B */
    @Override
    public void visit(EqualsTo exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong == rightLong;
    }

    /** evaluates A > B */
    @Override
    public void visit(GreaterThan exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong > rightLong;
    }

    /** evaluates A >= B */
    @Override
    public void visit(GreaterThanEquals exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong >= rightLong;
    }

    /** evaluates A < B */
    @Override
    public void visit(MinorThan exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong < rightLong;
    }

    /** evaluates A <= B */
    @Override
    public void visit(MinorThanEquals exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong <= rightLong;
    }

    /** evaluates A != B */
    @Override
    public void visit(NotEqualsTo exp) {
        long leftLong= evaluateLong(exp.getLeftExpression());
        long rightLong= evaluateLong(exp.getRightExpression());
        booleanResult= leftLong != rightLong;
    }

    /** evaluates a long value */
    @Override
    public void visit(LongValue longValue) {
        longResult= longValue.getValue();
    }

    /** evaluates a column reference by looking up the corresponding column in the current Tuple */
    @Override
    public void visit(Column col) {
        String tableName= col.getTable().getAlias();
        if (tableName == null) tableName= col.getTable().getName();
        longResult= currentTuple.get(tableName, col.getColumnName());
    }

}
