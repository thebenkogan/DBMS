package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
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
        Catalog.init("samples2/input", null, null);
    }

    SelectOperator getOperator() throws FileNotFoundException {
        Expression exp = Helpers.strExpToExp("Boats.D = 32 AND Boats.E != 100");
        ScanOperator scanOp = new ScanOperator("Boats");
        return new SelectOperator(scanOp, exp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        SelectOperator selectOp = getOperator();

        assertEquals("32,90,191", selectOp.getNextTuple().toString());
        assertEquals("32,138,178", selectOp.getNextTuple().toString());
        assertEquals("32,72,66", selectOp.getNextTuple().toString());
        assertEquals("32,121,84", selectOp.getNextTuple().toString());
        assertEquals("32,126,129", selectOp.getNextTuple().toString());
        assertEquals("32,20,161", selectOp.getNextTuple().toString());
        assertEquals("32,54,122", selectOp.getNextTuple().toString());
        assertEquals("32,180,138", selectOp.getNextTuple().toString());
        assertNull(selectOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        SelectOperator selectOp = getOperator();

        assertEquals("32,90,191", selectOp.getNextTuple().toString());
        selectOp.reset();
        assertEquals("32,90,191", selectOp.getNextTuple().toString());
    }
}
