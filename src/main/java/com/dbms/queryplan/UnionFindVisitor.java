package com.dbms.queryplan;

import static com.dbms.utils.Helpers.wrapListOfExpressions;

import com.dbms.utils.Attribute;
import com.dbms.utils.Catalog;
import com.dbms.utils.ExpressionVisitorBase;
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

/** Visits a {@code WHERE} condition and determines the new stats of each attribute referenced */
public class UnionFindVisitor extends ExpressionVisitorBase {

    /** {@code UnionFind} object representing the condition */
    public UnionFind unionFind = new UnionFind();

    /** Maps an aliased table to a list of select expressions with it */
    public Map<String, List<Expression>> unusables = new HashMap<>();

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
    public void visit(AndExpression and) {
        and.getLeftExpression().accept(this);
        and.getRightExpression().accept(this);
    }

    /** @param tableName (aliased) table name */
    public Expression getExpression(String tableName) {
        String unaliased = Catalog.getRealTableName(tableName);
        Expression select = unionFind.expressionOfAttributes(Catalog.getAttributes(unaliased));
        List<Expression> unusable = unusables.get(namesToKey(tableName));
        if (select != null && unusable != null) unusable.add(select);
        if (unusable == null) return select;
        return wrapListOfExpressions(unusable);
    }

    /** @param name1 (aliased) first table name
     * @param name2 (aliased) second table name, must be different from name1
     * @return usable expression referencing the join of name1 and name2 */
    public Expression getExpression(String name1, String name2) {
        List<Expression> exps = unusables.get(namesToKey(name1, name2));
        if (exps == null) return null;
        return wrapListOfExpressions(exps);
    }

    /** Constructs a key from two table names to lookup their corresponding expressions.
     *
     * @param as 1 or 2 attributes to form the key from
     * @return expressions key based on attribute table names */
    private String namesToKey(String... names) {
        if (names.length == 1 || names[0].equals(names[1])) return names[0];
        List<String> tables = Arrays.asList(names[0], names[1]);
        Collections.sort(tables);
        return tables.toString();
    }

    /** Updates the unusables map to store this binary expression for the relevant table.
     *
     * @param exp unusable exp (could be column equality) */
    private void updateMap(BinaryExpression exp) {
        Expression left = exp.getLeftExpression();
        Expression right = exp.getRightExpression();
        String key;
        if (left instanceof Column && right instanceof Column) {
            leftAttribute = Attribute.fromColumn((Column) left);
            rightAttribute = Attribute.fromColumn((Column) right);
            key = namesToKey(leftAttribute.TABLE, rightAttribute.TABLE);
        } else if (right instanceof Column) {
            key = namesToKey(Attribute.fromColumn((Column) right).TABLE);
        } else {
            key = namesToKey(Attribute.fromColumn((Column) left).TABLE);
        }
        if (!unusables.containsKey(key)) unusables.put(key, new LinkedList<>());
        unusables.get(key).add(exp);
    }

    private void visit(Expression left, Expression right) {
        if (left instanceof Column) {
            // A OP 69
            visit((LongValue) right);
            leftAttribute = Attribute.fromColumn((Column) left);
        } else {
            // 69 OP A
            visit((LongValue) left);
            rightAttribute = Attribute.fromColumn((Column) right);
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
            if (isEqual) unionFind.union(leftAttribute, rightAttribute);
            updateMap(exp);
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
        updateMap(notEqualsTo);
    }

    @Override
    public void visit(Column tableColumn) {
        /* Never reached */
    }
}
