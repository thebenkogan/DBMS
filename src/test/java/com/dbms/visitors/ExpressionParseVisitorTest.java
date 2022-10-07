package com.dbms.visitors;

import static org.junit.Assert.assertTrue;

import com.dbms.utils.Helpers;
import com.dbms.utils.Tuple;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the ExpressionParseVisitor */
class ExpressionParseVisitorTest {
    private static ExpressionParseVisitor epv = new ExpressionParseVisitor();

    @BeforeAll
    public static void setup() {
        epv.currentTuple = new Tuple("t", Arrays.asList(new String[] {"A", "B"}), Arrays.asList(new Integer[] {3, 10}));
    }

    @ParameterizedTest(name = "Expression Visitor Test {index}: expected {1}; actual {0} ")
    @MethodSource("argumentProvider")
    void testExpressionVisitor(String actual, boolean expected) throws ParseException, IOException {
        Expression exp = Helpers.strExpToExp(actual);
        exp.accept(epv);
        assertTrue(actual, expected == epv.booleanResult);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of("4<5", true),
                Arguments.of("5<4", false),
                Arguments.of("4<=4", true),
                Arguments.of("4<=3", false),
                Arguments.of("4<=5", true),
                Arguments.of("5=5", true),
                Arguments.of("5=6", false),
                Arguments.of("5!=6", true),
                Arguments.of("5!=5", false),
                Arguments.of("5>=4", true),
                Arguments.of("5>=5", true),
                Arguments.of("5>=6", false),
                Arguments.of("5>4", true),
                Arguments.of("5>5", false),
                Arguments.of("5>6", false),
                Arguments.of("5<3 AND 12=1", false),
                Arguments.of("3>4 AND 42=42", false),
                Arguments.of("4>3 AND 4<3", false),
                Arguments.of("4=4 AND 2=2", true),
                Arguments.of("5>3 AND 12=12 AND 3>2 AND 4<1", false),
                Arguments.of("5>3 AND 12=12 AND 3>2 AND 4>1", true),
                Arguments.of("t.A=3 AND t.B=10", true),
                Arguments.of("t.A=t.B", false));
    }
}
