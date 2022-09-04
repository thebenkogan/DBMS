package DBMS.visitors;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import DBMS.utils.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

class ExpressionParseVisitorTest {

    @Test
    void test() {
        assertEquals(4, 2 + 2);
    }

    @ParameterizedTest
    @ValueSource(ints= { 1, 2, 3, 4, 5 })
    void isPositive(int number) {
        assertTrue(number > 0);
    }

    Expression testToExpression(String test) {
        try {
            InputStream is= new ByteArrayInputStream(
                ("select * from t where " + test).getBytes(StandardCharsets.UTF_8));
            CCJSqlParser parser= new CCJSqlParser(is);
            return ((PlainSelect) ((Select) parser.Statement()).getSelectBody()).getWhere();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    void testExpressionVisitor() throws ParseException, IOException {
        HashMap<String, Boolean> queries= new HashMap<String, Boolean>();
        queries.put("4<5", true);
        queries.put("5<4", false);
        queries.put("4<=4", true);
        queries.put("4<=3", false);
        queries.put("4<=5", true);
        queries.put("5=5", true);
        queries.put("5=6", false);
        queries.put("5!=6", true);
        queries.put("5!=5", false);
        queries.put("5>=4", true);
        queries.put("5>=5", true);
        queries.put("5>=6", false);
        queries.put("5>4", true);
        queries.put("5>5", false);
        queries.put("5>6", false);
        queries.put("5<3 AND 12=1", false);
        queries.put("3>4 AND 42=42", false);
        queries.put("4>3 AND 4<3", false);
        queries.put("4=4 AND 2=2", true);
        queries.put("5>3 AND 12=12 AND 3>2 AND 4<1", false);
        queries.put("5>3 AND 12=12 AND 3>2 AND 4>1", true);
        queries.put("t.A=3 AND t.B=10", true);
        queries.put("t.A=t.B", false);
        ExpressionParseVisitor epv= new ExpressionParseVisitor();
        epv.currentTuple= new Tuple(Arrays.asList(new String[] { "A", "B" }),
            Arrays.asList(new Integer[] { 3, 10 }));

        queries.forEach((test, expected) -> {
            Expression exp= testToExpression(test);
            exp.accept(epv);
            assertTrue(test, expected == epv.getBooleanResult());
        });
    }

}