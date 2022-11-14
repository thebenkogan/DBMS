package com.dbms.visitors;

import com.dbms.utils.Attribute;
import com.dbms.utils.Helpers;
import com.dbms.utils.UnionFind;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

/**
 * Visits a {@code WHERE} condition and determines the new stats of each attribute referenced
 */
public class UnionFindVisitor extends ExpressionVisitorBase {

    /** {@code UnionFind} object representing the condition */
    public UnionFind unionFind = new UnionFind();

    /** Maps an aliased table to a list of select expressions with it */
    public Map<String, List<Expression>> unusableSelects = new HashMap<>();

    /** Maps 2 aliased table names to a list of join expressions with them */
    public Map<String, List<Expression>> unusableJoins = new HashMap<>();

    /** Possible column reference on the left of a binary operator */
    private Attribute leftAttribute;

    /** Possible column refernece on the right of a binary operator */
    private Attribute rightAttribute;

    /** Any number that may be in the current condition */
    private Long number;

    @Override
    public void visit(LongValue longValue) {
        number = longValue.getValue();
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
    }

    /**
     * Given 2 column references, check if they are to 2 different tables or the same table.
     * @param exp given binary expression
     * @return {@code true} if there are 2 different tables (aka join), false otherwise
     */
    private boolean isJoin(BinaryExpression exp) {
        String leftName = Helpers.getProperTableName(((Column) exp.getLeftExpression()).getTable());
        String rightName = Helpers.getProperTableName(((Column) exp.getRightExpression()).getTable());
        return !leftName.equals(rightName);
    }

    /**
     * Constructs a key from two table names to lookup their corresponding expressions.
     * @param name1 first name
     * @param name2 second name
     * @return expressions key
     */
    private String attributesToKey(Attribute a1, Attribute a2) {
        List<String> tables = Arrays.asList(a1.TABLE, a2.TABLE);
        Collections.sort(tables);
        return tables.toString();
    }

    private void updateMap(Map<String, List<Expression>> unusable, String alias, Expression exp) {
        if (unusable.get(alias) != null) {
            unusable.get(alias).add(exp);
        } else {
            List<Expression> expressions = new LinkedList<>();
            expressions.add(exp);
            unusable.put(alias, expressions);
        }
    }

    private void visit(Expression left, Expression right) {
        if (left instanceof Column) {
            // A OP 69
            Column lc = (Column) left;
            visit((LongValue) right);
            leftAttribute = Attribute.fromColumn(lc);
        } else {
            // 69 OP A
            Column rc = (Column) right;
            visit((LongValue) left);
            rightAttribute = Attribute.fromColumn(rc);
        }
    }

    /** @param exp       binary expression with one of >, <, >=, <=, =
     * @param isInclusive   true if this is an inclusive inequality
     * @param isGreaterThan true if the binop is > or >=
     * @param isEqual       true if the binop is = */
    private void handleBinop(BinaryExpression exp, boolean isInclusive, boolean isGreaterThan, boolean isEqual) {
        Expression left = exp.getLeftExpression();
        Expression right = exp.getRightExpression();
        if (left instanceof Column && right instanceof Column) {
            leftAttribute = Attribute.fromColumn((Column) left);
            rightAttribute = Attribute.fromColumn((Column) right);
            boolean isJoin = isJoin(exp);
            if (isEqual) {
                unionFind.union(leftAttribute, rightAttribute, isJoin);
            } else {
                if (isJoin) {
                    updateMap(unusableJoins, attributesToKey(leftAttribute, rightAttribute), exp);
                } else {
                    updateMap(unusableSelects, leftAttribute.TABLE, exp);
                }
            }
            return;
        }
        visit(left, right);
        Attribute col = left instanceof Column ? leftAttribute : rightAttribute;
        boolean isLowerBoundUpdate =
                left instanceof Column && isGreaterThan || right instanceof Column && !isGreaterThan;
        int val = isInclusive || isEqual
                ? number.intValue()
                : isLowerBoundUpdate ? number.intValue() + 1 : number.intValue() - 1;
        if (isEqual) {
            unionFind.updateEquality(col, val);
        } else if (isLowerBoundUpdate) {
            unionFind.updateLower(col, val);
        } else {
            unionFind.updateUpper(col, val);
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        handleBinop(equalsTo, true, false, true);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        handleBinop(greaterThan, false, true, false);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        handleBinop(greaterThanEquals, true, true, false);
    }

    @Override
    public void visit(MinorThan minorThan) {
        handleBinop(minorThan, false, false, false);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        handleBinop(minorThanEquals, true, false, false);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        Expression left = notEqualsTo.getLeftExpression();
        Expression right = notEqualsTo.getRightExpression();
        if (left instanceof Column && right instanceof Column) {
            // A != B
            leftAttribute = Attribute.fromColumn((Column) left);
            rightAttribute = Attribute.fromColumn((Column) right);

            boolean isJoin = isJoin(notEqualsTo);

            if (isJoin) {
                updateMap(unusableJoins, attributesToKey(leftAttribute, rightAttribute), notEqualsTo);
            } else {
                updateMap(unusableSelects, leftAttribute.TABLE, notEqualsTo);
            }
        } else if (left instanceof Column) {
            // A != 69
            leftAttribute = Attribute.fromColumn((Column) left);
            updateMap(unusableSelects, leftAttribute.TABLE, notEqualsTo);
        } else {
            // 69 != A
            rightAttribute = Attribute.fromColumn((Column) right);
            updateMap(unusableSelects, rightAttribute.TABLE, notEqualsTo);
        }
    }

    @Override
    public void visit(Column tableColumn) {
        /* Never reached */
    }
}
