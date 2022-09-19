package com.dbms.visitors;

import static org.junit.Assert.assertTrue;

import com.dbms.utils.Helpers;
import com.dbms.utils.Tuple;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ParseException;
import org.junit.jupiter.api.Test;

/** Unit tests for the ExpressionParseVisitor */
class ExpressionParseVisitorTest {
    @Test
    void testExpressionVisitor() throws ParseException, IOException {
        /** maps string expressions to expected evaluation */
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
        epv.currentTuple= new Tuple(
            "t", Arrays.asList(new String[] { "A", "B" }), Arrays.asList(new Integer[] { 3, 10 }));

        queries.forEach(
            (test, expected) -> {
                Expression exp= Helpers.strExpToExp(test);
                exp.accept(epv);
                assertTrue(test, expected == epv.booleanResult);
            });
    }
}
