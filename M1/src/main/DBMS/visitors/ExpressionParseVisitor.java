package DBMS.visitors;

import DBMS.utils.Tuple;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

//"For this project, you only need to worry about AndExpression,
// Column, LongValue, EqualsTo, NotEqualsTo, GreaterThan, GreaterThanEquals,
// MinorThan and MinorThanEquals."

public class ExpressionParseVisitor implements ExpressionVisitor {

    private long longResult;
    private boolean booleanResult;

    public Tuple currentTuple;

    public long getLongResult() {
        return longResult;
    }

    public boolean getBooleanResult() {
        return booleanResult;
    }

    private boolean evaluateBoolean(Expression exp) {
        exp.accept(this);
        return booleanResult;
    }

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
        longResult= currentTuple.get(col.getColumnName());
    }

    // Remaining methods are unimplemented. Generated automatically to extend ExpressionVisitor

    @Override
    public void visit(NullValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Function arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(InverseExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(JdbcParameter arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(DoubleValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(DateValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(TimeValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(TimestampValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Parenthesis arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(StringValue arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Addition arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Division arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Multiplication arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Subtraction arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(OrExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Between arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(InExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(IsNullExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(LikeExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(SubSelect arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(CaseExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(WhenClause arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ExistsExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AllComparisonExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AnyComparisonExpression arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Concat arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Matches arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(BitwiseAnd arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(BitwiseOr arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(BitwiseXor arg0) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

}
