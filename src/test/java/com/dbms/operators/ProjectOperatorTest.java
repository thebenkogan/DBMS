package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the ProjectOperator */
class ProjectOperatorTest {
    private static List<SelectItem> items;
    private static Expression exp;
    private static ScanOperator scanOp;
    private static SelectOperator selectOp;
    private static ProjectOperator projectOp;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/general/config.txt");
        items = Helpers.strSelectItemsToSelectItems("Boats.F", "Boats.D");
        exp = Helpers.strExpToExp("Boats.D = 32 AND Boats.E != 100");
        scanOp = new ScanOperator("Boats");
        selectOp = new SelectOperator(scanOp, exp);
        projectOp = new ProjectOperator(selectOp, items);
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
    void testReset(String expected, ProjectOperator actual) throws IOException {
        actual.reset();
        assertEquals(expected, actual.getNextTuple().toString());
    }

    private static Stream<Arguments> nextTupleProvider() {
        return Stream.of(
                Arguments.of("191,32", projectOp.getNextTuple().toString()),
                Arguments.of("178,32", projectOp.getNextTuple().toString()),
                Arguments.of("66,32", projectOp.getNextTuple().toString()),
                Arguments.of("84,32", projectOp.getNextTuple().toString()),
                Arguments.of("129,32", projectOp.getNextTuple().toString()),
                Arguments.of("161,32", projectOp.getNextTuple().toString()),
                Arguments.of("122,32", projectOp.getNextTuple().toString()),
                Arguments.of("138,32", projectOp.getNextTuple().toString()),
                Arguments.of("null", projectOp.getNextTuple()));
    }

    private static Stream<Arguments> resetProvider() {
        return Stream.of(Arguments.of("191,32", projectOp), Arguments.of("191,32", projectOp));
    }
}
