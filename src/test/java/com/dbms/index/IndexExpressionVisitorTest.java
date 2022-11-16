package com.dbms.index;

import static com.dbms.utils.Helpers.str;
import static com.dbms.utils.Helpers.strExpToExp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Attribute;
import java.io.IOException;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IndexExpressionVisitorTest {
    private static IndexExpressionVisitor iv1 =
            new IndexExpressionVisitor(new Index(Attribute.bundle("A", "B"), 10, false));
    private static IndexExpressionVisitor iv2 =
            new IndexExpressionVisitor(new Index(Attribute.bundle("A", "C"), 10, true));
    private static IndexExpressionVisitor iv3 =
            new IndexExpressionVisitor(new Index(Attribute.bundle("A", "D"), 10, false));
    private static IndexExpressionVisitor iv4 =
            new IndexExpressionVisitor(new Index(Attribute.bundle("Whocares", "Me"), 10, false));

    @BeforeAll
    public static void setup() throws IOException {
        Expression exp1 = strExpToExp(String.join(" AND ", "A.B <= 69", "A.B >= 6", "A.B >= 9"));
        Expression exp2 = strExpToExp(String.join(" AND ", "A.C = 69", "A.C = 69", "A.D = 7"));
        Expression exp3 = strExpToExp(String.join(" AND ", "A.D < 70", "A.D > 68", "A.C = 69"));
        Expression exp4 = strExpToExp(String.join(" AND ", "A.D = 4", "Whocares.Me < 2"));
        exp1.accept(iv1);
        exp2.accept(iv2);
        exp3.accept(iv3);
        exp4.accept(iv4);
    }

    @ParameterizedTest(name = "IndexExpression Test {index}: expected {1}; actual {0} ")
    @MethodSource("argumentProvider")
    void testUnionFindVisitor(String actual, String expected) {
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of(str(iv1.high), "69"),
                Arguments.of(str(iv1.low), "9"),
                Arguments.of(str(iv2.high), "69"),
                Arguments.of(str(iv2.low), "69"),
                Arguments.of(str(iv3.high), "69"),
                Arguments.of(str(iv3.low), "69"),
                Arguments.of(str(iv4.high), "1"),
                Arguments.of(str(iv4.low), "null"));
    }
}
