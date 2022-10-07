package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.ScanOperator;
import com.dbms.utils.Catalog;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the ScanOperator */
class ScanOperatorTest {
    private static ScanOperator scanOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null, null);
        scanOp = new ScanOperator("Boats");
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
    void testReset(String expected, ScanOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("12,143,196", scanOp.getNextTuple().toString()),
                Arguments.of("30,63,101", scanOp.getNextTuple().toString()),
                Arguments.of("57,24,130", scanOp.getNextTuple().toString()),
                Arguments.of("172,68,43", scanOp.getNextTuple().toString()),
                Arguments.of("61,58,36", scanOp.getNextTuple().toString()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("12,143,196", scanOp), Arguments.of("12,143,196", scanOp));
    }
}
