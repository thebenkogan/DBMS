package com.dbms.visitors;

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.TryCastExpression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * An abstract class that only requires visit functions for the expressions in the scope of this
 * DBMS.
 */
public abstract class ExpressionVisitorBase implements ExpressionVisitor {

    @Override
    public abstract void visit(LongValue longValue);

    @Override
    public abstract void visit(AndExpression andExpression);

    @Override
    public abstract void visit(EqualsTo equalsTo);

    @Override
    public abstract void visit(GreaterThan greaterThan);

    @Override
    public abstract void visit(GreaterThanEquals greaterThanEquals);

    @Override
    public abstract void visit(MinorThan minorThan);

    @Override
    public abstract void visit(MinorThanEquals minorThanEquals);

    @Override
    public abstract void visit(NotEqualsTo notEqualsTo);

    @Override
    public abstract void visit(Column tableColumn);

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

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(CastExpression cast) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(Modulo modulo) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(TryCastExpression cast) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(UserVariable var) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(NumericBind bind) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(KeepExpression aexpr) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ValueListExpression valueList) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(OracleHint hint) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(NotExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(NextValExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(CollateExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ArrayExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(VariableAssignment aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(TimezoneExpression aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(JsonAggregateFunction aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(JsonFunction aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(ConnectByRootOperator aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(OracleNamedFunctionParameter aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AllColumns allColumns) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(AllValue allValue) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(HexValue hexValue) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(IntegerDivision division) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(XorExpression orExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        throw new UnsupportedOperationException("Unsupported Expression");
    }
}
