package com.dbms.operators.physical;

import static com.dbms.utils.Helpers.strExpToExp;
import static com.dbms.utils.Helpers.strOrderBysToOrderBys;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.utils.Catalog;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the SortOperator */
class ExternalSortOperatorTest {
    private static List<OrderByElement> orderByElements;
    private static Expression exp;
    private static ScanOperator scanOp;
    private static SelectOperator selectOp;
    private static ExternalSortOperator sortOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/general/config.txt");
        orderByElements = strOrderBysToOrderBys("Boats.E");
        exp = strExpToExp("Boats.D = 32 AND Boats.E != 100");
        scanOp = new ScanOperator("Boats");
        selectOp = new SelectOperator(scanOp, exp);
        sortOp = new ExternalSortOperator(selectOp, orderByElements, 5);
    }

    @AfterAll
    public static void finish() throws IOException {
        // call next tuple until null so the reader is closed
        while (sortOp.getNextTuple() != null) {}
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
    void testReset(String expected, ExternalSortOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("32,20,161", sortOp.getNextTuple().toString()),
                Arguments.of("32,54,122", sortOp.getNextTuple().toString()),
                Arguments.of("32,72,66", sortOp.getNextTuple().toString()),
                Arguments.of("32,90,191", sortOp.getNextTuple().toString()),
                Arguments.of("32,121,84", sortOp.getNextTuple().toString()),
                Arguments.of("32,126,129", sortOp.getNextTuple().toString()),
                Arguments.of("32,138,178", sortOp.getNextTuple().toString()),
                Arguments.of("32,180,138", sortOp.getNextTuple().toString()),
                Arguments.of("null", sortOp.getNextTuple()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("32,20,161", sortOp), Arguments.of("32,20,161", sortOp));
    }
}
