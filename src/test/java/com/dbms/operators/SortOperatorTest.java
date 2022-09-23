package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.operators.physical.SortOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the SortOperator */
class SortOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null);
    }

    SortOperator getOperator() throws FileNotFoundException {
        List<OrderByElement> orderByElements = Helpers.strOrderBysToOrderBys("Boats.E");
        Expression exp = Helpers.strExpToExp("Boats.D = 32 AND Boats.E != 100");
        ScanOperator scanOp = new ScanOperator("Boats");
        SelectOperator selectOp = new SelectOperator(scanOp, exp);
        return new SortOperator(selectOp, orderByElements);
    }

    @Test
    void testGetNextTuple() throws IOException {
        SortOperator sortOp = getOperator();

        assertEquals("32,20,161", sortOp.getNextTuple().toString());
        assertEquals("32,54,122", sortOp.getNextTuple().toString());
        assertEquals("32,72,66", sortOp.getNextTuple().toString());
        assertEquals("32,90,191", sortOp.getNextTuple().toString());
        assertEquals("32,121,84", sortOp.getNextTuple().toString());
        assertEquals("32,126,129", sortOp.getNextTuple().toString());
        assertEquals("32,138,178", sortOp.getNextTuple().toString());
        assertEquals("32,180,138", sortOp.getNextTuple().toString());
        assertNull(sortOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        SortOperator sortOp = getOperator();

        assertEquals("32,20,161", sortOp.getNextTuple().toString());
        sortOp.reset();
        assertEquals("32,20,161", sortOp.getNextTuple().toString());
    }
}
