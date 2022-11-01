package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.operators.physical.DuplicateEliminationOperator;
import com.dbms.operators.physical.InMemorySortOperator;
import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the DuplicateEliminationOperator */
class DuplicateEliminationOperatorTest {

    private static List<SelectItem> items;
    private static ScanOperator scanOp;
    private static Expression exp;
    private static SelectOperator selectOp;
    private static ProjectOperator projectOp;
    private static List<OrderByElement> orderBys;
    private static InMemorySortOperator sortOp;
    private static DuplicateEliminationOperator duplicateOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/general/config.txt");
        items = Helpers.strSelectItemsToSelectItems("Reserves.H");
        scanOp = new ScanOperator("Reserves");
        exp = Helpers.strExpToExp("Reserves.H > 185");
        selectOp = new SelectOperator(scanOp, exp);
        projectOp = new ProjectOperator(selectOp, items);
        orderBys = Helpers.strOrderBysToOrderBys("Reserves.H");
        sortOp = new InMemorySortOperator(projectOp, orderBys);
        duplicateOp = new DuplicateEliminationOperator(sortOp);
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
    void testReset(String expected, DuplicateEliminationOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("186", duplicateOp.getNextTuple().toString()),
                Arguments.of("187", duplicateOp.getNextTuple().toString()),
                Arguments.of("188", duplicateOp.getNextTuple().toString()),
                Arguments.of("189", duplicateOp.getNextTuple().toString()),
                Arguments.of("190", duplicateOp.getNextTuple().toString()),
                Arguments.of("191", duplicateOp.getNextTuple().toString()),
                Arguments.of("192", duplicateOp.getNextTuple().toString()),
                Arguments.of("193", duplicateOp.getNextTuple().toString()),
                Arguments.of("194", duplicateOp.getNextTuple().toString()),
                Arguments.of("195", duplicateOp.getNextTuple().toString()),
                Arguments.of("196", duplicateOp.getNextTuple().toString()),
                Arguments.of("197", duplicateOp.getNextTuple().toString()),
                Arguments.of("198", duplicateOp.getNextTuple().toString()),
                Arguments.of("199", duplicateOp.getNextTuple().toString()),
                Arguments.of("200", duplicateOp.getNextTuple().toString()),
                Arguments.of("null", duplicateOp.getNextTuple()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("186", duplicateOp), Arguments.of("186", duplicateOp));
    }
}
