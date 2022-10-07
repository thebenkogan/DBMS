package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.JoinOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import com.dbms.visitors.JoinVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the JoinOperator */
class JoinOperatorTest {
    private static ScanOperator scanOp1;
    private static ScanOperator scanOp2;
    private static JoinVisitor jv;
    private static Expression exp;
    private static JoinOperator joinOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null, null);
        scanOp1 = new ScanOperator("Sailors");
        scanOp2 = new ScanOperator("Reserves");
        jv = new JoinVisitor(Arrays.asList(new String[] {"Sailors", "Reserves"}));
        exp = Helpers.strExpToExp("Sailors.A = Reserves.G AND Reserves.H = 23 AND Sailors.A = 106");
        exp.accept(jv);
        joinOp = new JoinOperator(scanOp1, scanOp2, exp);
    }

    @ParameterizedTest(name = "Next Tuple Test {index}: expected {0}; actual {1} ")
    @MethodSource("nextTupleProvider")
    void testGetNextTuple(String expected, String actual) throws IOException {
        if (expected.equals("null") && actual == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest(name = "Reset Test {index}: expected {0}; actual {1} ")
    @MethodSource("resetProvider")
    void testReset(String expected, JoinOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("106,39,42,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("106,102,163,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("106,71,138,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("106,99,118,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("106,59,191,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("106,142,77,106,23", joinOp.getNextTuple().toString()),
                Arguments.of("null", joinOp.getNextTuple()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("106,39,42,106,23", joinOp), Arguments.of("106,39,42,106,23", joinOp));
    }
}
