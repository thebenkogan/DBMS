package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.DuplicateEliminationOperator;
import com.dbms.operators.physical.InMemorySortOperator;
import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the DuplicateEliminationOperator */
class DuplicateEliminationOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null, null);
    }

    DuplicateEliminationOperator getOperator() throws FileNotFoundException {
        List<SelectItem> items = Helpers.strSelectItemsToSelectItems("Reserves.H");
        ScanOperator scanOp = new ScanOperator("Reserves");
        Expression exp = Helpers.strExpToExp("Reserves.H > 185");
        SelectOperator selectOp = new SelectOperator(scanOp, exp);
        ProjectOperator projectOp = new ProjectOperator(selectOp, items);
        List<OrderByElement> orderBys = Helpers.strOrderBysToOrderBys("Reserves.H");
        InMemorySortOperator sortOp = new InMemorySortOperator(projectOp, orderBys);
        return new DuplicateEliminationOperator(sortOp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        DuplicateEliminationOperator duplicateOp = getOperator();

        assertEquals("186", duplicateOp.getNextTuple().toString());
        assertEquals("187", duplicateOp.getNextTuple().toString());
        assertEquals("188", duplicateOp.getNextTuple().toString());
        assertEquals("189", duplicateOp.getNextTuple().toString());
        assertEquals("190", duplicateOp.getNextTuple().toString());
        assertEquals("191", duplicateOp.getNextTuple().toString());
        assertEquals("192", duplicateOp.getNextTuple().toString());
        assertEquals("193", duplicateOp.getNextTuple().toString());
        assertEquals("194", duplicateOp.getNextTuple().toString());
        assertEquals("195", duplicateOp.getNextTuple().toString());
        assertEquals("196", duplicateOp.getNextTuple().toString());
        assertEquals("197", duplicateOp.getNextTuple().toString());
        assertEquals("198", duplicateOp.getNextTuple().toString());
        assertEquals("199", duplicateOp.getNextTuple().toString());
        assertEquals("200", duplicateOp.getNextTuple().toString());
        assertNull(duplicateOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        DuplicateEliminationOperator duplicateOp = getOperator();

        assertEquals("186", duplicateOp.getNextTuple().toString());
        duplicateOp.reset();
        assertEquals("186", duplicateOp.getNextTuple().toString());
    }
}
