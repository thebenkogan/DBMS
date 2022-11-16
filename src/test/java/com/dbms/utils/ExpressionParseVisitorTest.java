package com.dbms.utils;

import static com.dbms.utils.Helpers.strExpToExp;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private static Attribute tA = Attribute.bundle("t", "A");
    private static Attribute tB = Attribute.bundle("t", "B");

    @BeforeAll
    public static void setup() {
        epv.currentTuple = new Tuple(Schema.from("t", Arrays.asList(tA, tB)), Arrays.asList(3, 10));
    }

    @ParameterizedTest(name = "Expression Visitor Test {index}: expression {0} should evaluate to {1} ")
    @MethodSource("argumentProvider")
    void testExpressionVisitor(String expression, boolean expected) throws ParseException, IOException {
        Expression exp = strExpToExp(expression);
        exp.accept(epv);
        assertEquals(expected, epv.booleanResult);
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
