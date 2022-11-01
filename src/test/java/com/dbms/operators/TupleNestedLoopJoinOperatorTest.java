package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.operators.physical.TupleNestedLoopJoinOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.IOException;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the JoinOperator */
class TupleNestedLoopJoinOperatorTest {
    private static TupleNestedLoopJoinOperator joinOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/general/config.txt");
        ScanOperator scanOp1 = new ScanOperator("Sailors");
        ScanOperator scanOp2 = new ScanOperator("Reserves");
        Expression exp1 = Helpers.strExpToExp("Sailors.A = 106");
        Expression exp2 = Helpers.strExpToExp("Reserves.H = 23");
        SelectOperator selectOp1 = new SelectOperator(scanOp1, exp1);
        SelectOperator selectOp2 = new SelectOperator(scanOp2, exp2);
        Expression joinExp = Helpers.strExpToExp("Sailors.A = Reserves.G");
        joinOp = new TupleNestedLoopJoinOperator(selectOp1, selectOp2, joinExp);
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
    void testReset(String expected, TupleNestedLoopJoinOperator actual) throws IOException {
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
