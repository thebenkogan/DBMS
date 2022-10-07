package com.dbms.visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Helpers;
import java.util.Arrays;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the JoinVisitor */
class JoinVisitorTest {

    private static String name1 = "t";
    private static String name2 = "b";
    private static String name3 = "c";

    private static JoinVisitor jv = new JoinVisitor(Arrays.asList(new String[] {name1, name2, name3}));
    private static Expression exp =
            Helpers.strExpToExp("b.A = t.G AND t.H > 3 AND 99 < b.Y AND b.A = c.A AND t.A = c.A");

    @BeforeAll
    public static void setup() {
        exp.accept(jv);
    }

    @ParameterizedTest(name = "Join Visitor Test {index}: expected {1}; actual {0} ")
    @MethodSource("argumentProvider")
    void testJoinVisitor(String actual, String expected) {
        assertEquals(actual, expected);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of(jv.getExpression(name1).toString(), "t.H > 3"),
                Arguments.of(jv.getExpression(name2).toString(), "99 < b.Y"),
                Arguments.of(jv.getExpression(name1, name2).toString(), "b.A = t.G"),
                Arguments.of(
                        jv.getExpression(name3, Arrays.asList(new String[] {name1, name2}))
                                .toString(),
                        "t.A = c.A AND b.A = c.A"));
    }
}
