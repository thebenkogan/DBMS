package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the DuplicateEliminationOperator */
class DuplicateEliminationOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input", null);
    }

    DuplicateEliminationOperator getOperator() throws FileNotFoundException {
        List<SelectItem> items= Helpers.strSelectItemsToSelectItems("Reserves.H");
        ScanOperator scanOp= new ScanOperator("Reserves");
        ProjectOperator projectOp= new ProjectOperator(scanOp, items);
        List<OrderByElement> orderBys= Helpers.strOrderBysToOrderBys("Reserves.H");
        SortOperator sortOp= new SortOperator(projectOp, orderBys);
        return new DuplicateEliminationOperator(sortOp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        DuplicateEliminationOperator duplicateOp= getOperator();

        assertEquals("101", duplicateOp.getNextTuple().toString());
        assertEquals("102", duplicateOp.getNextTuple().toString());
        assertEquals("103", duplicateOp.getNextTuple().toString());
        assertEquals("104", duplicateOp.getNextTuple().toString());
        assertNull(duplicateOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        DuplicateEliminationOperator duplicateOp= getOperator();

        assertEquals("101", duplicateOp.getNextTuple().toString());
        duplicateOp.reset();
        assertEquals("101", duplicateOp.getNextTuple().toString());
    }
}
