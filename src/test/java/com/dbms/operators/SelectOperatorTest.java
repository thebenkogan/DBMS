package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the SelectOperator */
class SelectOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input", null);
    }

    SelectOperator getOperator() throws FileNotFoundException {
        Expression exp= Helpers.strExpToExp("Boats.D > 102 AND Boats.E != 1");
        ScanOperator scanOp= new ScanOperator("Boats");
        return new SelectOperator(scanOp, exp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        SelectOperator selectOp= getOperator();

        assertEquals("104,104,2", selectOp.getNextTuple().toString());
        assertEquals("107,2,8", selectOp.getNextTuple().toString());
        assertNull(selectOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        SelectOperator selectOp= getOperator();

        assertEquals("104,104,2", selectOp.getNextTuple().toString());
        selectOp.reset();
        assertEquals("104,104,2", selectOp.getNextTuple().toString());
    }
}
