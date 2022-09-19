package com.dbms.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/** A static class that provides helpful functions for converting string SQL segments to JsqlParser
 * segments, and other useful functions for expressions. */
public class Helpers {

    /*** @param query The string representation of a query
     * @return the query (PlainSelect) which the string represents */
    private static PlainSelect convertQuery(String query) {
        try {
            InputStream is= new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
            CCJSqlParser parser= new CCJSqlParser(is);
            return (PlainSelect) ((Select) parser.Statement()).getSelectBody();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** @param exp The string representation of an expression
     * @return the Expression which the string represents */
    public static Expression strExpToExp(String exp) {
        return convertQuery("select * from t where " + exp).getWhere();
    }

    @SuppressWarnings("unchecked")
    /** @param orderBys A list of string representations of OrderByElements
     * @return the list of OrderByElements corresponding to the input strings */
    public static List<OrderByElement> strOrderBysToOrderBys(String... orderBys) {
        return convertQuery("select * from t order by " + String.join(", ", orderBys))
            .getOrderByElements();
    }

    @SuppressWarnings("unchecked")
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
        AndExpression andExp= new AndExpression(null, exp);
        return andExp;
    }
}
