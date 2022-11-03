package com.dbms.utils;

import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/** A static class that provides helpful functions for converting string SQL segments to JsqlParser
 * segments, and other useful functions for expressions. */
public class Helpers {

    /** @param query The string representation of a query
     * @return the query (PlainSelect) which the string represents */
    private static PlainSelect convertQuery(String query) {
        try {
            return (PlainSelect) ((Select) CCJSqlParserUtil.parse(query)).getSelectBody();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** @param exp The string representation of an expression
     * @return the Expression which the string represents */
    public static Expression strExpToExp(String exp) {
        return convertQuery("select * from t where " + exp).getWhere();
    }

    /** @param orderBys A list of string representations of OrderByElements
     * @return the list of OrderByElements corresponding to the input strings */
    public static List<OrderByElement> strOrderBysToOrderBys(String... orderBys) {
        return convertQuery("select * from t order by " + String.join(", ", orderBys))
                .getOrderByElements();
    }

    /** @param selectItems A list of string representations of SelectItems
     * @return the list of SelectItems corresponding to the input strings */
    public static List<SelectItem> strSelectItemsToSelectItems(String... selectItems) {
        return convertQuery("select " + String.join(", ", selectItems) + " from t")
                .getSelectItems();
    }

    /** @param exp The expression to be added to an AndExpression
     * @return the AndExpression whose rightExpression is exp and leftExpression is empty */
    public static AndExpression wrapExpressionWithAnd(Expression exp) {
        if (exp instanceof AndExpression) return (AndExpression) exp;
        AndExpression andExp = new AndExpression(null, exp);
        return andExp;
    }

    /** @param expList
     * @return an AND Expression comprised of joined expList */
    public static Expression wrapListOfExpressions(List<Expression> expList) {
        assert !expList.isEmpty();
        if (expList.size() == 1) {
            return expList.get(0);
        }
        AndExpression and = new AndExpression(expList.get(0), expList.get(1));
        for (int i = 2; i < expList.size(); i++) {
            and = new AndExpression(and, expList.get(i));
        }
        return and;
    }

    /** Wrapper for grabbing the proper name of a table in String form.
     *
     * @param table Table to get the name of.
     * @return alias name as a String if there is one, original name if no alias. */
    public static String getProperTableName(Table table) {
        return table.getAlias() != null ? table.getAlias().getName() : table.getName();
    }

    public static List<ColumnName> getColumnNamesFromSelectItems(List<SelectItem> selectItems) {
        List<ColumnName> names = new LinkedList<>();
        for (SelectItem item : selectItems) {
            Column col = (Column) ((SelectExpressionItem) item).getExpression();
            String tableName = Helpers.getProperTableName(col.getTable());
            names.add(ColumnName.bundle(tableName, col.getColumnName()));
        }
        return names;
    }

    /** Retrieves all the EqualTo conditions from a given expression. Precondition: all
     * sub-expressions are EqualTo expressions.
     *
     * @param exp The Expression associated with the JoinOperator. Precondition: exp is an
     *            AndExpression
     * @return a list of EqualsTo expressions in the EquiJoin */
    public static List<EqualsTo> getEqualityConditions(Expression exp) {
        AndExpression andExpression = wrapExpressionWithAnd(exp);
        List<EqualsTo> result = new LinkedList<>();

        while (andExpression.getRightExpression() != null) {
            Expression nextAnd = andExpression.getLeftExpression();
            result.add((EqualsTo) andExpression.getRightExpression());
            if (nextAnd instanceof EqualsTo) {
                result.add((EqualsTo) andExpression.getLeftExpression());
                return result;
            }
            if (nextAnd == null) return result;
            andExpression = (AndExpression) nextAnd;
        }

        return result;
    }
}
