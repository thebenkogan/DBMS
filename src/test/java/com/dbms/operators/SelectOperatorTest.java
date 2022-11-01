package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.IOException;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the SelectOperator */
class SelectOperatorTest {
    private static Expression exp;
    private static SelectOperator selectOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/general/config.txt");
        exp = Helpers.strExpToExp("Boats.D = 32 AND Boats.E != 100");
        selectOp = new SelectOperator(new ScanOperator("Boats"), exp);
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
    void testReset(String expected, SelectOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("32,90,191", selectOp.getNextTuple().toString()),
                Arguments.of("32,138,178", selectOp.getNextTuple().toString()),
                Arguments.of("32,72,66", selectOp.getNextTuple().toString()),
                Arguments.of("32,121,84", selectOp.getNextTuple().toString()),
                Arguments.of("32,126,129", selectOp.getNextTuple().toString()),
                Arguments.of("32,20,161", selectOp.getNextTuple().toString()),
                Arguments.of("32,54,122", selectOp.getNextTuple().toString()),
                Arguments.of("32,180,138", selectOp.getNextTuple().toString()),
                Arguments.of("null", selectOp.getNextTuple()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("32,90,191", selectOp), Arguments.of("32,90,191", selectOp));
    }
}
