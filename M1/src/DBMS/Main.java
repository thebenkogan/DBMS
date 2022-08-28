package DBMS;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

public class Main {
    public static void main(String args[]) throws JSQLParserException {
        Expression e= CCJSqlParserUtil.parseExpression("1");
        int a= 2 + 2;
        System.out.println(a);
        System.out.println("Hello World!");
        System.out.println(e);
    }
}
